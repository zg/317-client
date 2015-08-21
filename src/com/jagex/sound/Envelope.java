package com.jagex.sound;

import com.jagex.io.Buffer;

/**
 * A simple envelope generator to control a variety of parameters (such as attack and release).
 */
public class Envelope {

	int end;
	int form;
	int start;
	private int amplitude;
	private int[] durations;
	private int[] peaks;
	private int segmentIndex;
	private int segments;
	private int step;
	private int threshold;
	private int ticks;

	public final void decode(Buffer buffer) {
		form = buffer.readUByte();
		start = buffer.readInt();
		end = buffer.readInt();
		decodeSegments(buffer);
	}

	/**
	 * Decodes the segment data from the specified {@link Buffer}.
	 * 
	 * @param buffer The buffer.
	 */
	public final void decodeSegments(Buffer buffer) {
		segments = buffer.readUByte();
		durations = new int[segments];
		peaks = new int[segments];
		for (int index = 0; index < segments; index++) {
			durations[index] = buffer.readUShort();
			peaks[index] = buffer.readUShort();
		}
	}

	/**
	 * Resets this envelope.
	 */
	final void reset() {
		threshold = 0;
		segmentIndex = 0;
		step = 0;
		amplitude = 0;
		ticks = 0;
	}

	/**
	 * Proceeds to the next step of the envelope,
	 * 
	 * @param period The current period.
	 * @return The change.
	 */
	final int step(int period) {
		if (ticks >= threshold) {
			amplitude = peaks[segmentIndex++] << 15;
			if (segmentIndex >= segments) {
				segmentIndex = segments - 1;
			}

			threshold = (int) (durations[segmentIndex] / 65536D * period);
			if (threshold > ticks) {
				step = ((peaks[segmentIndex] << 15) - amplitude) / (threshold - ticks);
			}
		}

		amplitude += step;
		ticks++;
		return amplitude - step >> 15;
	}

}