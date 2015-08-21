package com.jagex.util;

import com.jagex.cache.Archive;
import com.jagex.io.Buffer;

public class MessageCensor {

	private static byte[][][] badEncoding;
	private static char[][] badWords;
	private static char[][] domains;
	private static final String[] EXCEPTIONS = { "cook", "cook's", "cooks", "seeks", "sheet", "woop", "woops", "faq", "noob",
			"noobs" };
	private static int[] fragments;
	private static char[][] tldList;

	private static int[] tlds;

	public static String apply(String message) {
		char[] chars = message.toCharArray();
		stripIllegalCharacters(chars);
		String trimmed = new String(chars).trim();
		chars = trimmed.toLowerCase().toCharArray();
		String trimmedLowerCase = trimmed.toLowerCase();
		censorDomains(chars);
		censorBad(chars);
		method501(chars);
		method514(chars);

		for (String exception : EXCEPTIONS) {
			for (int index = -1; (index = trimmedLowerCase.indexOf(exception, index + 1)) != -1;) {
				char[] exceptionChars = exception.toCharArray();
				for (int i = 0; i < exceptionChars.length; i++) {
					chars[i + index] = exceptionChars[i];
				}
			}
		}

		copyCase(trimmed.toCharArray(), chars);
		capitalise(chars);
		return new String(chars).trim();
	}

	public static int hash(char[] chars) {
		if (chars.length > 6) {
			return 0;
		}

		int code = 0;
		for (int index = 0; index < chars.length; index++) {
			char character = chars[chars.length - index - 1];
			if (character >= 'a' && character <= 'z') {
				code = code * 38 + character - 97 + 1;
			} else if (character == '\'') {
				code = code * 38 + 27;
			} else if (character >= '0' && character <= '9') {
				code = code * 38 + character - 48 + 28;
			} else if (character != 0) {
				return 0;
			}
		}

		return code;
	}

	public static void init(Archive archive) {
		Buffer fragments = new Buffer(archive.getEntry("fragmentsenc.txt"));
		Buffer bad = new Buffer(archive.getEntry("badenc.txt"));
		Buffer domain = new Buffer(archive.getEntry("domainenc.txt"));
		Buffer tldlist = new Buffer(archive.getEntry("tldlist.txt"));
		decode(fragments, bad, domain, tldlist);
	}

