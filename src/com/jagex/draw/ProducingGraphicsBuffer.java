package com.jagex.draw;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

public final class ProducingGraphicsBuffer implements ImageProducer, ImageObserver {

	private ImageConsumer consumer;
	private int height;
	private Image image;
	private ColorModel model;
	private int[] pixels;
	private int width;

	public ProducingGraphicsBuffer(Component component, int width, int height) {
		this.width = width;
		this.height = height;

		pixels = new int[width * height];
		model = new DirectColorModel(32, 0xFF0000, 65280, 255);
		image = component.createImage(this);

		setPixels();
		component.prepareImage(image, this);
		setPixels();
		component.prepareImage(image, this);
		setPixels();
		component.prepareImage(image, this);
		initializeRasterizer();
	}

	@Override
	public synchronized void addConsumer(ImageConsumer consumer) {
		this.consumer = consumer;
		consumer.setDimensions(width, height);
		consumer.setProperties(null);
		consumer.setColorModel(model);
		consumer.setHints(14);
	}

	public void drawImage(Graphics graphics, int x, int y) {
		setPixels();
		graphics.drawImage(image, x, y, this);
	}

	public int getHeight() {
		return height;
	}

	public Image getImage() {
		return image;
	}

	public int[] getPixels() {
		return pixels;
	}
	
	public void setPixel(int index, int colour){
		pixels[index] = colour;
	}
	
	public int getPixel(int index){
		return pixels[index];
	}

	public int getWidth() {
		return width;
	}

	@Override
	public boolean imageUpdate(Image image, int flags, int x, int y, int width, int height) {
		return true;
	}

	public void initializeRasterizer() {
		Raster.init(height, width, pixels);
	}

	@Override
	public synchronized boolean isConsumer(ImageConsumer consumer) {
		return this.consumer == consumer;
	}

	@Override
	public synchronized void removeConsumer(ImageConsumer consumer) {
		if (this.consumer == consumer) {
			this.consumer = null;
		}
	}

	@Override
	public void requestTopDownLeftRightResend(ImageConsumer consumer) {
		System.out.println("TDLR");
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public synchronized void setPixels() {
		if (consumer == null) {
			return;
		}

		consumer.setPixels(0, 0, width, height, model, pixels, 0, width);
		consumer.imageComplete(2);
	}

	public void setPixels(int[] pixels) {
		this.pixels = pixels;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void startProduction(ImageConsumer consumer) {
		addConsumer(consumer);
	}

}