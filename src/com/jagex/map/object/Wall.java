package com.jagex.map.object;

import com.jagex.entity.Renderable;

public final class Wall {

	public int anInt276;

	public int anInt277;
	private byte config;
	private int height;
	private int key;
	private int positionX;
	private int positionY;
	private Renderable primary;
	private Renderable secondary;

	/**
	 * Gets the secondary.
	 *
	 * @return The secondary.
	 */
	public Renderable getSecondary() {
		return secondary;
	}

	/**
	 * Sets the secondary.
	 *
	 * @param secondary The secondary.
	 */
	public void setSecondary(Renderable secondary) {
		this.secondary = secondary;
	}

	/**
	 * Gets the primary.
	 *
	 * @return The primary.
	 */
	public Renderable getPrimary() {
		return primary;
	}

	/**
	 * Sets the primary.
	 *
	 * @param primary The primary.
	 */
	public void setPrimary(Renderable primary) {
		this.primary = primary;
	}

	/**
	 * Gets the positionY.
	 *
	 * @return The positionY.
	 */
	public int getPositionY() {
		return positionY;
	}

	/**
	 * Sets the positionY.
	 *
	 * @param positionY The positionY.
	 */
	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	/**
	 * Gets the positionX.
	 *
	 * @return The positionX.
	 */
	public int getPositionX() {
		return positionX;
	}

	/**
	 * Sets the positionX.
	 *
	 * @param positionX The positionX.
	 */
	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	/**
	 * Gets the height.
	 *
	 * @return The height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height.
	 *
	 * @param height The height.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Gets the key.
	 *
	 * @return The key.
	 */
	public int getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key The key.
	 */
	public void setKey(int key) {
		this.key = key;
	}

	/**
	 * Gets the config.
	 *
	 * @return The config.
	 */
	public byte getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 *
	 * @param config The config.
	 */
	public void setConfig(byte config) {
		this.config = config;
	}

}