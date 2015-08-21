package com.jagex.cache.graphics;

import com.jagex.cache.Archive;
import com.jagex.draw.Raster;
import com.jagex.io.Buffer;

import java.util.Random;

public final class Font extends Raster {

	private int[] glyphHeights = new int[256];
	private byte[][] glyphs = new byte[256][];
	private int[] glyphSpacings = new int[256];
	private int[] glyphWidths = new int[256];
	private int[] horizontalOffsets = new int[256];
	private Random random = new Random();
	private boolean strikethrough;
	private int[] verticalOffsets = new int[256];
	private int verticalSpace;

	public Font(boolean wideSpace, String name, Archive archive) {
		Buffer data = new Buffer(archive.getEntry(name + ".dat"));
		Buffer meta = new Buffer(archive.getEntry("index.dat"));
		meta.setPosition(data.readUShort() + 4);

		int position = meta.readUByte();
		if (position > 0) {
			meta.setPosition(meta.getPosition() + 3 * (position - 1));
		}

		for (int character = 0; character < 256; character++) {
			horizontalOffsets[character] = meta.readUByte();
			verticalOffsets[character] = meta.readUByte();
			int width = glyphWidths[character] = meta.readUShort();
			int height = glyphHeights[character] = meta.readUShort();
			int format = meta.readUByte();
			int pixels = width * height;
			glyphs[character] = new byte[pixels];

			if (format == 0) {
				for (int pixel = 0; pixel < pixels; pixel++) {
					glyphs[character][pixel] = data.readByte();
				}
			} else if (format == 1) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						glyphs[character][x + y * width] = data.readByte();
					}
				}
			}

			if (height > verticalSpace && character < 128) {
				verticalSpace = height;
			}

			horizontalOffsets[character] = 1;
			glyphSpacings[character] = width + 2;
			int filledCount = 0;

			for (int y = height / 7; y < height; y++) {
				filledCount += glyphs[character][y * width];
			}

			if (filledCount <= height / 7) {
				glyphSpacings[character]--;
				horizontalOffsets[character] = 0;
			}
			filledCount = 0;

			for (int y = height / 7; y < height; y++) {
				filledCount += glyphs[character][width - 1 + y * width];
			}

			if (filledCount <= height / 7) {
				glyphSpacings[character]--;
			}
		}

		glyphSpacings[' '] = wideSpace ? glyphSpacings['I'] : glyphSpacings['i'];
	}

	public int getColouredTextWidth(String text) {
		if (text == null) {
			return 0;
		}

		int width = 0;
		for (int index = 0; index < text.length(); index++) {
			if (text.charAt(index) == '@' && index + 4 < text.length() && text.charAt(index + 4) == '@') {
				index += 4;
			} else {
				width += glyphSpacings[text.charAt(index)];
			}
		}

		return width;
	}

	public int[] getGlyphHeights() {
		return glyphHeights;
	}

	public byte[][] getGlyphs() {
		return glyphs;
	}

	public int[] getGlyphSpacings() {
		return glyphSpacings;
	}

	public int[] getGlyphWidths() {
		return glyphWidths;
	}

	public int[] getHorizontalOffsets() {
		return horizontalOffsets;
	}

	public Random getRandom() {
		return random;
	}

	public int getTextWidth(String text) {
		if (text == null) {
			return 0;
		}

		int width = 0;
		for (int index = 0; index < text.length(); index++) {
			width += glyphSpacings[text.charAt(index)];
		}

		return width;
	}

	public int[] getVerticalOffsets() {
		return verticalOffsets;
	}

	public int getVerticalSpace() {
		return verticalSpace;
	}

	public boolean isStrikethrough() {
		return strikethrough;
	}

	public void render(int x, int y, String text, int colour) {
		if (text == null) {
			return;
		}

		y -= verticalSpace;
		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (character != ' ') {
				if (character == 32 || character == 73 | character == 105) {

				}
				render(glyphs[character], x + horizontalOffsets[character], y + verticalOffsets[character],
						glyphWidths[character], glyphHeights[character], colour);
			}

			x += glyphSpacings[character];
		}
	}

	public void renderCentre(int x, int y, String text, int colour) {
		render(x - getTextWidth(text) / 2, y, text, colour);
	}

	public void renderLeft(int x, int y, String text, int colour) {
		render(x - getTextWidth(text), y, text, colour);
	}

	public void renderRandom(String text, int x, int y, int colour, boolean shadow, int seed) {
		if (text == null) {
			return;
		}

		random.setSeed(seed);
		int alpha = 192 + (random.nextInt() & 0x1f);
		y -= verticalSpace;

		for (int index = 0; index < text.length(); index++) {
			if (text.charAt(index) == '@' && index + 4 < text.length() && text.charAt(index + 4) == '@') {
				int rgb = rgb(text.substring(index + 1, index + 4));
				if (rgb != -1) {
					colour = rgb;
				}

				index += 4;
			} else {
				char character = text.charAt(index);
				if (character != ' ') {
					if (shadow) {
						renderRgba(glyphs[character], x + horizontalOffsets[character] + 1, y + 1 + verticalOffsets[character],
								glyphHeights[character], glyphWidths[character], 192, 0);
					}
					renderRgba(glyphs[character], x + horizontalOffsets[character], y + verticalOffsets[character],
							glyphHeights[character], glyphWidths[character], alpha, colour);
				}

				x += glyphSpacings[character];
				if ((random.nextInt() & 3) == 0) {
					x++;
				}
			}
		}
	}

	public int rgb(String colour) {
		if (colour.equals("red")) {
			return 0xff0000;
		} else if (colour.equals("gre")) {
			return 65280;
		} else if (colour.equals("blu")) {
			return 255;
		} else if (colour.equals("yel")) {
			return 0xffff00;
		} else if (colour.equals("cya")) {
			return 65535;
		} else if (colour.equals("mag")) {
			return 0xff00ff;
		} else if (colour.equals("whi")) {
			return 0xffffff;
		} else if (colour.equals("bla")) {
			return 0;
		} else if (colour.equals("lre")) {
			return 0xff9040;
		} else if (colour.equals("dre")) {
			return 0x800000;
		} else if (colour.equals("dbl")) {
			return 128;
		} else if (colour.equals("or1")) {
			return 0xffb000;
		} else if (colour.equals("or2")) {
			return 0xff7000;
		} else if (colour.equals("or3")) {
			return 0xff3000;
		} else if (colour.equals("gr1")) {
			return 0xc0ff00;
		} else if (colour.equals("gr2")) {
			return 0x80ff00;
		} else if (colour.equals("gr3")) {
			return 0x40ff00;
		} else if (colour.equals("str")) {
			strikethrough = true;
		} else if (colour.equals("end")) {
			strikethrough = false;
		}

		return -1;
	}

	public void setGlyphHeights(int[] glyphHeights) {
		this.glyphHeights = glyphHeights;
	}

	public void setGlyphs(byte[][] glyphs) {
		this.glyphs = glyphs;
	}

	public void setGlyphSpacings(int[] glyphSpacings) {
		this.glyphSpacings = glyphSpacings;
	}

	public void setGlyphWidths(int[] glyphWidths) {
		this.glyphWidths = glyphWidths;
	}

	public void setHorizontalOffsets(int[] horizontalOffsets) {
		this.horizontalOffsets = horizontalOffsets;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public void setStrikethrough(boolean strikethrough) {
		this.strikethrough = strikethrough;
	}

	public void setVerticalOffsets(int[] verticalOffsets) {
		this.verticalOffsets = verticalOffsets;
	}

	public void setVerticalSpace(int verticalSpace) {
		this.verticalSpace = verticalSpace;
	}

	public void shadow(int x, int y, String text, boolean shadow, int colour) {
		strikethrough = false;
		int width = x;
		if (text == null) {
			return;
		}
		y -= verticalSpace;

		for (int index = 0; index < text.length(); index++) {
			if (text.charAt(index) == '@' && index + 4 < text.length() && text.charAt(index + 4) == '@') {
				int rgb = rgb(text.substring(index + 1, index + 4));
				if (rgb != -1) {
					colour = rgb;
				}

				index += 4;
			} else {
				char character = text.charAt(index);

				if (character != ' ') {
					if (shadow) {
						render(glyphs[character], x + horizontalOffsets[character] + 1, y + verticalOffsets[character] + 1,
								glyphWidths[character], glyphHeights[character], 0);
					}

					render(glyphs[character], x + horizontalOffsets[character], y + verticalOffsets[character],
							glyphWidths[character], glyphHeights[character], colour);
				}

				x += glyphSpacings[character];
			}
		}

		if (strikethrough) {
			Raster.drawHorizontal(width, y + (int) (verticalSpace * 0.7D), x - width, 0x800000);
		}
	}

	public void shadowCentre(int x, int y, String text, boolean shadow, int colour) {
		shadow(x - getColouredTextWidth(text) / 2, y, text, shadow, colour);
	}

	public void shake(String text, int x, int y, int colour, int elapsed, int tick) {
		if (text == null) {
			return;
		}

		double amplitude = 7D - elapsed / 8D;
		if (amplitude < 0) {
			amplitude = 0;
		}
		x -= getTextWidth(text) / 2;
		y -= verticalSpace;

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (character != ' ') {
				render(glyphs[character], x + horizontalOffsets[character],
						y + verticalOffsets[character] + (int) (Math.sin(index / 1.5D + tick) * amplitude),
						glyphWidths[character], glyphHeights[character], colour);
			}
			x += glyphSpacings[character];
		}
	}

	public void wave(String text, int x, int y, int colour, int tick) {
		if (text == null) {
			return;
		}
		x -= getTextWidth(text) / 2;
		y -= verticalSpace;

		for (int index = 0; index < text.length(); index++) {
			char c = text.charAt(index);
			if (c != ' ') {
				render(glyphs[c], x + horizontalOffsets[c],
						y + verticalOffsets[c] + (int) (Math.sin(index / 2D + tick / 5D) * 5), glyphWidths[c], glyphHeights[c],
						colour);
			}
			x += glyphSpacings[c];
		}
	}

	public void wave2(String text, int x, int y, int colour, int tick) {
		if (text == null) {
			return;
		}
		x -= getTextWidth(text) / 2;
		y -= verticalSpace;

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (character != ' ') {
				render(glyphs[character], x + horizontalOffsets[character] + (int) (Math.sin(index / 5D + tick / 5D) * 5), y
						+ verticalOffsets[character] + (int) (Math.sin(index / 3D + tick / 5D) * 5), glyphWidths[character],
						glyphHeights[character], colour);
			}

			x += glyphSpacings[character];
		}
	}

	private void render(byte[] glyph, int x, int y, int width, int height, int colour) {
		int rasterIndex = x + y * Raster.width;
		int rasterClip = Raster.width - width;
		int glyphClip = 0;
		int glyphIndex = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			glyphIndex += dy * width;
			rasterIndex += dy * Raster.width;
		}

		if (y + height >= Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop() + 1;
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			glyphIndex += dx;
			rasterIndex += dx;
			glyphClip += dx;
			rasterClip += dx;
		}

		if (x + width >= Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight() + 1;
			width -= dx;
			glyphClip += dx;
			rasterClip += dx;
		}

		if (width > 0 && height > 0) {
			render(Raster.raster, glyph, colour, glyphIndex, rasterIndex, width, height, rasterClip, glyphClip);
		}
	}

	private void render(int[] raster, byte[] glyph, int colour, int glyphPosition, int rasterPosition, int width, int height,
			int rasterOffset, int glyphOffset) {
		int offsetX = -(width >> 2);
		width = -(width & 3);

		for (int y = -height; y < 0; y++) {
			for (int x = offsetX; x < 0; x++) {
				if (glyph[glyphPosition++] != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
				if (glyph[glyphPosition++] != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
				if (glyph[glyphPosition++] != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
				if (glyph[glyphPosition++] != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
			}

			for (int i = width; i < 0; i++) {
				if (glyph[glyphPosition++] != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
			}

			rasterPosition += rasterOffset;
			glyphPosition += glyphOffset;
		}
	}

	private void renderRgba(byte[] glyph, int x, int y, int height, int width, int alpha, int colour) {
		int rasterIndex = x + y * Raster.width;
		int rasterClip = Raster.width - width;
		int glyphClip = 0;
		int glyphIndex = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			glyphIndex += dy * width;
			rasterIndex += dy * Raster.width;
		}

		if (y + height >= Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop() + 1;
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			glyphIndex += dx;
			rasterIndex += dx;
			glyphClip += dx;
			rasterClip += dx;
		}

		if (x + width >= Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight() + 1;
			width -= dx;
			glyphClip += dx;
			rasterClip += dx;
		}

		if (width > 0 && height > 0) {
			renderRgba(glyph, height, rasterIndex, Raster.raster, glyphIndex, width, glyphClip, rasterClip, colour, alpha);
		}
	}

	private void renderRgba(byte[] glyph, int height, int rasterPosition, int[] raster, int glyphPosition, int width,
			int glyphOffset, int rasterOffset, int colour, int alpha) {
		colour = ((colour & 0xff00ff) * alpha & 0xff00ff00) + ((colour & 0xff00) * alpha & 0xff0000) >> 8;
		alpha = 256 - alpha;
		for (int y = -height; y < 0; y++) {
			for (int x = -width; x < 0; x++) {
				if (glyph[glyphPosition++] != 0) {
					int rgba = raster[rasterPosition];
					raster[rasterPosition++] = (((rgba & 0xff00ff) * alpha & 0xff00ff00) + ((rgba & 0xff00) * alpha & 0xff0000) >> 8)
							+ colour;
				} else {
					rasterPosition++;
				}
			}

			rasterPosition += rasterOffset;
			glyphPosition += glyphOffset;
		}
	}

}