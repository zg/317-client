package com.jagex.sound;

import com.jagex.io.Buffer;

/*
 * an implementation of a reconfigurable filter that calculates
 * coefficients from pole magnitude/phases and a serial
 * configuration of cascading second-order iir filters
 */
public class Filter {

	// Thanks Super_

	/**
	 * The filter coefficients, where the first dimension is the direction and the second dimension is the pair.
	 */
	static int[][] coefficients = new int[2][8];
	static float forwardMinimisedCoefficientMultiplier; // TODO names
	static int forwardMultiplier;

	static float[][] minimisedCoefficients = new float[2][8];

	int[][][] magnitudes = new int[2][2][4];
	/**
	 * The amount of pairs, where pairs[0] is the amount of feedforward pairs, and pairs[1] is the amount of feedback
	 * pairs.
	 */
	int[] pairs = new int[2];
	int[][][] phases = new int[2][2][4];
	int[] unity = new int[2];

	/* direction: 0 = feedforward, 1 = feedback */
	public int compute(int direction, float step) {
		if (direction == 0) {
			float unity = this.unity[0] + (this.unity[1] - this.unity[0]) * step * 0.003051758F;
			forwardMinimisedCoefficientMultiplier = (float) Math.pow(0.1, unity / 20);
			forwardMultiplier = (int) (forwardMinimisedCoefficientMultiplier * 65536);
		}

		if (pairs[direction] == 0) {
			return 0;
		}

		float initialMagnitude = interpolateMagnitude(direction, 0, step);
		minimisedCoefficients[direction][0] = -2 * initialMagnitude * (float) Math.cos(interpolatePhase(direction, 0, step));
		minimisedCoefficients[direction][1] = initialMagnitude * initialMagnitude;

		for (int pair = 1; pair < pairs[direction]; pair++) {
			float magnitude = interpolateMagnitude(direction, pair, step);
			float f4 = -2 * magnitude * (float) Math.cos(interpolatePhase(direction, pair, step));
			float f5 = magnitude * magnitude;

			minimisedCoefficients[direction][pair * 2 + 1] = minimisedCoefficients[direction][pair * 2 - 1] * f5;
			minimisedCoefficients[direction][pair * 2] = minimisedCoefficients[direction][pair * 2 - 1] * f4
					+ minimisedCoefficients[direction][pair * 2 - 2] * f5;

			for (int j1 = pair * 2 - 1; j1 >= 2; j1--) {
				minimisedCoefficients[direction][j1] += minimisedCoefficients[direction][j1 - 1] * f4
						+ minimisedCoefficients[direction][j1 - 2] * f5;
			}

			minimisedCoefficients[direction][1] += minimisedCoefficients[direction][0] * f4 + f5;
			minimisedCoefficients[direction][0] += f4;
		}

		if (direction == 0) {
			for (int pair = 0; pair < pairs[0] * 2; pair++) {
				minimisedCoefficients[0][pair] *= forwardMinimisedCoefficientMultiplier;
			}
		}

		for (int pair = 0; pair < pairs[direction] * 2; pair++) {
			coefficients[direction][pair] = (int) (minimisedCoefficients[direction][pair] * 65536);
		}

		return pairs[direction] * 2;
	}

	public final void decode(Buffer buffer, Envelope envelope) {
		int count = buffer.readUByte();
		pairs[0] = count >> 4;
		pairs[1] = count & 0xf;

		if (count != 0) {
			unity[0] = buffer.readUShort();
			unity[1] = buffer.readUShort();
			int migration = buffer.readUByte();
			for (int direction = 0; direction < 2; direction++) {
				for (int index = 0; index < pairs[direction]; index++) {
					phases[direction][0][index] = buffer.readUShort();
					magnitudes[direction][0][index] = buffer.readUShort();
				}
			}

			for (int direction = 0; direction < 2; direction++) {
				for (int pair = 0; pair < pairs[direction]; pair++) {
					if ((migration & 1 << direction * 4 << pair) != 0) { // pole/zero migration
						phases[direction][1][pair] = buffer.readUShort();
						magnitudes[direction][1][pair] = buffer.readUShort();
					} else {
						phases[direction][1][pair] = phases[direction][0][pair];
						magnitudes[direction][1][pair] = magnitudes[direction][0][pair];
					}
				}
			}

			if (migration != 0 || unity[1] != unity[0]) {
				envelope.decodeSegments(buffer);
			}
		} else {
			unity[0] = unity[1] = 0;
		}
	}

	/**
	 * Perform precise linear interpolation on the magnitude (where "precise" means that the result is guaranteed to be
	 * the first magnitude ({@code magnitudes[direction][1][pair]}) when the step is {@code 1}).
	 * 
	 * @param direction The direction, where {@code 0} is feedforward, and {@code 1} is feedback.
	 * @param pair The pair.
	 * @param step The step (the interpolation parameter).
	 * @return The interpolated magnitude.
	 */
	private float interpolateMagnitude(int direction, int pair, float step) {
		float magnitude = (magnitudes[direction][0][pair] + step
				* (magnitudes[direction][1][pair] - magnitudes[direction][0][pair]));
		return 1F - (float) Math.pow(10, -(magnitude * 0.001525879) / 20);
	}

	/**
	 * Perform linear interpolation on the phase
	 * 
	 * @param direction The direction, where {@code 0} is feedforward, and {@code 1} is feedback.
	 * @param pair The pair.
	 * @param step The step (the interpolation parameter).
	 * @return The interpolated phase.
	 */
	private float interpolatePhase(int direction, int pair, float step) {
		float phase = phases[direction][0][pair] + step * (phases[direction][1][pair] - phases[direction][0][pair]);
		return normalise(phase * 0.0001220703F);
	}

	private float normalise(float exponent) {
		return (float) (32.7032 * Math.pow(2, exponent) * Math.PI / 11025);
	}

}