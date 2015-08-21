package com.jagex.draw;

import com.jagex.cache.Archive;
import com.jagex.cache.graphics.IndexedImage;

public class Rasterizer extends Raster {

	public static boolean aBoolean1462;
	public static int currentAlpha;
	public static int anInt1481;
	public static int[] anIntArray1468;
	public static int[] anIntArray1469;
	public static int[] anIntArray1480 = new int[50];
	public static int[] anIntArray1482 = new int[0x10000];
	public static boolean approximateAlphaBlending = true;
	public static int[] COSINE;
	public static IndexedImage[] textures = new IndexedImage[50];
	public static boolean lowMemory = true;
	public static int originViewX;
	public static int originViewY;
	public static int[] scanOffsets;
	public static int[] SINE;
	static boolean aBoolean1463;
	static boolean[] aBooleanArray1475 = new boolean[50];
	static int anInt1477;
	static int[] anIntArray1476 = new int[50];
	static int[][] anIntArrayArray1478;
	static int[][] anIntArrayArray1479 = new int[50][];
	static int[][] texturePalettes = new int[50][]; // [texture][floor_raster[i]]
	static int textureCount;

	static {
		anIntArray1468 = new int[512];
		anIntArray1469 = new int[2048];
		SINE = new int[2048];
		COSINE = new int[2048];
		for (int i = 1; i < 512; i++) {
			anIntArray1468[i] = 32768 / i;
		}

		for (int j = 1; j < 2048; j++) {
			anIntArray1469[j] = 0x10000 / j;
		}

		for (int theta = 0; theta < 2048; theta++) {
			SINE[theta] = (int) (65536D * Math.sin(theta * 0.0030679614999999999D));
			COSINE[theta] = (int) (65536D * Math.cos(theta * 0.0030679614999999999D));
		}
	}

	public static void dispose() {
		anIntArray1468 = null;
		SINE = null;
		COSINE = null;
		scanOffsets = null;
		textures = null;
		aBooleanArray1475 = null;
		anIntArray1476 = null;
		anIntArrayArray1478 = null;
		anIntArrayArray1479 = null;
		anIntArray1480 = null;
		anIntArray1482 = null;
		texturePalettes = null;
	}

