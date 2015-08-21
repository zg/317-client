package com.jagex.cache.anim;

import com.jagex.io.Buffer;

public class FrameBase {

	/**
	 * The amount of transformations.
	 */
	private int count;
	private int[][] groups;

	/**
	 * The type of each transformation.
	 */
	private int[] transformationType;

	public FrameBase(Buffer buffer) {
		count = buffer.readUByte();
		transformationType = new int[count];
		groups = new int[count][];
		for (int index = 0; index < count; index++) {
			transformationType[index] = buffer.readUByte();
		}

		for (int group = 0; group < count; group++) {
			int count = buffer.readUByte();
			groups[group] = new int[count];

			for (int index = 0; index < count; index++) {
				groups[group][index] = buffer.readUByte();
			}
		}
	}

	public int[] getGroups(int group) {
		return groups[group];
	}

	/**
	 * Gets the amount of transformations in this FrameBase.
	 * 
	 * @return The amount of transformations.
	 */
	public int getTransformationCount() {
		return count;
	}

	/**
	 * Gets the transformation type of the transformation at the specified index.
	 * 
	 * @param index The index.
	 * @return The transformation type.
	 */
	public int getTransformationType(int index) {
		return transformationType[index];
	}

}