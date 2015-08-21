package com.jagex;

import java.awt.Frame;
import java.awt.Graphics;

/**
 * A custom {@link Frame} used to draw the game in.
 */
@SuppressWarnings("serial")
public final class GameFrame extends Frame {

	// Frame_Sub1

	private GameApplet applet;

	public GameFrame(GameApplet applet, int width, int height) {
		this.applet = applet;

		setTitle("Jagex");
		setResizable(false);
		setVisible(true);
		toFront();
		setSize(width + 8, height + 28);
	}

	@Override
	public Graphics getGraphics() {
		Graphics graphics = super.getGraphics();
		graphics.translate(4, 24);
		return graphics;
	}

	@Override
	public final void paint(Graphics graphics) {
		applet.paint(graphics);
	}

	@Override
	public final void update(Graphics graphics) {
		applet.update(graphics);
	}

}