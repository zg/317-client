package com.jagex.setting;

import com.jagex.cache.Archive;
import com.jagex.io.Buffer;

public class VariableBits {

	public static VariableBits[] bits;
	private static int count;

	public static int getCount() {
		return count;
	}

	public static void init(Archive archive) {
		Buffer buffer = new Buffer(archive.getEntry("varbit.dat"));
		count = buffer.readUShort();
		if (bits == null) {
			bits = new VariableBits[count];
		}

		for (int i = 0; i < count; i++) {
			if (bits[i] == null) {
				bits[i] = new VariableBits();
			}
			bits[i].decode(buffer);
		}

		if (buffer.getPosition() != buffer.getPayload().length) {
			System.out.println("varbit load mismatch");
		}
	}

	private int high;
	private int low;
	private int setting;

	public void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				return;
			}

			if (opcode == 1) {
				setting = buffer.readUShort();
				low = buffer.readUByte();
				high = buffer.readUByte();
			} else if (opcode == 10) {
				buffer.readString();
			} else if (opcode == 3 || opcode == 4) {
				buffer.readInt();
			} else if (opcode != 2) {
				System.out.println("Error unrecognised config code: " + opcode);
			}
		} while (true);
	}

	public int getHigh() {
		return high;
	}

	public int getLow() {
		return low;
	}

	public int getSetting() {
		return setting;
	}

}