	public static void method509(byte[][] complex, char[] message, char[] word) {
		if (word.length > message.length) {
			return;
		}

		int j;
		for (int i = 0; i <= message.length - word.length; i += j) {
			int censorEnd = i;
			int characterCount = 0;
			int j1 = 0;
			j = 1;

			boolean finalCharacterIsNotAlphanumeric = false;
			boolean nextCharacterIsDigit = false;
			boolean finalCharacterIsDigit = false;

			while (censorEnd < message.length && (!nextCharacterIsDigit || !finalCharacterIsDigit)) {
				int k1 = 0;
				char charAtEnd = message[censorEnd];
				char nextCharacter = '\0';

				if (censorEnd + 1 < message.length) {
					nextCharacter = message[censorEnd + 1];
				}

				if (characterCount < word.length && (k1 = method512(nextCharacter, charAtEnd, word[characterCount])) > 0) {
					if (k1 == 1 && isDigit(charAtEnd)) {
						nextCharacterIsDigit = true;
					}
					if (k1 == 2 && (isDigit(charAtEnd) || isDigit(nextCharacter))) {
						nextCharacterIsDigit = true;
					}
					censorEnd += k1;
					characterCount++;
					continue;
				}

				if (characterCount == 0) {
					break;
				}

				if ((k1 = method512(nextCharacter, charAtEnd, word[characterCount - 1])) > 0) {
					censorEnd += k1;
					if (characterCount == 1) {
						j++;
					}
					continue;
				}
				if (characterCount >= word.length || !method518(charAtEnd)) {
					break;
				}
				if (isNotAlphanumeric(charAtEnd) && charAtEnd != '\'') {
					finalCharacterIsNotAlphanumeric = true;
				}
				if (isDigit(charAtEnd)) {
					finalCharacterIsDigit = true;
				}
				censorEnd++;
				if (++j1 * 100 / (censorEnd - i) > 90) {
					break;
				}
			}
			if (characterCount >= word.length && (!nextCharacterIsDigit || !finalCharacterIsDigit)) {
				boolean applyCensor = true;
				if (!finalCharacterIsNotAlphanumeric) {
					char characterBeforeBeginning = ' ';
					if (i - 1 >= 0) {
						characterBeforeBeginning = message[i - 1];
					}
					char characterAtEnd = ' ';
					if (censorEnd < message.length) {
						characterAtEnd = message[censorEnd];
					}
					byte firstCharacterCode = getCensoringCharacterCode(characterBeforeBeginning);
					byte finalCharacterCode = getCensoringCharacterCode(characterAtEnd);
					if (complex != null && method510(firstCharacterCode, complex, finalCharacterCode)) {
						applyCensor = false;
					}
				} else {
					boolean initalCharacterIsInvalid = false;
					boolean finalCharacterIsInvalid = false;
					if (i - 1 < 0 || isNotAlphanumeric(message[i - 1]) && message[i - 1] != '\'') {
						initalCharacterIsInvalid = true;
					}
					if (censorEnd >= message.length || isNotAlphanumeric(message[censorEnd]) && message[censorEnd] != '\'') {
						finalCharacterIsInvalid = true;
					}
					if (!initalCharacterIsInvalid || !finalCharacterIsInvalid) {
						boolean valid = false;
						int position = i - 2;
						if (initalCharacterIsInvalid) {
							position = i;
						}
						for (; !valid && position < censorEnd; position++) {
							if (position >= 0 && (!isNotAlphanumeric(message[position]) || message[position] == '\'')) {
								char chars[] = new char[3];
								int idx;
								for (idx = 0; idx < 3; idx++) {
									if (position + idx >= message.length || isNotAlphanumeric(message[position + idx])
											&& message[position + idx] != '\'') {
										break;
									}
									chars[idx] = message[position + idx];
								}

								boolean ok = true;
								if (idx == 0) {
									ok = false;
								}
								if (idx < 3 && position - 1 >= 0
										&& (!isNotAlphanumeric(message[position - 1]) || message[position - 1] == '\'')) {
									ok = false;
								}
								if (ok && !containsFragment(chars)) {
									valid = true;
								}
							}
						}

						if (!valid) {
							applyCensor = false;
						}
					}
				}
				if (applyCensor) {
					int digitCount = 0;
					int letterCount = 0;
					int finalLetterPosition = -1;
					for (int k = i; k < censorEnd; k++) {
						if (isDigit(message[k])) {
							digitCount++;
						} else if (isLetter(message[k])) {
							letterCount++;
							finalLetterPosition = k;
						}
					}

					if (finalLetterPosition > -1) {
						digitCount -= censorEnd - 1 - finalLetterPosition;
					}
					if (digitCount <= letterCount) {
						for (int i3 = i; i3 < censorEnd; i3++) {
							message[i3] = '*';
						}

					} else {
						j = 1;
					}
				}
			}
		}
	}

	private static void capitalise(char[] chars) {
		boolean flag = true;

		for (int index = 0; index < chars.length; index++) {
			char character = chars[index];

			if (isLetter(character)) {
				if (flag) {
					if (isLowerCase(character)) {
						flag = false;
					}
				} else if (isUpperCase(character)) {
					chars[index] = (char) (character + 97 - 65);
				}
			} else {
				flag = true;
			}
		}
	}

	private static void censorBad(char[] message) {
		for (int i = 0; i < 2; i++) {
			for (int word = badWords.length - 1; word >= 0; word--) {
				method509(badEncoding[word], message, badWords[word]);
			}
		}
	}

