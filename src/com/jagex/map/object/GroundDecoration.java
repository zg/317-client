package com.jagex.map.object;

import com.jagex.entity.Renderable;

public final class GroundDecoration {

	/**
	 * A packed config value containing the type and orientation of this decoration, in the form
	 * {@code (orientation << 6) | type}.
	 */
	private byte config; // (orientation << 6) | type

	/**
	 * The draw height of this decoration.
	 */
	private int height;

	/**
	 * The key of this decoration.
	 */
	private int key; // 0x4_000_0000 | (id << 14) | y << 7 | x (and if it's not interactive, | 0x8_000_000)

	/**
	 * The renderable of this decoration.
	 */
	private Renderable renderable;

	/**
	 * The x coordinate of this decoration.
	 */
	private int x;

	/**
	 * The y coordinate of this decoration.
	 */
	private int y;

	/**
	 * A packed config value containing the type and orientation of this decoration, in the form
	 * {@code (orientation << 6) | type}.
	 */
	public byte getConfig() {
		return config;
	}

	/**
	 * The draw height of this decoration.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * The key of this decoration.
	 */
	public int getKey() {
		return key;
	}

	/**
	 * The renderable of this decoration.
	 */
	public Renderable getRenderable() {
		return renderable;
	}

	/**
	 * The x coordinate of this decoration.
	 */
	public int getX() {
		return x;
	}

	/**
	 * The y coordinate of this decoration.
	 */
	public int getY() {
		return y;
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