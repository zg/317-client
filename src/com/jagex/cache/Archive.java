package com.jagex.cache;

import com.jagex.io.Buffer;

/**
 * A container used in the cache, which stores sub-containers, called entries.
 * <p>
 * Archives may be compressed in whole, or have their individual entries compressed. Both types use Bzip2 for
 * compression.
 */
public final class Archive {

	/**
	 * The amount of entries in this Archive.
	 */
	private int entries;

	/**
	 * Whether or not this Archive was compressed as a whole: if false, decompression will be performed on each of the
	 * individual entries.
	 */
	private boolean extracted;

	/**
	 * The raw (i.e. decompressed) sizes of each of the entries in this Archive.
	 */
	private int[] extractedSizes;

	/**
	 * The identifiers (i.e. hashed names) of each of the entries in this Archive.
	 */
	private int[] identifiers;
	private int[] indices;

	/**
	 * The buffer containing the decompressed data in this Archive.
	 */
	private byte[] buffer;

	/**
	 * The compressed sizes of each of the entries in this Archive.
	 */
	private int[] sizes;

	public Archive(byte[] data) {
		Buffer buffer = new Buffer(data);
		int length = buffer.readTriByte();
		int decompressedLength = buffer.readTriByte();

		if (decompressedLength != length) {
			byte[] output = new byte[length];
			BZip2Decompressor.decompress(output, length, data, decompressedLength, 6);

			this.buffer = output;
			buffer = new Buffer(output);
			extracted = true;
		} else {
			this.buffer = data;
		}

		entries = buffer.readUShort();
		identifiers = new int[entries];
		extractedSizes = new int[entries];
		sizes = new int[entries];
		indices = new int[entries];

		int offset = buffer.getPosition() + entries * 10;
		for (int file = 0; file < entries; file++) {
			identifiers[file] = buffer.readInt();
			extractedSizes[file] = buffer.readTriByte();
			sizes[file] = buffer.readTriByte();
			indices[file] = offset;
			offset += sizes[file];
		}
	}

	public byte[] getEntry(String name) {
		int hash = 0;
		name = name.toUpperCase();
		for (int index = 0; index < name.length(); index++) {
			hash = hash * 61 + name.charAt(index) - 32;
		}

		for (int file = 0; file < entries; file++) {
			if (identifiers[file] == hash) {
				byte[] output = new byte[extractedSizes[file]];

				if (!extracted) {
					BZip2Decompressor.decompress(output, extractedSizes[file], this.buffer, sizes[file], indices[file]);
				} else {
					for (int index = 0; index < extractedSizes[file]; index++) {
						output[index] = this.buffer[indices[file] + index];
					}
				}

				return output;
			}
		}

		return null;
	}

}