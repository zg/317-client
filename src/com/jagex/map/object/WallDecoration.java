package com.jagex.map.object;

import com.jagex.entity.Renderable;

public final class WallDecoration {

	private int attributes;

	private byte config;
	private int height;
	private int key;
	private int orientation;
	private Renderable renderable;
	private int x;
	private int y;

	public int getAttributes() {
		return attributes;
	}

	public byte getConfig() {
		return config;
	}

	public int getHeight() {
		return height;
	}

	public int getKey() {
		return key;
	}

	public int getOrientation() {
		return orientation;
	}

	public Renderable getRenderable() {
		return renderable;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setAttributes(int attributes) {
		this.attributes = attributes;
	}

	public void setConfig(byte config) {
		this.config = config;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	public void setRenderable(Renderable renderable) {
		this.renderable = renderable;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
}