package com.jagex.entity;

import com.jagex.cache.anim.Animation;

public class Mob extends Renderable {

	// Class30_Sub2_Sub4_Sub1

	public int animationDelay;
	public boolean animationStretches = false;
	public int anInt1503;
	public int anInt1519;
	public int anInt1522;
	public int anInt1542;
	public int currentAnimation;
	public int currentAnimationLoops;
	public int currentHealth;
	public int cycleStatus = -1000;
	public int destinationX;
	public int destinationY;
	public int direction;
	public int displayedEmoteFrames;
	public int displayedMovementFrames;
	public int emoteAnimation = -1;
	public int emoteTimeRemaining;
	public int endForceMovement;
	public int faceX;
	public int faceY;
	public int graphic = -1;
	public int graphicDelay;
	public int graphicHeight;
	public int halfTurnAnimation = -1;
	public int height = 200;
	public int[] hitCycles = new int[4];
	public int[] hitDamages = new int[4];
	public int[] hitTypes = new int[4];
	public int idleAnimation = -1;
	public int initialX;
	public int initialY;
	public int interactingMob = -1;
	public int maximumHealth;
	public int movementAnimation = -1;
	public int nextStepOrientation;
	public int orientation;
	public boolean[] pathRun = new boolean[10];
	public int[] pathX = new int[10];
	public int[] pathY = new int[10];
	public int quarterAnticlockwiseTurnAnimation = -1;
	public int quarterClockwiseTurnAnimation = -1;
	public int remainingPath;
	public int rotation = 32;
	public int runAnimation = -1;
	public int size = 1;
	public String spokenText;
	public int startForceMovement;
	public int textColour;
	public int textCycle = 100;
	public int textEffect;
	public int time;
	public int turnAnimation = -1;
	public int walkingAnimation = -1;
	public int worldX;
	public int worldY;

	public final void damage(int damage, int type, int cycle) {
		for (int index = 0; index < 4; index++) {
			if (hitCycles[index] <= cycle) {
				hitDamages[index] = damage;
				hitTypes[index] = type;
				hitCycles[index] = cycle + 70;
				break;
			}
		}
	}

	public boolean isVisible() {
		return false;
	}

	public final void move(int x, int y, boolean teleported) {
		if (emoteAnimation != -1 && Animation.animations[emoteAnimation].getWalkingPrecedence() == 1) {
			emoteAnimation = -1;
		}

		if (!teleported) {
			int dirX = x - pathX[0];
			int dirY = y - pathY[0];
			if (dirX >= -8 && dirX <= 8 && dirY >= -8 && dirY <= 8) {
				if (remainingPath < 9) {
					remainingPath++;
				}
				for (int i = remainingPath; i > 0; i--) {
					pathX[i] = pathX[i - 1];
					pathY[i] = pathY[i - 1];
					pathRun[i] = pathRun[i - 1];
				}

				pathX[0] = x;
				pathY[0] = y;
				pathRun[0] = false;
				return;
			}
		}

		remainingPath = 0;
		anInt1542 = 0;
		anInt1503 = 0;
		pathX[0] = x;
		pathY[0] = y;
		worldX = pathX[0] << 7 + size << 6;
		worldY = pathY[0] << 7 + size << 6;
	}

	public final void resetPath() {
		remainingPath = 0;
		anInt1542 = 0;
	}

	public final void walk(int direction, boolean run) {
		int x = pathX[0];
		int y = pathY[0];
		if (direction == 0) {
			x--;
			y++;
		}
		if (direction == 1) {
			y++;
		}
		if (direction == 2) {
			x++;
			y++;
		}
		if (direction == 3) {
			x--;
		}
		if (direction == 4) {
			x++;
		}
		if (direction == 5) {
			x--;
			y--;
		}
		if (direction == 6) {
			y--;
		}
		if (direction == 7) {
			x++;
			y--;
		}
		if (emoteAnimation != -1 && Animation.animations[emoteAnimation].getWalkingPrecedence() == 1) {
			emoteAnimation = -1;
		}
		if (remainingPath < 9) {
			remainingPath++;
		}
		for (int i = remainingPath; i > 0; i--) {
			pathX[i] = pathX[i - 1];
			pathY[i] = pathY[i - 1];
			pathRun[i] = pathRun[i - 1];
		}

		pathX[0] = x;
		pathY[0] = y;
		pathRun[0] = run;
	}

}