	private static void censorDomains(char[] chars) {
		char[] clone = chars.clone();
		char[] dot = { 'd', 'o', 't' };
		method509(null, clone, dot);
		char[] clone2 = chars.clone();
		char[] slash = { 's', 'l', 'a', 's', 'h' };
		method509(null, clone2, slash);

		for (int i = 0; i < tldList.length; i++) {
			method506(clone2, tldList[i], tlds[i], clone, chars);
		}
	}

	private static boolean containsFragment(char[] chars) {
		boolean onlyDigits = true;
		for (int index = 0; index < chars.length; index++) {
			if (!isDigit(chars[index]) && chars[index] != 0) {
				onlyDigits = false;
			}
		}

		if (onlyDigits) {
			return true;
		}

		int code = hash(chars);
		int index = 0;
		int length = fragments.length - 1;
		if (code == fragments[index] || code == fragments[length]) {
			return true;
		}

		do {
			int fragment = (index + length) / 2;

			if (code == fragments[fragment]) {
				return true;
			} else if (code < fragments[fragment]) {
				length = fragment;
			} else {
				index = fragment;
			}
		} while (index != length && index + 1 != length);

		return false;
	}

	private static void copyCase(char[] originalText, char[] censoredText) {
		for (int index = 0; index < originalText.length; index++) {
			if (censoredText[index] != '*' && isUpperCase(originalText[index])) {
				censoredText[index] = originalText[index];
			}
		}
	}

	private static void decode(Buffer fragments, Buffer badenc, Buffer domainenc, Buffer tldlist) {
		decodeBad(badenc);
		decodeDomain(domainenc);
		decodeFragments(fragments);
		decodeTlds(tldlist);
	}

	private static void decodeBad(Buffer buffer) {
		int length = buffer.readInt();
		badWords = new char[length][];
		badEncoding = new byte[length][][];
		initBad(buffer, badWords, badEncoding);
	}

	private static void decodeDomain(Buffer buffer) {
		int length = buffer.readInt();
		domains = new char[length][];
		initDomains(domains, buffer);
	}

