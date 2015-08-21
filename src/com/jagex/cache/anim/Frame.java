package com.jagex.cache.anim;

import org.major.cache.anim.FrameConstants;

import com.jagex.io.Buffer;

public class Frame {

	private static Frame[] frames;
	
	private static final int TRANSFORM_X = 0b1;
	private static final int TRANSFORM_Y = 0b10;
	private static final int TRANSFORM_Z = 0b100;
	
	private static boolean[] opaque;

	public static void clearFrames() {
		frames = null;
	}

	public static void init(int size) {
		frames = new Frame[size + 1];

		opaque = new boolean[size + 1];
		for (int index = 0; index < size + 1; index++) {
			opaque[index] = true;
		}
	}

	public static boolean isInvalid(int frame) {
		return frame == -1;
	}

	public static void load(byte[] data) {
		Buffer buffer = new Buffer(data);
		buffer.setPosition(data.length - 8);

		int attributesOffset = buffer.readUShort();
		int translationsOffset = buffer.readUShort();
		int durationsOffset = buffer.readUShort();
		int baseOffset = buffer.readUShort();

		int offset = 0;
		Buffer head = new Buffer(data);
		head.setPosition(offset);

		offset += attributesOffset + 2;
		Buffer attributes = new Buffer(data);
		attributes.setPosition(offset);

		offset += translationsOffset;
		Buffer translations = new Buffer(data);
		translations.setPosition(offset);

		offset += durationsOffset;
		Buffer durations = new Buffer(data);
		durations.setPosition(offset);

		offset += baseOffset;
		Buffer bases = new Buffer(data);
		bases.setPosition(offset);

		FrameBase base = new FrameBase(bases);
		int frameCount = head.readUShort();

		int[] translationIndices = new int[500];
		int[] transformX = new int[500];
		int[] transformY = new int[500];
		int[] transformZ = new int[500];

		for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
			int id = head.readUShort();
			Frame frame = frames[id] = new Frame();
			frame.duration = durations.readUByte();
			frame.base = base;

			int transformations = head.readUByte();
			int lastIndex = -1;
			int transformation = 0;

			for (int index = 0; index < transformations; index++) {
				int attribute = attributes.readUByte();
				if (attribute > 0) {
					if (base.getTransformationType(index) != FrameConstants.CENTROID_TRANSFORMATION) {
						for (int next = index - 1; next > lastIndex; next--) {
							if (base.getTransformationType(next) != FrameConstants.CENTROID_TRANSFORMATION) {
								continue;
							}

							translationIndices[transformation] = next;
							transformX[transformation] = 0;
							transformY[transformation] = 0;
							transformZ[transformation] = 0;
							transformation++;
							break;
						}
					}

					translationIndices[transformation] = index;
					int standard = (base.getTransformationType(index) == FrameConstants.SCALE_TRANSFORMATION) ? 128 : 0;

					transformX[transformation] = ((attribute & TRANSFORM_X) != 0) ? translations.readSmart() : standard;
					transformY[transformation] = ((attribute & TRANSFORM_Y) != 0) ? translations.readSmart() : standard;
					transformZ[transformation] = ((attribute & TRANSFORM_Z) != 0) ? translations.readSmart() : standard;

					lastIndex = index;
					transformation++;

					if (base.getTransformationType(index) == FrameConstants.ALPHA_TRANSFORMATION) {
						opaque[id] = false;
					}
				}
			}

			frame.transformationCount = transformation;
			frame.transformationIndices = new int[transformation];
			frame.transformX = new int[transformation];
			frame.transformY = new int[transformation];
			frame.transformZ = new int[transformation];

			for (int index = 0; index < transformation; index++) {
				frame.transformationIndices[index] = translationIndices[index];
				frame.transformX[index] = transformX[index];
				frame.transformY[index] = transformY[index];
				frame.transformZ[index] = transformZ[index];
			}
		}
	}

	public static Frame lookup(int index) {
		return (frames == null) ? null : frames[index];
	}

	private FrameBase base;
	private int duration;
	private int transformationCount;
	private int[] transformX;
	private int[] transformY;
	private int[] transformZ;
	private int[] transformationIndices;

	/**
	 * Gets the {@link FrameBase} of this Frame.
	 * 
	 * @return The FrameBase.
	 */
	public FrameBase getBase() {
		return base;
	}

	/**
	 * Gets the duration this Frame lasts.
	 * 
	 * @return The duration.
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Gets the amount of transformations in this Frame.
	 * 
	 * @return The amount of transformations.
	 */
	public int getTransformationCount() {
		return transformationCount;
	}

	public int getTransformX(int transformation) {
		return transformX[transformation];
	}

	public int getTransformY(int transformation) {
		return transformY[transformation];
	}

	public int getTransformZ(int transformation) {
		return transformZ[transformation];
	}

	public int getTransformationIndex(int index) {
		return transformationIndices[index];
	}

}