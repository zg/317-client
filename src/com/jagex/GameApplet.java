package com.jagex;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.jagex.draw.ProducingGraphicsBuffer;

@SuppressWarnings("serial")
public class GameApplet extends Applet implements Runnable, MouseListener, MouseMotionListener, KeyListener, FocusListener,
		WindowListener {

	// Applet_Sub1

	protected boolean debug = false;
	protected int fps;
	protected GameFrame frame;
	protected ProducingGraphicsBuffer frameGraphicsBuffer;
	protected int frameHeight;
	protected int frameWidth;
	protected Graphics graphics;
	protected boolean hasFocus = true;
	protected int[] keyStatuses = new int[128];
	protected int lastClickX;
	protected int lastClickY;
	protected int lastMetaModifier;
	protected long lastMouseClick;
	protected int metaModifierHeld;
	protected int metaModifierPressed;
	protected int minimumSleepTime = 1;
	protected long mouseClickTime;
	protected int mouseEventX;
	protected int mouseEventY;
	protected boolean paintBlack = true;
	protected int pressedX;
	protected int pressedY;
	protected int timeIdle;
	private long[] aLongArray7 = new long[10];
	private int lastProcessedKey;
	private int[] pressedKeys = new int[128];
	private int state;
	private int timeDelta = 20;
	private int unprocessedKeyCount;

	@Override
	public final void destroy() {
		state = -1;
		try {
			Thread.sleep(5000L);
		} catch (Exception ex) {
		}

		if (state == -1) {
			exit();
		}
	}

	public void draw() {
	}

	public void drawLoadingText(int x, String string) {
		while (graphics == null) {
			graphics = getFrame().getGraphics();
			try {
				getFrame().repaint();
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
		}

		Font helvetica1 = new Font("Helvetica", 1, 13);
		FontMetrics font = getFrame().getFontMetrics(helvetica1);
		Font helvetica2 = new Font("Helvetica", 0, 13);
		getFrame().getFontMetrics(helvetica2);
		if (paintBlack) {
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, frameWidth, frameHeight);
			paintBlack = false;
		}
		Color color = new Color(140, 17, 17);
		int y = frameHeight / 2 - 18;
		graphics.setColor(color);
		graphics.drawRect(frameWidth / 2 - 152, y, 304, 34);
		graphics.fillRect(frameWidth / 2 - 150, y + 2, x * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect(frameWidth / 2 - 150 + x * 3, y + 2, 300 - x * 3, 30);
		graphics.setFont(helvetica1);
		graphics.setColor(Color.white);
		graphics.drawString(string, (frameWidth - font.stringWidth(string)) / 2, y + 22);
	}

	public final void exit() {
		state = -2;
		shutdown();
		if (frame != null) {
			try {
				Thread.sleep(1000L);
				System.exit(0);
			} catch (Throwable _ex) {
			}
		}
	}

	@Override
	public final void focusGained(FocusEvent focusevent) {
		hasFocus = true;
		paintBlack = true;
		method10();
	}

	@Override
	public final void focusLost(FocusEvent focusevent) {
		hasFocus = false;
		for (int i = 0; i < 128; i++) {
			keyStatuses[i] = 0;
		}
	}

	public int getFps() {
		return fps;
	}

	public Component getFrame() {
		if (frame != null) {
			return frame;
		}
		return this;
	}

	public ProducingGraphicsBuffer getFrameGraphicsBuffer() {
		return frameGraphicsBuffer;
	}

	public int getFrameHeight() {
		return frameHeight;
	}

	public int getFrameWidth() {
		return frameWidth;
	}

	public GameFrame getGameFrame() {
		return frame;
	}

	@Override
	public Graphics getGraphics() {
		return graphics;
	}

	public int[] getKeyStatuses() {
		return keyStatuses;
	}

	public int getLastClickX() {
		return lastClickX;
	}

	public int getLastClickY() {
		return lastClickY;
	}

	public int getLastMetaModifier() {
		return lastMetaModifier;
	}

	public long getLastMouseClick() {
		return lastMouseClick;
	}

	public int getMetaModifierHeld() {
		return metaModifierHeld;
	}

	public int getMetaModifierPressed() {
		return metaModifierPressed;
	}

	public int getMinimumSleepTime() {
		return minimumSleepTime;
	}

	public long getMouseClickTime() {
		return mouseClickTime;
	}

	public int getMouseEventX() {
		return mouseEventX;
	}

	public int getMouseEventY() {
		return mouseEventY;
	}

	public int getPressedX() {
		return pressedX;
	}

	public int getPressedY() {
		return pressedY;
	}

	public int getTimeIdle() {
		return timeIdle;
	}

	public final void initFrame(int height, int width) {
		frameWidth = width;
		frameHeight = height;
		frame = new GameFrame(this, frameWidth, frameHeight);
		graphics = getFrame().getGraphics();
		frameGraphicsBuffer = new ProducingGraphicsBuffer(getFrame(), frameWidth, frameHeight);
		startRunnable(this, 1);
		return;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isHasFocus() {
		return hasFocus;
	}

	public boolean isPaintBlack() {
		return paintBlack;
	}

	@Override
	public final void keyPressed(KeyEvent keyevent) {
		timeIdle = 0;
		int keyCode = keyevent.getKeyCode();
		int keyChar = keyevent.getKeyChar();
		if (keyChar < 30) {
			keyChar = 0;
		}
		if (keyCode == 37) {
			keyChar = 1;
		}
		if (keyCode == 39) {
			keyChar = 2;
		}
		if (keyCode == 38) {
			keyChar = 3;
		}
		if (keyCode == 40) {
			keyChar = 4;
		}
		if (keyCode == 17) {
			keyChar = 5;
		}
		if (keyCode == 8) {
			keyChar = 8;
		}
		if (keyCode == 127) {
			keyChar = 8;
		}
		if (keyCode == 9) {
			keyChar = 9;
		}
		if (keyCode == 10) {
			keyChar = 10;
		}
		if (keyCode >= 112 && keyCode <= 123) {
			keyChar = 1008 + keyCode - 112;
		}
		if (keyCode == 36) {
			keyChar = 1000;
		}
		if (keyCode == 35) {
			keyChar = 1001;
		}
		if (keyCode == 33) {
			keyChar = 1002;
		}
		if (keyCode == 34) {
			keyChar = 1003;
		}
		if (keyChar > 0 && keyChar < 128) {
			keyStatuses[keyChar] = 1;
		}
		if (keyChar > 4) {
			pressedKeys[unprocessedKeyCount] = keyChar;
			unprocessedKeyCount = unprocessedKeyCount + 1 & 0x7f;
		}
	}

	@Override
	public final void keyReleased(KeyEvent event) {
		timeIdle = 0;
		int keyCode = event.getKeyCode();
		char keyChar = event.getKeyChar();

		if (keyChar < '\036') {
			keyChar = '\0';
		}

		if (keyCode == 37) {
			keyChar = '\001';
		} else if (keyCode == 39) {
			keyChar = '\002';
		} else if (keyCode == 38) {
			keyChar = '\003';
		} else if (keyCode == 40) {
			keyChar = '\004';
		} else if (keyCode == 17) {
			keyChar = '\005';
		} else if (keyCode == 8) {
			keyChar = '\b';
		} else if (keyCode == 127) {
			keyChar = '\b';
		} else if (keyCode == 9) {
			keyChar = '\t';
		} else if (keyCode == 10) {
			keyChar = '\n';
		}

		if (keyChar > 0 && keyChar < '\200') {
			keyStatuses[keyChar] = 0;
		}
	}

	@Override
	public final void keyTyped(KeyEvent event) {
	}

	public void load() {
	}

	public void method10() {
	}

	@Override
	public final void mouseClicked(MouseEvent event) {
	}

	@Override
	public final void mouseDragged(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		if (frame != null) {
			x -= 4;
			y -= 22;
		}

		timeIdle = 0;
		mouseEventX = x;
		mouseEventY = y;
	}

	@Override
	public final void mouseEntered(MouseEvent event) {
	}

	@Override
	public final void mouseExited(MouseEvent event) {
		timeIdle = 0;
		mouseEventX = -1;
		mouseEventY = -1;
	}

	@Override
	public final void mouseMoved(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		if (frame != null) {
			x -= 4;
			y -= 22;
		}

		timeIdle = 0;
		mouseEventX = x;
		mouseEventY = y;
	}

	@Override
	public final void mousePressed(MouseEvent event) {
		int x = event.getX();
		int y = event.getY();
		if (frame != null) {
			x -= 4;
			y -= 22;
		}

		timeIdle = 0;
		pressedX = x;
		pressedY = y;
		mouseClickTime = System.currentTimeMillis();

		if (event.isMetaDown()) {
			metaModifierPressed = 2;
			metaModifierHeld = 2;
		} else {
			metaModifierPressed = 1;
			metaModifierHeld = 1;
		}
	}

	@Override
	public final void mouseReleased(MouseEvent event) {
		timeIdle = 0;
		metaModifierHeld = 0;
	}

	public final int nextPressedKey() {
		int key = -1;
		if (unprocessedKeyCount != lastProcessedKey) {
			key = pressedKeys[lastProcessedKey];
			lastProcessedKey = lastProcessedKey + 1 & 0x7f;
		}

		return key;
	}

	@Override
	public final void paint(Graphics graphics) {
		if (this.graphics == null) {
			this.graphics = graphics;
		}

		paintBlack = true;
		method10();
	}

	public void pulse() {
	}

	public final void resetTimeDelta() {
		timeDelta = 1000;
	}

	@Override
	public void run() {
		getFrame().addMouseListener(this);
		getFrame().addMouseMotionListener(this);
		getFrame().addKeyListener(this);
		getFrame().addFocusListener(this);

		if (frame != null) {
			frame.addWindowListener(this);
		}

		drawLoadingText(0, "Loading...");
		load();

		int index = 0;
		int step = 256;
		int sleep = 1;
		int exceptions = 0;
		for (int k1 = 0; k1 < 10; k1++) {
			aLongArray7[k1] = System.currentTimeMillis();
		}

		while (state >= 0) {
			if (state > 0) {
				state--;

				if (state == 0) {
					exit();
					return;
				}
			}

			int initialStep = step;
			int initialSleep = sleep;
			step = 300;
			sleep = 1;
			long time = System.currentTimeMillis();

			if (aLongArray7[index] == 0L) {
				step = initialStep;
				sleep = initialSleep;
			} else if (time > aLongArray7[index]) {
				step = (int) ((2560 * timeDelta) / (time - aLongArray7[index]));
			}

			if (step < 25) {
				step = 25;
			} else if (step > 256) {
				step = 256;
				sleep = (int) (timeDelta - (time - aLongArray7[index]) / 10L);
			}

			if (sleep > timeDelta) {
				sleep = timeDelta;
			}

			aLongArray7[index] = time;
			index = (index + 1) % 10;
			if (sleep > 1) {
				for (int k2 = 0; k2 < 10; k2++) {
					if (aLongArray7[k2] != 0L) {
						aLongArray7[k2] += sleep;
					}
				}
			}

			if (sleep < minimumSleepTime) {
				sleep = minimumSleepTime;
			}

			try {
				Thread.sleep(sleep);
			} catch (InterruptedException ex) {
				exceptions++;
			}

			int iterations = 0;
			for (; iterations < 256; iterations += step) {
				lastMetaModifier = metaModifierPressed;
				lastClickX = pressedX;
				lastClickY = pressedY;
				lastMouseClick = mouseClickTime;
				metaModifierPressed = 0;
				pulse();
				lastProcessedKey = unprocessedKeyCount;
			}

			iterations &= 0xff;
			if (timeDelta > 0) {
				fps = 1000 * step / (timeDelta * 256);
			}

			draw();
			if (debug) {
				System.out.println("ntime:" + time);
				for (int l2 = 0; l2 < 10; l2++) {
					int i3 = (index - l2 - 1 + 20) % 10;
					System.out.println("otim" + i3 + ":" + aLongArray7[i3]);
				}

				System.out.println("fps:" + fps + " ratio:" + step + " count:" + iterations);
				System.out.println("del:" + sleep + " deltime:" + timeDelta + " mindel:" + minimumSleepTime);
				System.out.println("intex:" + exceptions + " opos:" + index);
				debug = false;
				exceptions = 0;
			}
		}
		if (state == -1) {
			exit();
		}
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public void setFrame(GameFrame frame) {
		this.frame = frame;
	}

	public void setFrameGraphicsBuffer(ProducingGraphicsBuffer frameGraphicsBuffer) {
		this.frameGraphicsBuffer = frameGraphicsBuffer;
	}

	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	public void setGraphics(Graphics graphics) {
		this.graphics = graphics;
	}

	public void setHasFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

	public void setKeyStatuses(int[] keyStatuses) {
		this.keyStatuses = keyStatuses;
	}

	public void setLastClickX(int lastClickX) {
		this.lastClickX = lastClickX;
	}

	public void setLastClickY(int lastClickY) {
		this.lastClickY = lastClickY;
	}

	public void setLastMetaModifier(int lastMetaModifier) {
		this.lastMetaModifier = lastMetaModifier;
	}

	public void setLastMouseClick(long lastMouseClick) {
		this.lastMouseClick = lastMouseClick;
	}

	public void setMetaModifierHeld(int metaModifierHeld) {
		this.metaModifierHeld = metaModifierHeld;
	}

	public void setMetaModifierPressed(int metaModifierPressed) {
		this.metaModifierPressed = metaModifierPressed;
	}

	public void setMinimumSleepTime(int minimumSleepTime) {
		this.minimumSleepTime = minimumSleepTime;
	}

	public void setMouseClickTime(long mouseClickTime) {
		this.mouseClickTime = mouseClickTime;
	}

	public void setMouseEventX(int mouseEventX) {
		this.mouseEventX = mouseEventX;
	}

	public void setMouseEventY(int mouseEventY) {
		this.mouseEventY = mouseEventY;
	}

	public void setPaintBlack(boolean paintBlack) {
		this.paintBlack = paintBlack;
	}

	public void setPressedX(int pressedX) {
		this.pressedX = pressedX;
	}

	public void setPressedY(int pressedY) {
		this.pressedY = pressedY;
	}

	public void setTimeIdle(int timeIdle) {
		this.timeIdle = timeIdle;
	}

	public void shutdown() {
	}

	@Override
	public final void start() {
		if (state >= 0) {
			state = 0;
		}
	}

	public final void startApplet(int height, int width) {
		frameWidth = width;
		frameHeight = height;
		graphics = getFrame().getGraphics();
		frameGraphicsBuffer = new ProducingGraphicsBuffer(getFrame(), frameWidth, frameHeight);
		startRunnable(this, 1);
	}

	public void startRunnable(Runnable runnable, int priority) {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.setPriority(priority);
	}

	@Override
	public final void stop() {
		if (state >= 0) {
			state = 4000 / timeDelta;
		}
	}

	@Override
	public final void update(Graphics graphics) {
		if (this.graphics == null) {
			this.graphics = graphics;
		}
		paintBlack = true;
		method10();
	}

	@Override
	public final void windowActivated(WindowEvent event) {
	}

	@Override
	public final void windowClosed(WindowEvent event) {
	}

	@Override
	public final void windowClosing(WindowEvent event) {
		destroy();
	}

	@Override
	public final void windowDeactivated(WindowEvent event) {
	}

	@Override
	public final void windowDeiconified(WindowEvent event) {
	}

	@Override
	public final void windowIconified(WindowEvent event) {
	}

	@Override
	public final void windowOpened(WindowEvent event) {
	}

}