	private static void decodeFragments(Buffer buffer) {
		fragments = new int[buffer.readInt()];
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = buffer.readUShort();
		}
	}

	private static void decodeTlds(Buffer buffer) {
		int length = buffer.readInt();
		tldList = new char[length][];
		tlds = new int[length];

		for (int i = 0; i < length; i++) {
			tlds[i] = buffer.readUByte();
			char[] tld = new char[buffer.readUByte()];
			for (int j = 0; j < tld.length; j++) {
				tld[j] = (char) buffer.readUByte();
			}

			tldList[i] = tld;
		}

	}

	private static byte getCensoringCharacterCode(char character) {
		if (character >= 'a' && character <= 'z') {
			return (byte) (character - 97 + 1);
		} else if (character == '\'') {
			return 28;
		} else if (character >= '0' && character <= '9') {
			return (byte) (character - 48 + 29);
		}

		return 27;
	}

	private static int indexOfDigit(char chars[], int offset) {
		for (int index = offset; index < chars.length && index >= 0; index++) {
			if (chars[index] >= '0' && chars[index] <= '9') {
				return index;
			}
		}

		return -1;
	}

	private static int indexOfNonDigit(char chars[], int offset) {
		for (int i = offset; i < chars.length && i >= 0; i++) {
			if (chars[i] < '0' || chars[i] > '9') {
				return i;
			}
		}
		return chars.length;
	}

	private static void initBad(Buffer buffer, char[][] words, byte[][][] complexBadEnc) {
		for (int index = 0; index < words.length; index++) {
			int length = buffer.readUByte();
			char[] word = new char[length];

			for (int character = 0; character < length; character++) {
				word[character] = (char) buffer.readUByte();
			}

			words[index] = word;

			byte[][] complex = new byte[buffer.readUByte()][2];
			for (int j = 0; j < complex.length; j++) {
				complex[j][0] = (byte) buffer.readUByte();
				complex[j][1] = (byte) buffer.readUByte();
			} // TODO 'complex'

			if (complex.length > 0) {
				complexBadEnc[index] = complex;
			}
		}
	}

	private static void initDomains(char[][] domains, Buffer buffer) {
		for (int index = 0; index < domains.length; index++) {
			char[] domain = new char[buffer.readUByte()];

			for (int i = 0; i < domain.length; i++) {
				domain[i] = (char) buffer.readUByte();
			}

			domains[index] = domain;
		}
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private static boolean isLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	private static boolean isLowerCase(char c) {
		return c >= 'a' && c <= 'z';
	}

	private static boolean isNotAlphanumeric(char c) {
		return !isLetter(c) && !isDigit(c);
	}

	private static boolean isUpperCase(char c) {
		return c >= 'A' && c <= 'Z';
	}

	private static boolean legalCharacter(char c) {
		return c >= ' ' && c <= '\177' || c == ' ' || c == '\n' || c == '\t' || c == '\243' || c == '\u20AC';
	}

	private static void method501(char ac[]) {
		char[] clone = ac.clone();
		char[] at = { '(', 'a', ')' };
		method509(null, clone, at);
		char[] clone2 = ac.clone();
		char[] dot = { 'd', 'o', 't' };
		method509(null, clone2, dot);
		for (int i = domains.length - 1; i >= 0; i--) {
			method502(ac, domains[i], clone2, clone);
		}
	}

	private static void method502(char ac[], char ac1[], char ac2[], char ac3[]) {
		if (ac1.length > ac.length) {
			return;
		}
		int j;
		for (int k = 0; k <= ac.length - ac1.length; k += j) {
			int l = k;
			int i1 = 0;
			j = 1;
			while (l < ac.length) {
				int j1 = 0;
				char c = ac[l];
				char c1 = '\0';
				if (l + 1 < ac.length) {
					c1 = ac[l + 1];
				}
				if (i1 < ac1.length && (j1 = method511(c, ac1[i1], c1)) > 0) {
					l += j1;
					i1++;
					continue;
				}
				if (i1 == 0) {
					break;
				}
				if ((j1 = method511(c, ac1[i1 - 1], c1)) > 0) {
					l += j1;
					if (i1 == 1) {
						j++;
					}
					continue;
				}
				if (i1 >= ac1.length || !isNotAlphanumeric(c)) {
					break;
				}
				l++;
			}
			if (i1 >= ac1.length) {
				boolean flag1 = false;
				int k1 = method503(ac, ac3, k);
				int l1 = method504(ac2, l - 1, ac);
				if (k1 > 2 || l1 > 2) {
					flag1 = true;
				}
				if (flag1) {
					for (int i2 = k; i2 < l; i2++) {
						ac[i2] = '*';
					}

				}
			}
		}

	}

	private static int method503(char ac[], char ac1[], int j) {
		if (j == 0) {
			return 2;
		}

		for (int k = j - 1; k >= 0; k--) {
			if (!isNotAlphanumeric(ac[k])) {
				break;
			}
			if (ac[k] == '@') {
				return 3;
			}
		}

		int l = 0;
		for (int i1 = j - 1; i1 >= 0; i1--) {
			if (!isNotAlphanumeric(ac1[i1])) {
				break;
			}
			if (ac1[i1] == '*') {
				l++;
			}
		}

		if (l >= 3) {
			return 4;
		}
		return !isNotAlphanumeric(ac[j - 1]) ? 0 : 1;
	}

	private static int method504(char ac[], int i, char ac1[]) {
		if (i + 1 == ac1.length) {
			return 2;
		}
		for (int j = i + 1; j < ac1.length; j++) {
			if (!isNotAlphanumeric(ac1[j])) {
				break;
			}
			if (ac1[j] == '.' || ac1[j] == ',') {
				return 3;
			}
		}

		int k = 0;
		for (int l = i + 1; l < ac1.length; l++) {
			if (!isNotAlphanumeric(ac[l])) {
				break;
			}
			if (ac[l] == '*') {
				k++;
			}
		}

		if (k >= 3) {
			return 4;
		}
		return !isNotAlphanumeric(ac1[i + 1]) ? 0 : 1;
	}

	private static void method506(char ac[], char ac1[], int i, char ac2[], char ac3[]) {
		if (ac1.length > ac3.length) {
			return;
		}
		int j;
		for (int k = 0; k <= ac3.length - ac1.length; k += j) {
			int l = k;
			int i1 = 0;
			j = 1;
			while (l < ac3.length) {
				int j1 = 0;
				char c = ac3[l];
				char c1 = '\0';
				if (l + 1 < ac3.length) {
					c1 = ac3[l + 1];
				}
				if (i1 < ac1.length && (j1 = method511(c, ac1[i1], c1)) > 0) {
					l += j1;
					i1++;
					continue;
				}
				if (i1 == 0) {
					break;
				}
				if ((j1 = method511(c, ac1[i1 - 1], c1)) > 0) {
					l += j1;
					if (i1 == 1) {
						j++;
					}
					continue;
				}
				if (i1 >= ac1.length || !isNotAlphanumeric(c)) {
					break;
				}
				l++;
			}
			if (i1 >= ac1.length) {
				boolean flag1 = false;
				int k1 = method507(ac3, k, ac2);
				int l1 = method508(ac3, ac, l - 1);
				if (i == 1 && k1 > 0 && l1 > 0) {
					flag1 = true;
				}
				if (i == 2 && (k1 > 2 && l1 > 0 || k1 > 0 && l1 > 2)) {
					flag1 = true;
				}
				if (i == 3 && k1 > 0 && l1 > 2) {
					flag1 = true;
				}
				if (flag1) {
					int i2 = k;
					int j2 = l - 1;
					if (k1 > 2) {
						if (k1 == 4) {
							boolean flag2 = false;
							for (int l2 = i2 - 1; l2 >= 0; l2--) {
								if (flag2) {
									if (ac2[l2] != '*') {
										break;
									}
									i2 = l2;
								} else if (ac2[l2] == '*') {
									i2 = l2;
									flag2 = true;
								}
							}

						}
						boolean flag3 = false;
						for (int i3 = i2 - 1; i3 >= 0; i3--) {
							if (flag3) {
								if (isNotAlphanumeric(ac3[i3])) {
									break;
								}
								i2 = i3;
							} else if (!isNotAlphanumeric(ac3[i3])) {
								flag3 = true;
								i2 = i3;
							}
						}

					}
					if (l1 > 2) {
						if (l1 == 4) {
							boolean flag4 = false;
							for (int j3 = j2 + 1; j3 < ac3.length; j3++) {
								if (flag4) {
									if (ac[j3] != '*') {
										break;
									}
									j2 = j3;
								} else if (ac[j3] == '*') {
									j2 = j3;
									flag4 = true;
								}
							}

						}
						boolean flag5 = false;
						for (int k3 = j2 + 1; k3 < ac3.length; k3++) {
							if (flag5) {
								if (isNotAlphanumeric(ac3[k3])) {
									break;
								}
								j2 = k3;
							} else if (!isNotAlphanumeric(ac3[k3])) {
								flag5 = true;
								j2 = k3;
							}
						}

					}
					for (int k2 = i2; k2 <= j2; k2++) {
						ac3[k2] = '*';
					}

				}
			}
		}
	}

	private static int method507(char ac[], int j, char ac1[]) {
		if (j == 0) {
			return 2;
		}
		for (int k = j - 1; k >= 0; k--) {
			if (!isNotAlphanumeric(ac[k])) {
				break;
			}
			if (ac[k] == ',' || ac[k] == '.') {
				return 3;
			}
		}

		int l = 0;
		for (int i1 = j - 1; i1 >= 0; i1--) {
			if (!isNotAlphanumeric(ac1[i1])) {
				break;
			}
			if (ac1[i1] == '*') {
				l++;
			}
		}

		if (l >= 3) {
			return 4;
		}
		return !isNotAlphanumeric(ac[j - 1]) ? 0 : 1;
	}

	private static int method508(char ac[], char ac1[], int i) {
		if (i + 1 == ac.length) {
			return 2;
		}
		for (int j = i + 1; j < ac.length; j++) {
			if (!isNotAlphanumeric(ac[j])) {
				break;
			}
			if (ac[j] == '\\' || ac[j] == '/') {
				return 3;
			}
		}

		int k = 0;
		for (int l = i + 1; l < ac.length; l++) {
			if (!isNotAlphanumeric(ac1[l])) {
				break;
			}
			if (ac1[l] == '*') {
				k++;
			}
		}

		if (k >= 5) {
			return 4;
		}
		return !isNotAlphanumeric(ac[i + 1]) ? 0 : 1;
	}

	private static boolean method510(byte byte0, byte abyte0[][], byte byte2) {
		int i = 0;
		if (abyte0[i][0] == byte0 && abyte0[i][1] == byte2) {
			return true;
		}
		int j = abyte0.length - 1;
		if (abyte0[j][0] == byte0 && abyte0[j][1] == byte2) {
			return true;
		}
		do {
			int k = (i + j) / 2;
			if (abyte0[k][0] == byte0 && abyte0[k][1] == byte2) {
				return true;
			}
			if (byte0 < abyte0[k][0] || byte0 == abyte0[k][0] && byte2 < abyte0[k][1]) {
				j = k;
			} else {
				i = k;
			}
		} while (i != j && i + 1 != j);
		return false;
	}

	private static int method511(char c, char c1, char c2) {
		if (c1 == c) {
			return 1;
		}
		if (c1 == 'o' && c == '0') {
			return 1;
		}
		if (c1 == 'o' && c == '(' && c2 == ')') {
			return 2;
		}
		if (c1 == 'c' && (c == '(' || c == '<' || c == '[')) {
			return 1;
		}
		if (c1 == 'e' && c == '\u20AC') {
			return 1;
		}
		if (c1 == 's' && c == '$') {
			return 1;
		}
		return c1 != 'l' || c != 'i' ? 0 : 1;
	}

	private static int method512(char c, char c1, char c2) {
		if (c2 == c1) {
			return 1;
		}
		if (c2 >= 'a' && c2 <= 'm') {
			if (c2 == 'a') {
				if (c1 == '4' || c1 == '@' || c1 == '^') {
					return 1;
				}
				return c1 != '/' || c != '\\' ? 0 : 2;
			}
			if (c2 == 'b') {
				if (c1 == '6' || c1 == '8') {
					return 1;
				}
				return (c1 != '1' || c != '3') && (c1 != 'i' || c != '3') ? 0 : 2;
			}
			if (c2 == 'c') {
				return c1 != '(' && c1 != '<' && c1 != '{' && c1 != '[' ? 0 : 1;
			}
			if (c2 == 'd') {
				return (c1 != '[' || c != ')') && (c1 != 'i' || c != ')') ? 0 : 2;
			}
			if (c2 == 'e') {
				return c1 != '3' && c1 != '\u20AC' ? 0 : 1;
			}
			if (c2 == 'f') {
				if (c1 == 'p' && c == 'h') {
					return 2;
				}
				return c1 != '\243' ? 0 : 1;
			}
			if (c2 == 'g') {
				return c1 != '9' && c1 != '6' && c1 != 'q' ? 0 : 1;
			}
			if (c2 == 'h') {
				return c1 != '#' ? 0 : 1;
			}
			if (c2 == 'i') {
				return c1 != 'y' && c1 != 'l' && c1 != 'j' && c1 != '1' && c1 != '!' && c1 != ':' && c1 != ';' && c1 != '|' ? 0
						: 1;
			}
			if (c2 == 'j') {
				return 0;
			}
			if (c2 == 'k') {
				return 0;
			}
			if (c2 == 'l') {
				return c1 != '1' && c1 != '|' && c1 != 'i' ? 0 : 1;
			}
			if (c2 == 'm') {
				return 0;
			}
		}
		if (c2 >= 'n' && c2 <= 'z') {
			if (c2 == 'n') {
				return 0;
			}
			if (c2 == 'o') {
				if (c1 == '0' || c1 == '*') {
					return 1;
				}
				return (c1 != '(' || c != ')') && (c1 != '[' || c != ']') && (c1 != '{' || c != '}') && (c1 != '<' || c != '>') ? 0
						: 2;
			}
			if (c2 == 'p') {
				return 0;
			}
			if (c2 == 'q') {
				return 0;
			}
			if (c2 == 'r') {
				return 0;
			}
			if (c2 == 's') {
				return c1 != '5' && c1 != 'z' && c1 != '$' && c1 != '2' ? 0 : 1;
			}
			if (c2 == 't') {
				return c1 != '7' && c1 != '+' ? 0 : 1;
			}
			if (c2 == 'u') {
				if (c1 == 'v') {
					return 1;
				}
				return (c1 != '\\' || c != '/') && (c1 != '\\' || c != '|') && (c1 != '|' || c != '/') ? 0 : 2;
			}
			if (c2 == 'v') {
				return (c1 != '\\' || c != '/') && (c1 != '\\' || c != '|') && (c1 != '|' || c != '/') ? 0 : 2;
			}
			if (c2 == 'w') {
				return c1 != 'v' || c != 'v' ? 0 : 2;
			}
			if (c2 == 'x') {
				return (c1 != ')' || c != '(') && (c1 != '}' || c != '{') && (c1 != ']' || c != '[') && (c1 != '>' || c != '<') ? 0
						: 2;
			}
			if (c2 == 'y') {
				return 0;
			}
			if (c2 == 'z') {
				return 0;
			}
		}
		if (c2 >= '0' && c2 <= '9') {
			if (c2 == '0') {
				if (c1 == 'o' || c1 == 'O') {
					return 1;
				}
				return (c1 != '(' || c != ')') && (c1 != '{' || c != '}') && (c1 != '[' || c != ']') ? 0 : 2;
			}
			if (c2 == '1') {
				return c1 != 'l' ? 0 : 1;
			}
			return 0;
		}
		if (c2 == ',') {
			return c1 != '.' ? 0 : 1;
		}
		if (c2 == '.') {
			return c1 != ',' ? 0 : 1;
		}
		if (c2 == '!') {
			return c1 != 'i' ? 0 : 1;
		}
		return 0;
	}

	private static void method514(char chars[]) {
		int j = 0;
		int offset = 0;
		int l = 0;
		int i1 = 0;
		while ((j = indexOfDigit(chars, offset)) != -1) {
			boolean flag = false;
			for (int j1 = offset; j1 >= 0 && j1 < j && !flag; j1++) {
				if (!isNotAlphanumeric(chars[j1]) && !method518(chars[j1])) {
					flag = true;
				}
			}

			if (flag) {
				l = 0;
			}
			if (l == 0) {
				i1 = j;
			}
			offset = indexOfNonDigit(chars, j);
			int k1 = 0;
			for (int l1 = j; l1 < offset; l1++) {
				k1 = k1 * 10 + chars[l1] - 48;
			}

			if (k1 > 255 || offset - j > 8) {
				l = 0;
			} else {
				l++;
			}
			if (l == 4) {
				for (int i2 = i1; i2 < offset; i2++) {
					chars[i2] = '*';
				}

				l = 0;
			}
		}
	}

	private static boolean method518(char c) {
		if (c < 'a' || c > 'z') {
			return true;
		}

		return c == 'v' || c == 'x' || c == 'j' || c == 'q' || c == 'z';
	}

	private static void stripIllegalCharacters(char chars[]) {
		int index = 0;
		for (int i = 0; i < chars.length; i++) {
			if (legalCharacter(chars[i])) {
				chars[index] = chars[i];
			} else {
				chars[index] = ' ';
			}
			if (index == 0 || chars[index] != ' ' || chars[index - 1] != ' ') {
				index++;
			}
		}

		for (int i = index; i < chars.length; i++) {
			chars[i] = ' ';
		}
	}

}