package com.jagex.cache;

import java.io.IOException;
import java.io.RandomAccessFile;

public final class Index {

	private static byte[] buffer = new byte[520];
	private RandomAccessFile data;
	private int file;
	private RandomAccessFile index;
	private int maximumSize;

	public Index(RandomAccessFile index, RandomAccessFile data, int file, int maximumFileSize) {
		this.file = file;
		this.data = data;
		this.index = index;
		this.maximumSize = maximumFileSize;
	}

	public synchronized byte[] decompress(int indexFile) {
		try {
			seek(index, indexFile * 6);
			for (int in = 0, read = 0; read < 6; read += in) {
				in = index.read(buffer, read, 6 - read);
				if (in == -1) {
					return null;
				}
			}

			int size = ((buffer[0] & 0xff) << 16) + ((buffer[1] & 0xff) << 8) + (buffer[2] & 0xff);
			int sector = ((buffer[3] & 0xff) << 16) + ((buffer[4] & 0xff) << 8) + (buffer[5] & 0xff);

			if (size < 0 || size > maximumSize) {
				return null;
			} else if (sector <= 0 || sector > data.length() / 520L) {
				return null;
			}

			byte[] decompressed = new byte[size];
			int totalRead = 0;

			for (int part = 0; totalRead < size; part++) {
				if (sector == 0) {
					return null;
				}

				seek(data, sector * 520);
				int unread = Math.min(size - totalRead, 512);

				for (int in = 0, read = 0; read < unread + 8; read += in) {
					in = data.read(buffer, read, unread + 8 - read);
					if (in == -1) {
						return null;
					}
				}

				int currentIndex = ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
				int currentPart = ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
				int nextSector = ((buffer[4] & 0xff) << 16) + ((buffer[5] & 0xff) << 8) + (buffer[6] & 0xff);
				int currentFile = buffer[7] & 0xff;

				if (currentIndex != indexFile || currentPart != part || currentFile != file) {
					return null;
				} else if (nextSector < 0 || nextSector > data.length() / 520L) {
					return null;
				}

				for (int i = 0; i < unread; i++) {
					decompressed[totalRead++] = buffer[i + 8];
				}

				sector = nextSector;
			}

			return decompressed;
		} catch (IOException ex) {
			return null;
		}
	}

	public synchronized boolean put(byte[] data, int index, int length) {
		return put(data, index, length, true) ? true : put(data, index, length, false);
	}

	public synchronized void seek(RandomAccessFile file, int position) throws IOException {
		if (position < 0 || position > 0x3c00000) {
			System.out.println("Badseek - pos:" + position + " len:" + file.length());
			position = 0x3c00000;

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ex) {
			}
		}

		file.seek(position);
	}

	private synchronized boolean put(byte[] bytes, int position, int length, boolean exists) {
		try {
			int sector;
			if (exists) {
				seek(index, position * 6);
				for (int in = 0, read = 0; read < 6; read += in) {
					in = index.read(buffer, read, 6 - read);
					if (in == -1) {
						return false;
					}
				}

				sector = ((buffer[3] & 0xff) << 16) + ((buffer[4] & 0xff) << 8) + (buffer[5] & 0xff);
				if (sector <= 0 || sector > data.length() / 520) {
					return false;
				}
			} else {
				sector = (int) ((data.length() + 519) / 520);
				if (sector == 0) {
					sector = 1;
				}
			}

			buffer[0] = (byte) (length >> 16);
			buffer[1] = (byte) (length >> 8);
			buffer[2] = (byte) length;
			buffer[3] = (byte) (sector >> 16);
			buffer[4] = (byte) (sector >> 8);
			buffer[5] = (byte) sector;
			seek(index, position * 6);
			index.write(buffer, 0, 6);
			int written = 0;

			for (int part = 0; written < length; part++) {
				int nextSector = 0;

				if (exists) {
					seek(data, sector * 520);
					int read = 0;

					for (int in = 0; read < 8; read += in) {
						in = data.read(buffer, read, 8 - read);
						if (in == -1) {
							break;
						}
					}

					if (read == 8) {
						int currentIndex = ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
						int currentPart = ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
						nextSector = ((buffer[4] & 0xff) << 16) + ((buffer[5] & 0xff) << 8) + (buffer[6] & 0xff);
						int currentFile = buffer[7] & 0xff;

						if (currentIndex != position || currentPart != part || currentFile != file) {
							return false;
						} else if (nextSector < 0 || nextSector > data.length() / 520) {
							return false;
						}
					}
				}

				if (nextSector == 0) {
					exists = false;
					nextSector = (int) ((data.length() + 519) / 520);
					if (nextSector == 0) {
						nextSector++;
					}

					if (nextSector == sector) {
						nextSector++;
					}
				}

				if (length - written <= 512) {
					nextSector = 0;
				}

				buffer[0] = (byte) (position >> 8);
				buffer[1] = (byte) position;
				buffer[2] = (byte) (part >> 8);
				buffer[3] = (byte) part;
				buffer[4] = (byte) (nextSector >> 16);
				buffer[5] = (byte) (nextSector >> 8);
				buffer[6] = (byte) nextSector;
				buffer[7] = (byte) file;
				seek(data, sector * 520);
				data.write(buffer, 0, 8);
				int unwritten = length - written;

				if (unwritten > 512) {
					unwritten = 512;
				}

				data.write(bytes, written, unwritten);
				written += unwritten;
				sector = nextSector;
			}

			return true;
		} catch (IOException ex) {
			return false;
		}
	}

}