package com.jagex.cache.graphics;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;

import com.jagex.cache.Archive;
import com.jagex.draw.Raster;
import com.jagex.io.Buffer;

public final class Sprite extends Raster {

	private int height;
	private int horizontalOffset;
	private int[] raster;
	private int resizeHeight;
	private int resizeWidth;
	private int verticalOffset;
	private int width;

	public Sprite(Archive archive, String name, int id) {
		Buffer sprite = new Buffer(archive.getEntry(name + ".dat"));
		Buffer meta = new Buffer(archive.getEntry("index.dat"));
		meta.setPosition(sprite.readUShort());

		resizeWidth = meta.readUShort();
		resizeHeight = meta.readUShort();

		int colours = meta.readUByte();
		int[] palette = new int[colours];

		for (int index = 0; index < colours - 1; index++) {
			int colour = meta.readTriByte();
			palette[index + 1] = colour == 0 ? 1 : colour;
		}

		for (int i = 0; i < id; i++) {
			meta.setPosition(meta.getPosition() + 2);
			sprite.setPosition(sprite.getPosition() + meta.readUShort() * meta.readUShort());
			meta.setPosition(meta.getPosition() + 1);
		}

		horizontalOffset = meta.readUByte();
		verticalOffset = meta.readUByte();
		width = meta.readUShort();
		height = meta.readUShort();

		int format = meta.readUByte();
		int pixels = width * height;
		raster = new int[pixels];

		if (format == 0) {
			for (int index = 0; index < pixels; index++) {
				raster[index] = palette[sprite.readUByte()];
			}
		} else if (format == 1) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					raster[x + y * width] = palette[sprite.readUByte()];
				}
			}
		}
	}

	public Sprite(byte[] data, Component component) {
		try {
			Image image = Toolkit.getDefaultToolkit().createImage(data);
			MediaTracker tracker = new MediaTracker(component);
			tracker.addImage(image, 0);
			tracker.waitForAll();

			width = image.getWidth(component);
			height = image.getHeight(component);
			resizeWidth = width;
			resizeHeight = height;

			horizontalOffset = 0;
			verticalOffset = 0;
			raster = new int[width * height];

			PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, raster, 0, width);
			grabber.grabPixels();
		} catch (Exception ex) {
			System.out.println("Error converting jpg");
		}
	}

	public Sprite(int width, int height) {
		raster = new int[width * height];
		this.width = resizeWidth = width;
		this.height = resizeHeight = height;
		horizontalOffset = verticalOffset = 0;
	}

	public void drawSprite(int x, int y) {
		x += horizontalOffset;
		y += verticalOffset;
		int rasterClip = x + y * Raster.width;
		int imageClip = 0;
		int height = this.height;
		int width = this.width;
		int rasterOffset = Raster.width - width;
		int imageOffset = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			imageClip += dy * width;
			rasterClip += dy * Raster.width;
		}

		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			imageClip += dx;
			rasterClip += dx;
			imageOffset += dx;
			rasterOffset += dx;
		}

		if (x + width > Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight();
			width -= dx;
			imageOffset += dx;
			rasterOffset += dx;
		}

		if (width > 0 && height > 0) {
			draw(Raster.raster, raster, 0, imageClip, rasterClip, width, height, rasterOffset, imageOffset);
		}
	}

	public void drawSprite(int x, int y, int alpha) {
		x += horizontalOffset;
		y += verticalOffset;
		int i1 = x + y * Raster.width;
		int j1 = 0;
		int height = this.height;
		int width = this.width;
		int dx = Raster.width - width;
		int j2 = 0;
		if (y < Raster.getClipBottom()) {
			int k2 = Raster.getClipBottom() - y;
			height -= k2;
			y = Raster.getClipBottom();
			j1 += k2 * width;
			i1 += k2 * Raster.width;
		}
		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}
		if (x < Raster.getClipLeft()) {
			int l2 = Raster.getClipLeft() - x;
			width -= l2;
			x = Raster.getClipLeft();
			j1 += l2;
			i1 += l2;
			j2 += l2;
			dx += l2;
		}
		if (x + width > Raster.getClipRight()) {
			int i3 = x + width - Raster.getClipRight();
			width -= i3;
			j2 += i3;
			dx += i3;
		}
		if (width > 0 && height > 0) {
			method351(j1, width, Raster.raster, 0, raster, j2, height, dx, alpha, i1);
		}
	}

	public int getHeight() {
		return height;
	}

	public int getHorizontalOffset() {
		return horizontalOffset;
	}

	public int[] getRaster() {
		return raster;
	}
	
	public int getPixel(int index){
		return raster[index];
	}

	public int getResizeHeight() {
		return resizeHeight;
	}

	public int getResizeWidth() {
		return resizeWidth;
	}

	public int getVerticalOffset() {
		return verticalOffset;
	}

	public int getWidth() {
		return width;
	}

	public void initRaster() {
		Raster.init(height, width, raster);
	}

	public void method346(int x, int y) {
		x += horizontalOffset;
		y += verticalOffset;
		int l = x + y * Raster.width;
		int i1 = 0;
		int j1 = height;
		int k1 = width;
		int l1 = Raster.width - k1;
		int i2 = 0;
		if (y < Raster.getClipBottom()) {
			int j2 = Raster.getClipBottom() - y;
			j1 -= j2;
			y = Raster.getClipBottom();
			i1 += j2 * k1;
			l += j2 * Raster.width;
		}
		if (y + j1 > Raster.getClipTop()) {
			j1 -= y + j1 - Raster.getClipTop();
		}
		if (x < Raster.getClipLeft()) {
			int k2 = Raster.getClipLeft() - x;
			k1 -= k2;
			x = Raster.getClipLeft();
			i1 += k2;
			l += k2;
			i2 += k2;
			l1 += k2;
		}
		if (x + k1 > Raster.getClipRight()) {
			int l2 = x + k1 - Raster.getClipRight();
			k1 -= l2;
			i2 += l2;
			l1 += l2;
		}
		if (k1 > 0 && j1 > 0) {
			method347(l, k1, j1, i2, i1, l1, raster, Raster.raster);
		}
	}

	public void method352(int i, int theta, int ai[], int k, int ai1[], int i1, int j1, int k1, int l1, int i2) {
		try {
			int j2 = -l1 / 2;
			int k2 = -i / 2;
			int l2 = (int) (Math.sin(theta / 326.11D) * 65536);
			int i3 = (int) (Math.cos(theta / 326.11D) * 65536);
			l2 = l2 * k >> 8;
			i3 = i3 * k >> 8;
			int j3 = (i2 << 16) + k2 * l2 + j2 * i3;
			int k3 = (i1 << 16) + k2 * i3 - j2 * l2;
			int l3 = k1 + j1 * Raster.width;
			for (j1 = 0; j1 < i; j1++) {
				int i4 = ai1[j1];
				int j4 = l3 + i4;
				int k4 = j3 + i3 * i4;
				int l4 = k3 - l2 * i4;
				for (k1 = -ai[j1]; k1 < 0; k1++) {
					Raster.raster[j4++] = raster[(k4 >> 16) + (l4 >> 16) * width];
					k4 += i3;
					l4 -= l2;
				}

				j3 += l2;
				k3 += i3;
				l3 += Raster.width;
			}
		} catch (Exception ex) {
		}
	}

	public void method353(int i, int j, int k, int l, int j1, int k1, double theta, int l1) {
		try {
			int i2 = -k / 2;
			int j2 = -k1 / 2;
			int k2 = (int) (Math.sin(theta) * 65536D);
			int l2 = (int) (Math.cos(theta) * 65536D);
			k2 = k2 * j1 >> 8;
			l2 = l2 * j1 >> 8;
			int i3 = (l << 16) + j2 * k2 + i2 * l2;
			int j3 = (j << 16) + j2 * l2 - i2 * k2;
			int k3 = l1 + i * Raster.width;
			for (i = 0; i < k1; i++) {
				int l3 = k3;
				int i4 = i3;
				int j4 = j3;
				for (l1 = -k; l1 < 0; l1++) {
					int k4 = raster[(i4 >> 16) + (j4 >> 16) * width];
					if (k4 != 0) {
						Raster.raster[l3++] = k4;
					} else {
						l3++;
					}
					i4 += l2;
					j4 -= k2;
				}

				i3 += k2;
				j3 += l2;
				k3 += Raster.width;
			}
		} catch (Exception ex) {
		}
	}

	public void method354(IndexedImage image, int y, int x) {
		x += horizontalOffset;
		y += verticalOffset;
		int k = x + y * Raster.width;
		int l = 0;
		int height = this.height;
		int width = this.width;
		int deltaWidth = Raster.width - width;
		int l1 = 0;

		if (y < Raster.getClipBottom()) {
			int dy = Raster.getClipBottom() - y;
			height -= dy;
			y = Raster.getClipBottom();
			l += dy * width;
			k += dy * Raster.width;
		}

		if (y + height > Raster.getClipTop()) {
			height -= y + height - Raster.getClipTop();
		}

		if (x < Raster.getClipLeft()) {
			int dx = Raster.getClipLeft() - x;
			width -= dx;
			x = Raster.getClipLeft();
			l += dx;
			k += dx;
			l1 += dx;
			deltaWidth += dx;
		}

		if (x + width > Raster.getClipRight()) {
			int dx = x + width - Raster.getClipRight();
			width -= dx;
			l1 += dx;
			deltaWidth += dx;
		}
		if (width > 0 && height > 0) {
			method355(raster, width, image.getRaster(), height, Raster.raster, 0, deltaWidth, k, l1, l);
		}
	}

	public void recolour(int redOffset, int greenOffset, int blueOffset) {
		for (int index = 0; index < raster.length; index++) {
			int rgb = raster[index];

			if (rgb != 0) {
				int red = rgb >> 16 & 0xff;
				red += redOffset;

				if (red < 1) {
					red = 1;
				} else if (red > 255) {
					red = 255;
				}

				int green = rgb >> 8 & 0xff;
				green += greenOffset;

				if (green < 1) {
					green = 1;
				} else if (green > 255) {
					green = 255;
				}

				int blue = rgb & 0xff;
				blue += blueOffset;

				if (blue < 1) {
					blue = 1;
				} else if (blue > 255) {
					blue = 255;
				}

				raster[index] = (red << 16) + (green << 8) + blue;
			}
		}
	}

	public void resize() {
		int[] raster = new int[resizeWidth * resizeHeight];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				raster[(y + verticalOffset) * resizeWidth + x + horizontalOffset] = this.raster[y * width + x];
			}
		}

		this.raster = raster;
		width = resizeWidth;
		height = resizeHeight;
		horizontalOffset = 0;
		verticalOffset = 0;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setHorizontalOffset(int horizontalOffset) {
		this.horizontalOffset = horizontalOffset;
	}

	public void setRaster(int[] raster) {
		this.raster = raster;
	}

	public void setResizeHeight(int resizeHeight) {
		this.resizeHeight = resizeHeight;
	}

	public void setResizeWidth(int resizeWidth) {
		this.resizeWidth = resizeWidth;
	}

	public void setVerticalOffset(int verticalOffset) {
		this.verticalOffset = verticalOffset;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	private void draw(int raster[], int[] image, int colour, int imagePosition, int rasterPosition, int width, int height,
			int rasterOffset, int imageOffset) {
		int minX = -(width >> 2);
		width = -(width & 3);
		for (int y = -height; y < 0; y++) {
			for (int x = minX; x < 0; x++) {
				colour = image[imagePosition++];
				if (colour != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
				colour = image[imagePosition++];

				if (colour != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
				colour = image[imagePosition++];

				if (colour != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
				colour = image[imagePosition++];

				if (colour != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
			}

			for (int k2 = width; k2 < 0; k2++) {
				colour = image[imagePosition++];
				if (colour != 0) {
					raster[rasterPosition++] = colour;
				} else {
					rasterPosition++;
				}
			}

			rasterPosition += rasterOffset;
			imagePosition += imageOffset;
		}
	}

	private void method347(int rasterPosition, int width, int height, int sourceOffset, int sourcePosition, int rasterOffset,
			int[] source, int[] raster) {
		int minX = -(width >> 2);
		width = -(width & 3);
		for (int y = -height; y < 0; y++) {
			for (int x = minX; x < 0; x++) {
				raster[rasterPosition++] = source[sourcePosition++];
				raster[rasterPosition++] = source[sourcePosition++];
				raster[rasterPosition++] = source[sourcePosition++];
				raster[rasterPosition++] = source[sourcePosition++];
			}

			for (int k2 = width; k2 < 0; k2++) {
				raster[rasterPosition++] = source[sourcePosition++];
			}

			rasterPosition += rasterOffset;
			sourcePosition += sourceOffset;
		}
	}

	private void method351(int i, int j, int ai[], int k, int ai1[], int l, int i1, int j1, int alpha, int l1) {
		int j2 = 256 - alpha;
		for (int k2 = -i1; k2 < 0; k2++) {
			for (int l2 = -j; l2 < 0; l2++) {
				k = ai1[i++];
				if (k != 0) {
					int i3 = ai[l1];
					ai[l1++] = ((k & 0xff00ff) * alpha + (i3 & 0xff00ff) * j2 & 0xff00ff00)
							+ ((k & 0xff00) * alpha + (i3 & 0xff00) * j2 & 0xff0000) >> 8;
				} else {
					l1++;
				}
			}

			l1 += j1;
			i += l;
		}
	}

	private void method355(int input[], int width, byte image[], int height, int output[], int in, int l, int offset, int j1,
			int k1) {
		int l1 = -(width >> 2);
		width = -(width & 3);
		for (int y = -height; y < 0; y++) {
			for (int x = l1; x < 0; x++) {
				in = input[k1++];
				if (in != 0 && image[offset] == 0) {
					output[offset++] = in;
				} else {
					offset++;
				}

				in = input[k1++];
				if (in != 0 && image[offset] == 0) {
					output[offset++] = in;
				} else {
					offset++;
				}

				in = input[k1++];
				if (in != 0 && image[offset] == 0) {
					output[offset++] = in;
				} else {
					offset++;
				}

				in = input[k1++];
				if (in != 0 && image[offset] == 0) {
					output[offset++] = in;
				} else {
					offset++;
				}
			}

			for (int l2 = width; l2 < 0; l2++) {
				in = input[k1++];
				if (in != 0 && image[offset] == 0) {
					output[offset++] = in;
				} else {
					offset++;
				}
			}

			offset += l;
			k1 += j1;
		}
	}

}