	public static void loadFloorImages(Archive archive) {
		textureCount = 0;
		for (int id = 0; id < 50; id++) {
			try {
				textures[id] = new IndexedImage(archive, String.valueOf(id), 0);

				if (lowMemory && textures[id].getResizeWidth() == 128) {
					textures[id].downscale();
				} else {
					textures[id].resize();
				}

				textureCount++;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void method366() {
		anIntArrayArray1478 = null;
		for (int j = 0; j < 50; j++) {
			anIntArrayArray1479[j] = null;
		}
	}

	public static void method367(int i) {
		if (anIntArrayArray1478 == null) {
			anInt1477 = i;

			anIntArrayArray1478 = lowMemory ? new int[anInt1477][16384] : new int[anInt1477][0x10000];

			for (int k = 0; k < 50; k++) {
				anIntArrayArray1479[k] = null;
			}
		}
	}

	public static int method369(int texture) {
		if (anIntArray1476[texture] != 0) {
			return anIntArray1476[texture];
		}

		int r = 0;
		int g = 0;
		int b = 0;
		int count = texturePalettes[texture].length;

		for (int i = 0; i < count; i++) {
			r += texturePalettes[texture][i] >> 16 & 0xff;
			g += texturePalettes[texture][i] >> 8 & 0xff;
			b += texturePalettes[texture][i] & 0xff;
		}

		int l1 = (r / count << 16) + (g / count << 8) + b / count;
		l1 = method373(l1, 1.4D);
		if (l1 == 0) {
			l1 = 1;
		}

		anIntArray1476[texture] = l1;
		return l1;
	}

	public static void method370(int i) {
		if (anIntArrayArray1479[i] == null) {
			return;
		}

		anIntArrayArray1478[anInt1477++] = anIntArrayArray1479[i];
		anIntArrayArray1479[i] = null;
	}

	public static int[] method371(int id) {
		anIntArray1480[id] = anInt1481++;
		if (anIntArrayArray1479[id] != null) {
			return anIntArrayArray1479[id];
		}

		int ai[];
		if (anInt1477 > 0) {
			ai = anIntArrayArray1478[--anInt1477];
			anIntArrayArray1478[anInt1477] = null;
		} else {
			int j = 0;
			int k = -1;
			for (int l = 0; l < textureCount; l++) {
				if (anIntArrayArray1479[l] != null && (anIntArray1480[l] < j || k == -1)) {
					j = anIntArray1480[l];
					k = l;
				}
			}

			ai = anIntArrayArray1479[k];
			anIntArrayArray1479[k] = null;
		}

		anIntArrayArray1479[id] = ai;
		IndexedImage image = textures[id];
		int palette[] = texturePalettes[id];

		if (lowMemory) {
			aBooleanArray1475[id] = false;
			for (int i1 = 0; i1 < 4096; i1++) {
				int i2 = ai[i1] = palette[image.getRaster()[i1]] & 0xf8f8ff;
				if (i2 == 0) {
					aBooleanArray1475[id] = true;
				}

				ai[4096 + i1] = i2 - (i2 >>> 3) & 0xf8f8ff;
				ai[8192 + i1] = i2 - (i2 >>> 2) & 0xf8f8ff;
				ai[12288 + i1] = i2 - (i2 >>> 2) - (i2 >>> 3) & 0xf8f8ff;
			}
		} else {
			if (image.getWidth() == 64) {
				for (int j1 = 0; j1 < 128; j1++) {
					for (int j2 = 0; j2 < 128; j2++) {
						ai[j2 + (j1 << 7)] = palette[image.getRaster()[(j2 >> 1) + (j1 >> 1 << 6)]];
					}
				}
			} else {
				for (int k1 = 0; k1 < 16384; k1++) {
					ai[k1] = palette[image.getRaster()[k1]];
				}
			}

			aBooleanArray1475[id] = false;
			for (int l1 = 0; l1 < 16384; l1++) {
				ai[l1] &= 0xf8f8ff;
				int k2 = ai[l1];
				if (k2 == 0) {
					aBooleanArray1475[id] = true;
				}

				ai[16384 + l1] = k2 - (k2 >>> 3) & 0xf8f8ff;
				ai[32768 + l1] = k2 - (k2 >>> 2) & 0xf8f8ff;
				ai[49152 + l1] = k2 - (k2 >>> 2) - (k2 >>> 3) & 0xf8f8ff;
			}

		}
		return ai;
	}

	public static void method372(double exponent) {
		exponent += Math.random() * 0.03 - 0.015;
		int j = 0;

		for (int k = 0; k < 512; k++) {
			double d1 = (k / 8) / 64D + 0.0078125D;
			double d2 = (k & 7) / 8D + 0.0625D;

			for (int k1 = 0; k1 < 128; k1++) {
				double initial = k1 / 128D;
				double r = initial;
				double g = initial;
				double b = initial;

				if (d2 != 0.0D) {
					double d7;
					if (initial < 0.5D) {
						d7 = initial * (1.0D + d2);
					} else {
						d7 = initial + d2 - initial * d2;
					}

					double d8 = 2D * initial - d7;
					double d9 = d1 + 0.33333333333333331D;
					if (d9 > 1.0D) {
						d9--;
					}

					double d10 = d1;
					double d11 = d1 - 0.33333333333333331D;
					if (d11 < 0.0D) {
						d11++;
					}

					if (6D * d9 < 1.0D) {
						r = d8 + (d7 - d8) * 6D * d9;
					} else if (2D * d9 < 1.0D) {
						r = d7;
					} else if (3D * d9 < 2D) {
						r = d8 + (d7 - d8) * (0.66666666666666663D - d9) * 6D;
					} else {
						r = d8;
					}

					if (6D * d10 < 1.0D) {
						g = d8 + (d7 - d8) * 6D * d10;
					} else if (2D * d10 < 1.0D) {
						g = d7;
					} else if (3D * d10 < 2D) {
						g = d8 + (d7 - d8) * (0.66666666666666663D - d10) * 6D;
					} else {
						g = d8;
					}

					if (6D * d11 < 1.0D) {
						b = d8 + (d7 - d8) * 6D * d11;
					} else if (2D * d11 < 1.0D) {
						b = d7;
					} else if (3D * d11 < 2D) {
						b = d8 + (d7 - d8) * (0.66666666666666663D - d11) * 6D;
					} else {
						b = d8;
					}
				}
				int newR = (int) (r * 256D);
				int newG = (int) (g * 256D);
				int newB = (int) (b * 256D);
				int colour = (newR << 16) + (newG << 8) + newB;

				colour = method373(colour, exponent);
				if (colour == 0) {
					colour = 1;
				}

				anIntArray1482[j++] = colour;
			}
		}

		for (int id = 0; id < 50; id++) {
			if (textures[id] != null) {
				int[] palette = textures[id].getPalette();
				texturePalettes[id] = new int[palette.length];

				for (int index = 0; index < palette.length; index++) {
					texturePalettes[id][index] = method373(palette[index], exponent);

					if ((texturePalettes[id][index] & 0xf8f8ff) == 0 && index != 0) {
						texturePalettes[id][index] = 1;
					}
				}
			}
		}

		for (int i1 = 0; i1 < 50; i1++) {
			method370(i1);
		}
	}

	public static int method373(int colour, double exponent) {
		double r = (colour >> 16) / 256D;
		double g = (colour >> 8 & 0xff) / 256D;
		double b = (colour & 0xff) / 256D;

		r = Math.pow(r, exponent);
		g = Math.pow(g, exponent);
		b = Math.pow(b, exponent);

		int newR = (int) (r * 256D);
		int newG = (int) (g * 256D);
		int newB = (int) (b * 256D);
		return (newR << 16) + (newG << 8) + newB;
	}

	public static void method374(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
		int j2 = 0;
		int k2 = 0;
		if (j != i) {
			j2 = (i1 - l << 16) / (j - i);
			k2 = (l1 - k1 << 15) / (j - i);
		}

		int l2 = 0;
		int i3 = 0;
		if (k != j) {
			l2 = (j1 - i1 << 16) / (k - j);
			i3 = (i2 - l1 << 15) / (k - j);
		}

		int j3 = 0;
		int k3 = 0;
		if (k != i) {
			j3 = (l - j1 << 16) / (i - k);
			k3 = (k1 - i2 << 15) / (i - k);
		}

		if (i <= j && i <= k) {
			if (i >= Raster.getClipTop()) {
				return;
			}
			if (j > Raster.getClipTop()) {
				j = Raster.getClipTop();
			}
			if (k > Raster.getClipTop()) {
				k = Raster.getClipTop();
			}
			if (j < k) {
				j1 = l <<= 16;
				i2 = k1 <<= 15;
				if (i < 0) {
					j1 -= j3 * i;
					l -= j2 * i;
					i2 -= k3 * i;
					k1 -= k2 * i;
					i = 0;
				}
				i1 <<= 16;
				l1 <<= 15;
				if (j < 0) {
					i1 -= l2 * j;
					l1 -= i3 * j;
					j = 0;
				}
				if (i != j && j3 < j2 || i == j && j3 > l2) {
					k -= j;
					j -= i;
					for (i = scanOffsets[i]; --j >= 0; i += Raster.width) {
						method375(Raster.raster, i, 0, 0, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
						j1 += j3;
						l += j2;
						i2 += k3;
						k1 += k2;
					}

					while (--k >= 0) {
						method375(Raster.raster, i, 0, 0, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
						j1 += j3;
						i1 += l2;
						i2 += k3;
						l1 += i3;
						i += Raster.width;
					}
					return;
				}
				k -= j;
				j -= i;
				for (i = scanOffsets[i]; --j >= 0; i += Raster.width) {
					method375(Raster.raster, i, 0, 0, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
					j1 += j3;
					l += j2;
					i2 += k3;
					k1 += k2;
				}

				while (--k >= 0) {
					method375(Raster.raster, i, 0, 0, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
					j1 += j3;
					i1 += l2;
					i2 += k3;
					l1 += i3;
					i += Raster.width;
				}
				return;
			}
			i1 = l <<= 16;
			l1 = k1 <<= 15;
			if (i < 0) {
				i1 -= j3 * i;
				l -= j2 * i;
				l1 -= k3 * i;
				k1 -= k2 * i;
				i = 0;
			}
			j1 <<= 16;
			i2 <<= 15;
			if (k < 0) {
				j1 -= l2 * k;
				i2 -= i3 * k;
				k = 0;
			}
			if (i != k && j3 < j2 || i == k && l2 > j2) {
				j -= k;
				k -= i;
				for (i = scanOffsets[i]; --k >= 0; i += Raster.width) {
					method375(Raster.raster, i, 0, 0, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
					i1 += j3;
					l += j2;
					l1 += k3;
					k1 += k2;
				}

				while (--j >= 0) {
					method375(Raster.raster, i, 0, 0, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
					j1 += l2;
					l += j2;
					i2 += i3;
					k1 += k2;
					i += Raster.width;
				}
				return;
			}
			j -= k;
			k -= i;
			for (i = scanOffsets[i]; --k >= 0; i += Raster.width) {
				method375(Raster.raster, i, 0, 0, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
				i1 += j3;
				l += j2;
				l1 += k3;
				k1 += k2;
			}

			while (--j >= 0) {
				method375(Raster.raster, i, 0, 0, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
				j1 += l2;
				l += j2;
				i2 += i3;
				k1 += k2;
				i += Raster.width;
			}
			return;
		}
		if (j <= k) {
			if (j >= Raster.getClipTop()) {
				return;
			}
			if (k > Raster.getClipTop()) {
				k = Raster.getClipTop();
			}
			if (i > Raster.getClipTop()) {
				i = Raster.getClipTop();
			}
			if (k < i) {
				l = i1 <<= 16;
				k1 = l1 <<= 15;
				if (j < 0) {
					l -= j2 * j;
					i1 -= l2 * j;
					k1 -= k2 * j;
					l1 -= i3 * j;
					j = 0;
				}
				j1 <<= 16;
				i2 <<= 15;
				if (k < 0) {
					j1 -= j3 * k;
					i2 -= k3 * k;
					k = 0;
				}
				if (j != k && j2 < l2 || j == k && j2 > j3) {
					i -= k;
					k -= j;
					for (j = scanOffsets[j]; --k >= 0; j += Raster.width) {
						method375(Raster.raster, j, 0, 0, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
						l += j2;
						i1 += l2;
						k1 += k2;
						l1 += i3;
					}

					while (--i >= 0) {
						method375(Raster.raster, j, 0, 0, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
						l += j2;
						j1 += j3;
						k1 += k2;
						i2 += k3;
						j += Raster.width;
					}
					return;
				}
				i -= k;
				k -= j;
				for (j = scanOffsets[j]; --k >= 0; j += Raster.width) {
					method375(Raster.raster, j, 0, 0, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
					l += j2;
					i1 += l2;
					k1 += k2;
					l1 += i3;
				}

				while (--i >= 0) {
					method375(Raster.raster, j, 0, 0, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
					l += j2;
					j1 += j3;
					k1 += k2;
					i2 += k3;
					j += Raster.width;
				}
				return;
			}
			j1 = i1 <<= 16;
			i2 = l1 <<= 15;
			if (j < 0) {
				j1 -= j2 * j;
				i1 -= l2 * j;
				i2 -= k2 * j;
				l1 -= i3 * j;
				j = 0;
			}
			l <<= 16;
			k1 <<= 15;
			if (i < 0) {
				l -= j3 * i;
				k1 -= k3 * i;
				i = 0;
			}
			if (j2 < l2) {
				k -= i;
				i -= j;
				for (j = scanOffsets[j]; --i >= 0; j += Raster.width) {
					method375(Raster.raster, j, 0, 0, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
					j1 += j2;
					i1 += l2;
					i2 += k2;
					l1 += i3;
				}

				while (--k >= 0) {
					method375(Raster.raster, j, 0, 0, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
					l += j3;
					i1 += l2;
					k1 += k3;
					l1 += i3;
					j += Raster.width;
				}
				return;
			}
			k -= i;
			i -= j;
			for (j = scanOffsets[j]; --i >= 0; j += Raster.width) {
				method375(Raster.raster, j, 0, 0, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
				j1 += j2;
				i1 += l2;
				i2 += k2;
				l1 += i3;
			}

			while (--k >= 0) {
				method375(Raster.raster, j, 0, 0, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
				l += j3;
				i1 += l2;
				k1 += k3;
				l1 += i3;
				j += Raster.width;
			}
			return;
		}
		if (k >= Raster.getClipTop()) {
			return;
		}
		if (i > Raster.getClipTop()) {
			i = Raster.getClipTop();
		}
		if (j > Raster.getClipTop()) {
			j = Raster.getClipTop();
		}
		if (i < j) {
			i1 = j1 <<= 16;
			l1 = i2 <<= 15;
			if (k < 0) {
				i1 -= l2 * k;
				j1 -= j3 * k;
				l1 -= i3 * k;
				i2 -= k3 * k;
				k = 0;
			}
			l <<= 16;
			k1 <<= 15;
			if (i < 0) {
				l -= j2 * i;
				k1 -= k2 * i;
				i = 0;
			}
			if (l2 < j3) {
				j -= i;
				i -= k;
				for (k = scanOffsets[k]; --i >= 0; k += Raster.width) {
					method375(Raster.raster, k, 0, 0, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
					i1 += l2;
					j1 += j3;
					l1 += i3;
					i2 += k3;
				}

				while (--j >= 0) {
					method375(Raster.raster, k, 0, 0, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
					i1 += l2;
					l += j2;
					l1 += i3;
					k1 += k2;
					k += Raster.width;
				}
				return;
			}
			j -= i;
			i -= k;
			for (k = scanOffsets[k]; --i >= 0; k += Raster.width) {
				method375(Raster.raster, k, 0, 0, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
				i1 += l2;
				j1 += j3;
				l1 += i3;
				i2 += k3;
			}

			while (--j >= 0) {
				method375(Raster.raster, k, 0, 0, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
				i1 += l2;
				l += j2;
				l1 += i3;
				k1 += k2;
				k += Raster.width;
			}
			return;
		}
		l = j1 <<= 16;
		k1 = i2 <<= 15;
		if (k < 0) {
			l -= l2 * k;
			j1 -= j3 * k;
			k1 -= i3 * k;
			i2 -= k3 * k;
			k = 0;
		}
		i1 <<= 16;
		l1 <<= 15;
		if (j < 0) {
			i1 -= j2 * j;
			l1 -= k2 * j;
			j = 0;
		}
		if (l2 < j3) {
			i -= j;
			j -= k;
			for (k = scanOffsets[k]; --j >= 0; k += Raster.width) {
				method375(Raster.raster, k, 0, 0, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
				l += l2;
				j1 += j3;
				k1 += i3;
				i2 += k3;
			}

			while (--i >= 0) {
				method375(Raster.raster, k, 0, 0, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
				i1 += j2;
				j1 += j3;
				l1 += k2;
				i2 += k3;
				k += Raster.width;
			}
			return;
		}
		i -= j;
		j -= k;
		for (k = scanOffsets[k]; --j >= 0; k += Raster.width) {
			method375(Raster.raster, k, 0, 0, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
			l += l2;
			j1 += j3;
			k1 += i3;
			i2 += k3;
		}

		while (--i >= 0) {
			method375(Raster.raster, k, 0, 0, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
			i1 += j2;
			j1 += j3;
			l1 += k2;
			i2 += k3;
			k += Raster.width;
		}
	}

	public static void method375(int ai[], int i, int j, int k, int l, int i1, int j1, int k1) {
		if (approximateAlphaBlending) {
			int l1;
			if (aBoolean1462) {
				if (i1 - l > 3) {
					l1 = (k1 - j1) / (i1 - l);
				} else {
					l1 = 0;
				}
				if (i1 > Raster.anInt1385) {
					i1 = Raster.anInt1385;
				}
				if (l < 0) {
					j1 -= l * l1;
					l = 0;
				}
				if (l >= i1) {
					return;
				}
				i += l;
				k = i1 - l >> 2;
				l1 <<= 2;
			} else {
				if (l >= i1) {
					return;
				}
				i += l;
				k = i1 - l >> 2;
				if (k > 0) {
					l1 = (k1 - j1) * anIntArray1468[k] >> 15;
				} else {
					l1 = 0;
				}
			}
			if (currentAlpha == 0) {
				while (--k >= 0) {
					j = anIntArray1482[j1 >> 8];
					j1 += l1;
					ai[i++] = j;
					ai[i++] = j;
					ai[i++] = j;
					ai[i++] = j;
				}
				k = i1 - l & 3;
				if (k > 0) {
					j = anIntArray1482[j1 >> 8];
					do {
						ai[i++] = j;
					} while (--k > 0);
					return;
				}
			} else {
				int j2 = currentAlpha;
				int l2 = 256 - currentAlpha;
				while (--k >= 0) {
					j = anIntArray1482[j1 >> 8];
					j1 += l1;
					j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
				}
				k = i1 - l & 3;
				if (k > 0) {
					j = anIntArray1482[j1 >> 8];
					j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
					do {
						ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					} while (--k > 0);
				}
			}
			return;
		}
		if (l >= i1) {
			return;
		}
		int i2 = (k1 - j1) / (i1 - l);
		if (aBoolean1462) {
			if (i1 > Raster.anInt1385) {
				i1 = Raster.anInt1385;
			}
			if (l < 0) {
				j1 -= l * i2;
				l = 0;
			}
			if (l >= i1) {
				return;
			}
		}
		i += l;
		k = i1 - l;
		if (currentAlpha == 0) {
			do {
				ai[i++] = anIntArray1482[j1 >> 8];
				j1 += i2;
			} while (--k > 0);
			return;
		}
		int k2 = currentAlpha;
		int i3 = 256 - currentAlpha;
		do {
			j = anIntArray1482[j1 >> 8];
			j1 += i2;
			j = ((j & 0xff00ff) * i3 >> 8 & 0xff00ff) + ((j & 0xff00) * i3 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * k2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * k2 >> 8 & 0xff00);
		} while (--k > 0);
	}

	public static void method376(int i, int j, int k, int l, int i1, int j1, int k1) {
		int l1 = 0;
		if (j != i) {
			l1 = (i1 - l << 16) / (j - i);
		}
		int i2 = 0;
		if (k != j) {
			i2 = (j1 - i1 << 16) / (k - j);
		}
		int j2 = 0;
		if (k != i) {
			j2 = (l - j1 << 16) / (i - k);
		}
		if (i <= j && i <= k) {
			if (i >= Raster.getClipTop()) {
				return;
			}
			if (j > Raster.getClipTop()) {
				j = Raster.getClipTop();
			}
			if (k > Raster.getClipTop()) {
				k = Raster.getClipTop();
			}
			if (j < k) {
				j1 = l <<= 16;
				if (i < 0) {
					j1 -= j2 * i;
					l -= l1 * i;
					i = 0;
				}
				i1 <<= 16;
				if (j < 0) {
					i1 -= i2 * j;
					j = 0;
				}
				if (i != j && j2 < l1 || i == j && j2 > i2) {
					k -= j;
					j -= i;
					for (i = scanOffsets[i]; --j >= 0; i += Raster.width) {
						method377(Raster.raster, i, k1, 0, j1 >> 16, l >> 16);
						j1 += j2;
						l += l1;
					}

					while (--k >= 0) {
						method377(Raster.raster, i, k1, 0, j1 >> 16, i1 >> 16);
						j1 += j2;
						i1 += i2;
						i += Raster.width;
					}
					return;
				}
				k -= j;
				j -= i;
				for (i = scanOffsets[i]; --j >= 0; i += Raster.width) {
					method377(Raster.raster, i, k1, 0, l >> 16, j1 >> 16);
					j1 += j2;
					l += l1;
				}

				while (--k >= 0) {
					method377(Raster.raster, i, k1, 0, i1 >> 16, j1 >> 16);
					j1 += j2;
					i1 += i2;
					i += Raster.width;
				}
				return;
			}
			i1 = l <<= 16;
			if (i < 0) {
				i1 -= j2 * i;
				l -= l1 * i;
				i = 0;
			}
			j1 <<= 16;
			if (k < 0) {
				j1 -= i2 * k;
				k = 0;
			}
			if (i != k && j2 < l1 || i == k && i2 > l1) {
				j -= k;
				k -= i;
				for (i = scanOffsets[i]; --k >= 0; i += Raster.width) {
					method377(Raster.raster, i, k1, 0, i1 >> 16, l >> 16);
					i1 += j2;
					l += l1;
				}

				while (--j >= 0) {
					method377(Raster.raster, i, k1, 0, j1 >> 16, l >> 16);
					j1 += i2;
					l += l1;
					i += Raster.width;
				}
				return;
			}
			j -= k;
			k -= i;
			for (i = scanOffsets[i]; --k >= 0; i += Raster.width) {
				method377(Raster.raster, i, k1, 0, l >> 16, i1 >> 16);
				i1 += j2;
				l += l1;
			}

			while (--j >= 0) {
				method377(Raster.raster, i, k1, 0, l >> 16, j1 >> 16);
				j1 += i2;
				l += l1;
				i += Raster.width;
			}
			return;
		}
		if (j <= k) {
			if (j >= Raster.getClipTop()) {
				return;
			}
			if (k > Raster.getClipTop()) {
				k = Raster.getClipTop();
			}
			if (i > Raster.getClipTop()) {
				i = Raster.getClipTop();
			}
			if (k < i) {
				l = i1 <<= 16;
				if (j < 0) {
					l -= l1 * j;
					i1 -= i2 * j;
					j = 0;
				}
				j1 <<= 16;
				if (k < 0) {
					j1 -= j2 * k;
					k = 0;
				}
				if (j != k && l1 < i2 || j == k && l1 > j2) {
					i -= k;
					k -= j;
					for (j = scanOffsets[j]; --k >= 0; j += Raster.width) {
						method377(Raster.raster, j, k1, 0, l >> 16, i1 >> 16);
						l += l1;
						i1 += i2;
					}

					while (--i >= 0) {
						method377(Raster.raster, j, k1, 0, l >> 16, j1 >> 16);
						l += l1;
						j1 += j2;
						j += Raster.width;
					}
					return;
				}
				i -= k;
				k -= j;
				for (j = scanOffsets[j]; --k >= 0; j += Raster.width) {
					method377(Raster.raster, j, k1, 0, i1 >> 16, l >> 16);
					l += l1;
					i1 += i2;
				}

				while (--i >= 0) {
					method377(Raster.raster, j, k1, 0, j1 >> 16, l >> 16);
					l += l1;
					j1 += j2;
					j += Raster.width;
				}
				return;
			}
			j1 = i1 <<= 16;
			if (j < 0) {
				j1 -= l1 * j;
				i1 -= i2 * j;
				j = 0;
			}
			l <<= 16;
			if (i < 0) {
				l -= j2 * i;
				i = 0;
			}
			if (l1 < i2) {
				k -= i;
				i -= j;
				for (j = scanOffsets[j]; --i >= 0; j += Raster.width) {
					method377(Raster.raster, j, k1, 0, j1 >> 16, i1 >> 16);
					j1 += l1;
					i1 += i2;
				}

				while (--k >= 0) {
					method377(Raster.raster, j, k1, 0, l >> 16, i1 >> 16);
					l += j2;
					i1 += i2;
					j += Raster.width;
				}
				return;
			}
			k -= i;
			i -= j;
			for (j = scanOffsets[j]; --i >= 0; j += Raster.width) {
				method377(Raster.raster, j, k1, 0, i1 >> 16, j1 >> 16);
				j1 += l1;
				i1 += i2;
			}

			while (--k >= 0) {
				method377(Raster.raster, j, k1, 0, i1 >> 16, l >> 16);
				l += j2;
				i1 += i2;
				j += Raster.width;
			}
			return;
		}
		if (k >= Raster.getClipTop()) {
			return;
		}
		if (i > Raster.getClipTop()) {
			i = Raster.getClipTop();
		}
		if (j > Raster.getClipTop()) {
			j = Raster.getClipTop();
		}
		if (i < j) {
			i1 = j1 <<= 16;
			if (k < 0) {
				i1 -= i2 * k;
				j1 -= j2 * k;
				k = 0;
			}
			l <<= 16;
			if (i < 0) {
				l -= l1 * i;
				i = 0;
			}
			if (i2 < j2) {
				j -= i;
				i -= k;
				for (k = scanOffsets[k]; --i >= 0; k += Raster.width) {
					method377(Raster.raster, k, k1, 0, i1 >> 16, j1 >> 16);
					i1 += i2;
					j1 += j2;
				}

				while (--j >= 0) {
					method377(Raster.raster, k, k1, 0, i1 >> 16, l >> 16);
					i1 += i2;
					l += l1;
					k += Raster.width;
				}
				return;
			}
			j -= i;
			i -= k;
			for (k = scanOffsets[k]; --i >= 0; k += Raster.width) {
				method377(Raster.raster, k, k1, 0, j1 >> 16, i1 >> 16);
				i1 += i2;
				j1 += j2;
			}

			while (--j >= 0) {
				method377(Raster.raster, k, k1, 0, l >> 16, i1 >> 16);
				i1 += i2;
				l += l1;
				k += Raster.width;
			}
			return;
		}
		l = j1 <<= 16;
		if (k < 0) {
			l -= i2 * k;
			j1 -= j2 * k;
			k = 0;
		}
		i1 <<= 16;
		if (j < 0) {
			i1 -= l1 * j;
			j = 0;
		}
		if (i2 < j2) {
			i -= j;
			j -= k;
			for (k = scanOffsets[k]; --j >= 0; k += Raster.width) {
				method377(Raster.raster, k, k1, 0, l >> 16, j1 >> 16);
				l += i2;
				j1 += j2;
			}

			while (--i >= 0) {
				method377(Raster.raster, k, k1, 0, i1 >> 16, j1 >> 16);
				i1 += l1;
				j1 += j2;
				k += Raster.width;
			}
			return;
		}
		i -= j;
		j -= k;
		for (k = scanOffsets[k]; --j >= 0; k += Raster.width) {
			method377(Raster.raster, k, k1, 0, j1 >> 16, l >> 16);
			l += i2;
			j1 += j2;
		}

		while (--i >= 0) {
			method377(Raster.raster, k, k1, 0, j1 >> 16, i1 >> 16);
			i1 += l1;
			j1 += j2;
			k += Raster.width;
		}
	}

	public static void method377(int ai[], int i, int j, int k, int l, int i1) {
		if (aBoolean1462) {
			if (i1 > Raster.anInt1385) {
				i1 = Raster.anInt1385;
			}
			if (l < 0) {
				l = 0;
			}
		}
		if (l >= i1) {
			return;
		}
		i += l;
		k = i1 - l >> 2;
		if (currentAlpha == 0) {
			while (--k >= 0) {
				ai[i++] = j;
				ai[i++] = j;
				ai[i++] = j;
				ai[i++] = j;
			}
			for (k = i1 - l & 3; --k >= 0;) {
				ai[i++] = j;
			}

			return;
		}
		int j1 = currentAlpha;
		int k1 = 256 - currentAlpha;
		j = ((j & 0xff00ff) * k1 >> 8 & 0xff00ff) + ((j & 0xff00) * k1 >> 8 & 0xff00);
		while (--k >= 0) {
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
		}
		for (k = i1 - l & 3; --k >= 0;) {
			ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
		}

	}

	public static void method378(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2,
			int i3, int j3, int k3, int l3, int i4, int j4, int colour) {
		int ai[] = method371(colour);
		aBoolean1463 = !aBooleanArray1475[colour];
		k2 = j2 - k2;
		j3 = i3 - j3;
		i4 = l3 - i4;
		l2 -= j2;
		k3 -= i3;
		j4 -= l3;
		int l4 = l2 * i3 - k3 * j2 << 14;
		int i5 = k3 * l3 - j4 * i3 << 8;
		int j5 = j4 * j2 - l2 * l3 << 5;
		int k5 = k2 * i3 - j3 * j2 << 14;
		int l5 = j3 * l3 - i4 * i3 << 8;
		int i6 = i4 * j2 - k2 * l3 << 5;
		int j6 = j3 * l2 - k2 * k3 << 14;
		int k6 = i4 * k3 - j3 * j4 << 8;
		int l6 = k2 * j4 - i4 * l2 << 5;
		int i7 = 0;
		int j7 = 0;
		if (j != i) {
			i7 = (i1 - l << 16) / (j - i);
			j7 = (l1 - k1 << 16) / (j - i);
		}
		int k7 = 0;
		int l7 = 0;
		if (k != j) {
			k7 = (j1 - i1 << 16) / (k - j);
			l7 = (i2 - l1 << 16) / (k - j);
		}
		int i8 = 0;
		int j8 = 0;
		if (k != i) {
			i8 = (l - j1 << 16) / (i - k);
			j8 = (k1 - i2 << 16) / (i - k);
		}
		if (i <= j && i <= k) {
			if (i >= Raster.getClipTop()) {
				return;
			}
			if (j > Raster.getClipTop()) {
				j = Raster.getClipTop();
			}
			if (k > Raster.getClipTop()) {
				k = Raster.getClipTop();
			}
			if (j < k) {
				j1 = l <<= 16;
				i2 = k1 <<= 16;
				if (i < 0) {
					j1 -= i8 * i;
					l -= i7 * i;
					i2 -= j8 * i;
					k1 -= j7 * i;
					i = 0;
				}
				i1 <<= 16;
				l1 <<= 16;
				if (j < 0) {
					i1 -= k7 * j;
					l1 -= l7 * j;
					j = 0;
				}
				int k8 = i - originViewY;
				l4 += j5 * k8;
				k5 += i6 * k8;
				j6 += l6 * k8;
				if (i != j && i8 < i7 || i == j && i8 > k7) {
					k -= j;
					j -= i;
					i = scanOffsets[i];
					while (--j >= 0) {
						method379(Raster.raster, ai, 0, 0, i, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
						j1 += i8;
						l += i7;
						i2 += j8;
						k1 += j7;
						i += Raster.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					while (--k >= 0) {
						method379(Raster.raster, ai, 0, 0, i, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
						j1 += i8;
						i1 += k7;
						i2 += j8;
						l1 += l7;
						i += Raster.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					return;
				}
				k -= j;
				j -= i;
				i = scanOffsets[i];
				while (--j >= 0) {
					method379(Raster.raster, ai, 0, 0, i, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += i8;
					l += i7;
					i2 += j8;
					k1 += j7;
					i += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--k >= 0) {
					method379(Raster.raster, ai, 0, 0, i, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += i8;
					i1 += k7;
					i2 += j8;
					l1 += l7;
					i += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			i1 = l <<= 16;
			l1 = k1 <<= 16;
			if (i < 0) {
				i1 -= i8 * i;
				l -= i7 * i;
				l1 -= j8 * i;
				k1 -= j7 * i;
				i = 0;
			}
			j1 <<= 16;
			i2 <<= 16;
			if (k < 0) {
				j1 -= k7 * k;
				i2 -= l7 * k;
				k = 0;
			}
			int l8 = i - originViewY;
			l4 += j5 * l8;
			k5 += i6 * l8;
			j6 += l6 * l8;
			if (i != k && i8 < i7 || i == k && k7 > i7) {
				j -= k;
				k -= i;
				i = scanOffsets[i];
				while (--k >= 0) {
					method379(Raster.raster, ai, 0, 0, i, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					i1 += i8;
					l += i7;
					l1 += j8;
					k1 += j7;
					i += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--j >= 0) {
					method379(Raster.raster, ai, 0, 0, i, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += k7;
					l += i7;
					i2 += l7;
					k1 += j7;
					i += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			j -= k;
			k -= i;
			i = scanOffsets[i];
			while (--k >= 0) {
				method379(Raster.raster, ai, 0, 0, i, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += i8;
				l += i7;
				l1 += j8;
				k1 += j7;
				i += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--j >= 0) {
				method379(Raster.raster, ai, 0, 0, i, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				j1 += k7;
				l += i7;
				i2 += l7;
				k1 += j7;
				i += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		if (j <= k) {
			if (j >= Raster.getClipTop()) {
				return;
			}
			if (k > Raster.getClipTop()) {
				k = Raster.getClipTop();
			}
			if (i > Raster.getClipTop()) {
				i = Raster.getClipTop();
			}
			if (k < i) {
				l = i1 <<= 16;
				k1 = l1 <<= 16;
				if (j < 0) {
					l -= i7 * j;
					i1 -= k7 * j;
					k1 -= j7 * j;
					l1 -= l7 * j;
					j = 0;
				}
				j1 <<= 16;
				i2 <<= 16;
				if (k < 0) {
					j1 -= i8 * k;
					i2 -= j8 * k;
					k = 0;
				}
				int i9 = j - originViewY;
				l4 += j5 * i9;
				k5 += i6 * i9;
				j6 += l6 * i9;
				if (j != k && i7 < k7 || j == k && i7 > i8) {
					i -= k;
					k -= j;
					j = scanOffsets[j];
					while (--k >= 0) {
						method379(Raster.raster, ai, 0, 0, j, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
						l += i7;
						i1 += k7;
						k1 += j7;
						l1 += l7;
						j += Raster.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					while (--i >= 0) {
						method379(Raster.raster, ai, 0, 0, j, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
						l += i7;
						j1 += i8;
						k1 += j7;
						i2 += j8;
						j += Raster.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					return;
				}
				i -= k;
				k -= j;
				j = scanOffsets[j];
				while (--k >= 0) {
					method379(Raster.raster, ai, 0, 0, j, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					l += i7;
					i1 += k7;
					k1 += j7;
					l1 += l7;
					j += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--i >= 0) {
					method379(Raster.raster, ai, 0, 0, j, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					l += i7;
					j1 += i8;
					k1 += j7;
					i2 += j8;
					j += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			j1 = i1 <<= 16;
			i2 = l1 <<= 16;
			if (j < 0) {
				j1 -= i7 * j;
				i1 -= k7 * j;
				i2 -= j7 * j;
				l1 -= l7 * j;
				j = 0;
			}
			l <<= 16;
			k1 <<= 16;
			if (i < 0) {
				l -= i8 * i;
				k1 -= j8 * i;
				i = 0;
			}
			int j9 = j - originViewY;
			l4 += j5 * j9;
			k5 += i6 * j9;
			j6 += l6 * j9;
			if (i7 < k7) {
				k -= i;
				i -= j;
				j = scanOffsets[j];
				while (--i >= 0) {
					method379(Raster.raster, ai, 0, 0, j, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += i7;
					i1 += k7;
					i2 += j7;
					l1 += l7;
					j += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--k >= 0) {
					method379(Raster.raster, ai, 0, 0, j, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
					l += i8;
					i1 += k7;
					k1 += j8;
					l1 += l7;
					j += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			k -= i;
			i -= j;
			j = scanOffsets[j];
			while (--i >= 0) {
				method379(Raster.raster, ai, 0, 0, j, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				j1 += i7;
				i1 += k7;
				i2 += j7;
				l1 += l7;
				j += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--k >= 0) {
				method379(Raster.raster, ai, 0, 0, j, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
				l += i8;
				i1 += k7;
				k1 += j8;
				l1 += l7;
				j += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		if (k >= Raster.getClipTop()) {
			return;
		}
		if (i > Raster.getClipTop()) {
			i = Raster.getClipTop();
		}
		if (j > Raster.getClipTop()) {
			j = Raster.getClipTop();
		}
		if (i < j) {
			i1 = j1 <<= 16;
			l1 = i2 <<= 16;
			if (k < 0) {
				i1 -= k7 * k;
				j1 -= i8 * k;
				l1 -= l7 * k;
				i2 -= j8 * k;
				k = 0;
			}
			l <<= 16;
			k1 <<= 16;
			if (i < 0) {
				l -= i7 * i;
				k1 -= j7 * i;
				i = 0;
			}
			int k9 = k - originViewY;
			l4 += j5 * k9;
			k5 += i6 * k9;
			j6 += l6 * k9;
			if (k7 < i8) {
				j -= i;
				i -= k;
				k = scanOffsets[k];
				while (--i >= 0) {
					method379(Raster.raster, ai, 0, 0, k, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
					i1 += k7;
					j1 += i8;
					l1 += l7;
					i2 += j8;
					k += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--j >= 0) {
					method379(Raster.raster, ai, 0, 0, k, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					i1 += k7;
					l += i7;
					l1 += l7;
					k1 += j7;
					k += Raster.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			j -= i;
			i -= k;
			k = scanOffsets[k];
			while (--i >= 0) {
				method379(Raster.raster, ai, 0, 0, k, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += k7;
				j1 += i8;
				l1 += l7;
				i2 += j8;
				k += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--j >= 0) {
				method379(Raster.raster, ai, 0, 0, k, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += k7;
				l += i7;
				l1 += l7;
				k1 += j7;
				k += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		l = j1 <<= 16;
		k1 = i2 <<= 16;
		if (k < 0) {
			l -= k7 * k;
			j1 -= i8 * k;
			k1 -= l7 * k;
			i2 -= j8 * k;
			k = 0;
		}
		i1 <<= 16;
		l1 <<= 16;
		if (j < 0) {
			i1 -= i7 * j;
			l1 -= j7 * j;
			j = 0;
		}
		int l9 = k - originViewY;
		l4 += j5 * l9;
		k5 += i6 * l9;
		j6 += l6 * l9;
		if (k7 < i8) {
			i -= j;
			j -= k;
			k = scanOffsets[k];
			while (--j >= 0) {
				method379(Raster.raster, ai, 0, 0, k, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				l += k7;
				j1 += i8;
				k1 += l7;
				i2 += j8;
				k += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--i >= 0) {
				method379(Raster.raster, ai, 0, 0, k, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += i7;
				j1 += i8;
				l1 += j7;
				i2 += j8;
				k += Raster.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		i -= j;
		j -= k;
		k = scanOffsets[k];
		while (--j >= 0) {
			method379(Raster.raster, ai, 0, 0, k, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
			l += k7;
			j1 += i8;
			k1 += l7;
			i2 += j8;
			k += Raster.width;
			l4 += j5;
			k5 += i6;
			j6 += l6;
		}
		while (--i >= 0) {
			method379(Raster.raster, ai, 0, 0, k, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
			i1 += i7;
			j1 += i8;
			l1 += j7;
			i2 += j8;
			k += Raster.width;
			l4 += j5;
			k5 += i6;
			j6 += l6;
		}
	}

	public static void method379(int ai[], int ai1[], int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2,
			int k2, int l2, int i3) {
		if (l >= i1) {
			return;
		}
		int j3;
		int k3;
		if (aBoolean1462) {
			j3 = (k1 - j1) / (i1 - l);
			if (i1 > Raster.anInt1385) {
				i1 = Raster.anInt1385;
			}
			if (l < 0) {
				j1 -= l * j3;
				l = 0;
			}
			if (l >= i1) {
				return;
			}
			k3 = i1 - l >> 3;
			j3 <<= 12;
			j1 <<= 9;
		} else {
			if (i1 - l > 7) {
				k3 = i1 - l >> 3;
				j3 = (k1 - j1) * anIntArray1468[k3] >> 6;
			} else {
				k3 = 0;
				j3 = 0;
			}
			j1 <<= 9;
		}
		k += l;
		if (lowMemory) {
			int i4 = 0;
			int k4 = 0;
			int k6 = l - originViewX;
			l1 += (k2 >> 3) * k6;
			i2 += (l2 >> 3) * k6;
			j2 += (i3 >> 3) * k6;
			int i5 = j2 >> 12;
			if (i5 != 0) {
				i = l1 / i5;
				j = i2 / i5;
				if (i < 0) {
					i = 0;
				} else if (i > 4032) {
					i = 4032;
				}
			}
			l1 += k2;
			i2 += l2;
			j2 += i3;
			i5 = j2 >> 12;
			if (i5 != 0) {
				i4 = l1 / i5;
				k4 = i2 / i5;
				if (i4 < 7) {
					i4 = 7;
				} else if (i4 > 4032) {
					i4 = 4032;
				}
			}
			int i7 = i4 - i >> 3;
			int k7 = k4 - j >> 3;
			i += (j1 & 0x600000) >> 3;
			int i8 = j1 >> 23;
			if (aBoolean1463) {
				while (k3-- > 0) {
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i = i4;
					j = k4;
					l1 += k2;
					i2 += l2;
					j2 += i3;
					int j5 = j2 >> 12;
					if (j5 != 0) {
						i4 = l1 / j5;
						k4 = i2 / j5;
						if (i4 < 7) {
							i4 = 7;
						} else if (i4 > 4032) {
							i4 = 4032;
						}
					}
					i7 = i4 - i >> 3;
					k7 = k4 - j >> 3;
					j1 += j3;
					i += (j1 & 0x600000) >> 3;
					i8 = j1 >> 23;
				}
				for (k3 = i1 - l & 7; k3-- > 0;) {
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
				}

				return;
			}
			while (k3-- > 0) {
				int k8;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i = i4;
				j = k4;
				l1 += k2;
				i2 += l2;
				j2 += i3;
				int k5 = j2 >> 12;
				if (k5 != 0) {
					i4 = l1 / k5;
					k4 = i2 / k5;
					if (i4 < 7) {
						i4 = 7;
					} else if (i4 > 4032) {
						i4 = 4032;
					}
				}
				i7 = i4 - i >> 3;
				k7 = k4 - j >> 3;
				j1 += j3;
				i += (j1 & 0x600000) >> 3;
				i8 = j1 >> 23;
			}
			for (k3 = i1 - l & 7; k3-- > 0;) {
				int l8;
				if ((l8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = l8;
				}
				k++;
				i += i7;
				j += k7;
			}

			return;
		}
		int j4 = 0;
		int l4 = 0;
		int l6 = l - originViewX;
		l1 += (k2 >> 3) * l6;
		i2 += (l2 >> 3) * l6;
		j2 += (i3 >> 3) * l6;
		int l5 = j2 >> 14;
		if (l5 != 0) {
			i = l1 / l5;
			j = i2 / l5;
			if (i < 0) {
				i = 0;
			} else if (i > 16256) {
				i = 16256;
			}
		}
		l1 += k2;
		i2 += l2;
		j2 += i3;
		l5 = j2 >> 14;
		if (l5 != 0) {
			j4 = l1 / l5;
			l4 = i2 / l5;
			if (j4 < 7) {
				j4 = 7;
			} else if (j4 > 16256) {
				j4 = 16256;
			}
		}
		int j7 = j4 - i >> 3;
		int l7 = l4 - j >> 3;
		i += j1 & 0x600000;
		int j8 = j1 >> 23;
		if (aBoolean1463) {
			while (k3-- > 0) {
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i = j4;
				j = l4;
				l1 += k2;
				i2 += l2;
				j2 += i3;
				int i6 = j2 >> 14;
				if (i6 != 0) {
					j4 = l1 / i6;
					l4 = i2 / i6;
					if (j4 < 7) {
						j4 = 7;
					} else if (j4 > 16256) {
						j4 = 16256;
					}
				}
				j7 = j4 - i >> 3;
				l7 = l4 - j >> 3;
				j1 += j3;
				i += j1 & 0x600000;
				j8 = j1 >> 23;
			}
			for (k3 = i1 - l & 7; k3-- > 0;) {
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
			}

			return;
		}
		while (k3-- > 0) {
			int i9;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i = j4;
			j = l4;
			l1 += k2;
			i2 += l2;
			j2 += i3;
			int j6 = j2 >> 14;
			if (j6 != 0) {
				j4 = l1 / j6;
				l4 = i2 / j6;
				if (j4 < 7) {
					j4 = 7;
				} else if (j4 > 16256) {
					j4 = 16256;
				}
			}
			j7 = j4 - i >> 3;
			l7 = l4 - j >> 3;
			j1 += j3;
			i += j1 & 0x600000;
			j8 = j1 >> 23;
		}
		for (int l3 = i1 - l & 7; l3-- > 0;) {
			int j9;
			if ((j9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = j9;
			}
			k++;
			i += j7;
			j += l7;
		}
	}

	public static void reposition(int width, int length) {
		scanOffsets = new int[width];

		for (int x = 0; x < width; x++) {
			scanOffsets[x] = length * x;
		}

		originViewX = length / 2;
		originViewY = width / 2;
	}

	public static void useViewport() {
		scanOffsets = new int[Raster.height];
		for (int j = 0; j < Raster.height; j++) {
			scanOffsets[j] = Raster.width * j;
		}

		originViewX = Raster.width / 2;
		originViewY = Raster.height / 2;
	}

}