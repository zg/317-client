package com.jagex.cache.anim;

import com.jagex.cache.Archive;
import com.jagex.io.Buffer;

public class Animation {

	public static Animation[] animations;
	private static int count;

	public static int getCount() {
		return count;
	}

	public static void init(Archive archive) {
		Buffer buffer = new Buffer(archive.getEntry("seq.dat"));
		count = buffer.readUShort();

		if (animations == null) {
			animations = new Animation[count];
		}

		for (int id = 0; id < count; id++) {
			if (animations[id] == null) {
				animations[id] = new Animation();
			}

			animations[id].decode(buffer);
		}
	}

	public static void setCount(int count) {
		Animation.count = count;
	}

	/**
	 * The animation precedence (will this animation 'override' other animations or will this one yield).
	 */
	private int animatingPrecedence = -1;

	/**
	 * The duration of each frame in this Animation.
	 */
	private int[] durations;

	/**
	 * The amount of frames in this Animation.
	 */
	private int frameCount;

	private int[] interleaveOrder;

	/**
	 * The amount of frames subtracted to restart the loop.
	 */
	private int loopOffset = -1;

	/**
	 * The maximum times this animation will loop.
	 */
	private int maximumLoops = 99;

	/**
	 * Indicates whether or not this player's shield will be displayed whilst this animation is played.
	 */
	private int playerOffhand = -1;

	/**
	 * Indicates whether or not this player's weapon will be displayed whilst this animation is played.
	 */
	private int playerMainhand = -1;

	/**
	 * The primary frame ids of this Animation.
	 */
	private int[] primaryFrames;

	private int priority = 5;

	private int replayMode = 2;

	/**
	 * The secondary frame ids of this Animation.
	 */
	private int[] secondaryFrames;

	private boolean stretches = false;

	/**
	 * The walking precedence (will the player be prevented from moving or can they continue).
	 */
	private int walkingPrecedence = -1;

	/**
	 * Reads values from the buffer.
	 * 
	 * @param buffer The buffer.
	 */
	public void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				break;
			}

			if (opcode == 1) {
				frameCount = buffer.readUByte();
				primaryFrames = new int[frameCount];
				secondaryFrames = new int[frameCount];
				durations = new int[frameCount];

				for (int frame = 0; frame < frameCount; frame++) {
					primaryFrames[frame] = buffer.readUShort();
					secondaryFrames[frame] = buffer.readUShort();

					if (secondaryFrames[frame] == 65535) {
						secondaryFrames[frame] = -1;
					}

					durations[frame] = buffer.readUShort();
				}
			} else if (opcode == 2) {
				loopOffset = buffer.readUShort();
			} else if (opcode == 3) {
				int count = buffer.readUByte();
				interleaveOrder = new int[count + 1];
				for (int index = 0; index < count; index++) {
					interleaveOrder[index] = buffer.readUByte();
				}

				interleaveOrder[count] = 0x98967f;
			} else if (opcode == 4) {
				stretches = true;
			} else if (opcode == 5) {
				priority = buffer.readUByte();
			} else if (opcode == 6) {
				playerOffhand = buffer.readUShort();
			} else if (opcode == 7) {
				playerMainhand = buffer.readUShort();
			} else if (opcode == 8) {
				maximumLoops = buffer.readUByte();
			} else if (opcode == 9) {
				animatingPrecedence = buffer.readUByte();
			} else if (opcode == 10) {
				walkingPrecedence = buffer.readUByte();
			} else if (opcode == 11) {
				replayMode = buffer.readUByte();
			} else if (opcode == 12) {
				buffer.readInt(); // unused
			} else {
				System.out.println("Error unrecognised seq config code: " + opcode);
			}
		} while (true);

		if (frameCount == 0) {
			frameCount = 1;
			primaryFrames = new int[1];
			primaryFrames[0] = -1;
			secondaryFrames = new int[1];
			secondaryFrames[0] = -1;
			durations = new int[1];
			durations[0] = -1;
		}

		if (animatingPrecedence == -1) {
			animatingPrecedence = (interleaveOrder == null) ? 0 : 2;
		}

		if (walkingPrecedence == -1) {
			walkingPrecedence = (interleaveOrder == null) ? 0 : 2;
		}
	}

	public int duration(int frameId) {
		int duration = durations[frameId];
		if (duration == 0) {
			Frame frame = Frame.lookup(primaryFrames[frameId]);

			if (frame != null) {
				duration = durations[frameId] = frame.getDuration();
			}
		}

		return duration == 0 ? 1 : duration;
	}

	/**
	 * Gets the animation precedence (will this animation 'override' other animations or will this one yield).
	 */
	public int getAnimatingPrecedence() {
		return animatingPrecedence;
	}

	public int[] getDurations() {
		return durations;
	}

	/**
	 * Gets the amount of frames in this Animation.
	 * 
	 * @return The amount of frames.
	 */
	public int getFrameCount() {
		return frameCount;
	}

	public int[] getInterleaveOrder() {
		return interleaveOrder;
	}

	/**
	 * Gets the amount of frames subtracted to restart the loop.
	 * 
	 * @return The loop offset.
	 */
	public int getLoopOffset() {
		return loopOffset;
	}

	/**
	 * Gets the maximum times this animation will loop.
	 * 
	 * @return The maximum loop count.
	 */
	public int getMaximumLoops() {
		return maximumLoops;
	}

	/**
	 * Returns whether or not this player's shield will be displayed whilst this animation is played.
	 */
	public int getPlayerShieldDelta() {
		return playerOffhand;
	}

	/**
	 * Returns whether or not this player's weapon will be displayed whilst this animation is played.
	 */
	public int getPlayerWeaponDelta() {
		return playerMainhand;
	}

	/**
	 * Gets the primary frame ids of this Animation.
	 * 
	 * @return The primary frame ids.
	 */
	public int getPrimaryFrame(int index) {
		return primaryFrames[index];
	}

	/**
	 * Gets the priority of this Animation.
	 * 
	 * @return The priority.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Gets the replay mode of this Animation.
	 * 
	 * @return The replay mode.
	 */
	public int getReplayMode() {
		return replayMode;
	}

	/**
	 * Gets the secondary frame ids of this Animation.
	 * 
	 * @return The secondary frame ids.
	 */
	public int getSecondaryFrame(int index) {
		return secondaryFrames[index];
	}

	/**
	 * Gets the walking precedence (will the player be prevented from moving or can they continue).
	 * 
	 * @return The walking precedence.
	 */
	public int getWalkingPrecedence() {
		return walkingPrecedence;
	}

	public boolean stretches() {
		return stretches;
	}

}