package com.jagex.util;

import com.jagex.io.Buffer;

public final class ChatMessageCodec {

	public static char[] message = new char[100];

	private static Buffer buffer = new Buffer(new byte[100]);

	private static final char[] VALID_CHARACTERS = { ' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r', 'd', 'l', 'u', 'm', 'w',
			'c', 'y', 'f', 'g', 'p', 'b', 'v', 'k', 'x', 'j', 'q', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ',
			'!', '?', '.', ',', ':', ';', '(', ')', '-', '&', '*', '\\', '\'', '@', '#', '+', '=', '\243', '$', '%', '"', '[',
			']' };

	public static String decode(Buffer buffer, int length) {
		int index = 0;
		int next = -1;

		for (int i = 0; i < length; i++) {
			int in = buffer.readUByte();
			int charIndex = in >> 4 & 0xF;

			if (next == -1) {
				if (charIndex < 13) {
					message[index++] = VALID_CHARACTERS[charIndex];
				} else {
					next = charIndex;
				}
			} else {
				message[index++] = VALID_CHARACTERS[(next << 4) + charIndex - 195];
				next = -1;
			}

			charIndex = in & 0xf;
			if (next == -1) {
				if (charIndex < 13) {
					message[index++] = VALID_CHARACTERS[charIndex];
				} else {
					next = charIndex;
				}
			} else {
				message[index++] = VALID_CHARACTERS[(next << 4) + charIndex - 195];
				next = -1;
			}
		}

		boolean capitaliseNext = true;
		for (int i = 0; i < index; i++) {
			char character = message[i];
			if (capitaliseNext && character >= 'a' && character <= 'z') {
				message[i] += '\uFFE0';
				capitaliseNext = false;
			}

			if (character == '.' || character == '!' || character == '?') {
				capitaliseNext = true;
			}
		}

		return new String(message, 0, index);
	}

	public static void encode(String string, Buffer buffer) {
		if (string.length() > 80) {
			string = string.substring(0, 80);
		}

		string = string.toLowerCase();
		int next = -1;

		for (int index = 0; index < string.length(); index++) {
			char character = string.charAt(index);
			int charIndex = 0;
			for (int i = 0; i < VALID_CHARACTERS.length; i++) {
				if (character != VALID_CHARACTERS[i]) {
					continue;
				}
				charIndex = i;
				break;
			}

			if (charIndex > 12) {
				charIndex += 195;
			}

			if (next == -1) {
				if (charIndex < 13) {
					next = charIndex;
				} else {
					buffer.writeByte(charIndex);
				}
			} else if (charIndex < 13) {
				buffer.writeByte((next << 4) + charIndex);
				next = -1;
			} else {
				buffer.writeByte((next << 4) + (charIndex >> 4));
				next = charIndex & 0xF;
			}
		}

		if (next != -1) {
			buffer.writeByte(next << 4);
		}
	}

	public static String verify(String string) {
		buffer.setPosition(0);
		encode(string, buffer);
		int length = buffer.getPosition();
		buffer.setPosition(0);
		return decode(buffer, length);
	}

}