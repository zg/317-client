package com.jagex.sound;

import com.jagex.io.Buffer;

/*
 * an implementation of a wavetable synthesizer that
 * generates square/sine/saw/noise/flat tables
 * and supports phase and amplitude modulation
 *
 * more: http://musicdsp.org/files/Wavetable-101.pdf
 */
/**
 * A wavetable synthesiser that generates square/sine/saw/noise/flat tables and supports phase and amplitude modulation.
 *
 * more: http://musicdsp.org/files/Wavetable-101.pdf
 */
public class Synthesizer {

	private static int[] delays = new int[5];
	private static int[] NOISE;
	private static int[] phases = new int[5];
	private static int[] pitchBaseSteps = new int[5];
	private static int[] pitchSteps = new int[5];
	private static int[] samples = new int[0x35d54];
	private static int[] SINE;
	private static int[] volumeSteps = new int[5];

	public static void init() {
		NOISE = new int[32768];
		for (int index = 0; index < 32768; index++) {
			NOISE[index] = Math.random() > 0.5D ? 1 : 0;
		}

		SINE = new int[32768];
		for (int index = 0; index < 32768; index++) {
			SINE[index] = (int) (Math.sin(index / 5215.1903) * 16384);
		}
	}

	int duration = 500;
	int offset;

	/**
	 * The envelope that modifies the attack time (i.e. time taken for initial run-up of level from nil to peak,
	 * beginning when the key is first pressed).
	 */
	private Envelope attack;
	private int delayDecay = 100;

	private int delayTime;

	private Filter filter;

	private Envelope filterEnvelope;
	private int[] oscillatorDelays = new int[5];
	private int[] oscillatorPitchShift = new int[5];
	private int[] oscillatorVolume = new int[5];
	private Envelope pitch;
	private Envelope pitchModifier;
	private Envelope pitchModifierAmplitude;

	/**
	 * The envelope to modify the release time (i.e. time taken for the level to decay from the sustain level to zero
	 * after the key is released).
	 */
	private Envelope release;
	private Envelope volume;
	private Envelope volumeMultiplier;
	private Envelope volumeMultiplierAmplitude;

	public final void decode(Buffer buffer) {
		pitch = new Envelope();
		pitch.decode(buffer);
		volume = new Envelope();
		volume.decode(buffer);

		int option = buffer.readUByte();
		if (option != 0) {
			buffer.setPosition(buffer.getPosition() - 1);
			pitchModifier = new Envelope();
			pitchModifier.decode(buffer);
			pitchModifierAmplitude = new Envelope();
			pitchModifierAmplitude.decode(buffer);
		}

		option = buffer.readUByte();
		if (option != 0) {
			buffer.setPosition(buffer.getPosition() - 1);
			volumeMultiplier = new Envelope();
			volumeMultiplier.decode(buffer);
			volumeMultiplierAmplitude = new Envelope();
			volumeMultiplierAmplitude.decode(buffer);
		}

		option = buffer.readUByte();
		if (option != 0) {
			buffer.setPosition(buffer.getPosition() - 1);
			release = new Envelope();
			release.decode(buffer);
			attack = new Envelope();
			attack.decode(buffer);
		}

		for (int index = 0; index < 10; index++) {
			int volume = buffer.readUSmart();
			if (volume == 0) {
				break;
			}
			oscillatorVolume[index] = volume;
			oscillatorPitchShift[index] = buffer.readSmart();
			oscillatorDelays[index] = buffer.readUSmart();
		}

		delayTime = buffer.readUSmart();
		delayDecay = buffer.readUSmart();
		duration = buffer.readUShort();
		offset = buffer.readUShort();
		filter = new Filter();
		filterEnvelope = new Envelope();
		filter.decode(buffer, filterEnvelope);
	}

	public final int[] synthesize(int sampleCount, int duration) {
		for (int i = 0; i < sampleCount; i++) {
			samples[i] = 0;
		}

		if (duration < 10) {
			return samples;
		}

		double samplesPerStep = (double) sampleCount / duration;
		pitch.reset();
		volume.reset();

		int pitchMultiplierStep = 0;
		int pitchModifierBaseStep = 0;
		int pitchModifierPhase = 0;
		if (pitchModifier != null) {
			pitchModifier.reset();
			pitchModifierAmplitude.reset();
			pitchMultiplierStep = (int) ((pitchModifier.end - pitchModifier.start) * 32.768D / samplesPerStep);
			pitchModifierBaseStep = (int) (pitchModifier.start * 32.768D / samplesPerStep);
		}

		int volumeMultiplierStep = 0;
		int volumeMultiplierBaseStep = 0;
		int volumeMultiplierPhase = 0;
		if (volumeMultiplier != null) {
			volumeMultiplier.reset();
			volumeMultiplierAmplitude.reset();
			volumeMultiplierStep = (int) ((volumeMultiplier.end - volumeMultiplier.start) * 32.768D / samplesPerStep);
			volumeMultiplierBaseStep = (int) (volumeMultiplier.start * 32.768D / samplesPerStep);
		}

		for (int index = 0; index < 5; index++) {
			if (oscillatorVolume[index] != 0) {
				phases[index] = 0;
				delays[index] = (int) (oscillatorDelays[index] * samplesPerStep);
				volumeSteps[index] = (oscillatorVolume[index] << 14) / 100;
				pitchSteps[index] = (int) ((pitch.end - pitch.start) * 32.768D
						* Math.pow(1.0057929410678534D, oscillatorPitchShift[index]) / samplesPerStep);
				pitchBaseSteps[index] = (int) (pitch.start * 32.768D / samplesPerStep);
			}
		}

		for (int sample = 0; sample < sampleCount; sample++) {
			int pitchChange = pitch.step(sampleCount);
			int volumeChange = volume.step(sampleCount);

			if (pitchModifier != null) {
				int modifier = pitchModifier.step(sampleCount);
				int ampModifier = pitchModifierAmplitude.step(sampleCount);
				pitchChange += evaluateWave(ampModifier, pitchModifierPhase, pitchModifier.form) >> 1;
				pitchModifierPhase += (modifier * pitchMultiplierStep >> 16) + pitchModifierBaseStep;
			}

			if (volumeMultiplier != null) {
				int multiplier = volumeMultiplier.step(sampleCount);
				int ampMultiplier = volumeMultiplierAmplitude.step(sampleCount);
				volumeChange = volumeChange
						* ((evaluateWave(ampMultiplier, volumeMultiplierPhase, volumeMultiplier.form) >> 1) + 32768) >> 15;
				volumeMultiplierPhase += (multiplier * volumeMultiplierStep >> 16) + volumeMultiplierBaseStep;
			}

			for (int delay = 0; delay < 5; delay++) {
				if (oscillatorVolume[delay] != 0) {
					int id = sample + delays[delay];
					if (id < sampleCount) {
						samples[id] += evaluateWave(volumeChange * volumeSteps[delay] >> 15, phases[delay], pitch.form);
						phases[delay] += (pitchChange * pitchSteps[delay] >> 16) + pitchBaseSteps[delay];
					}
				}
			}
		}

		if (release != null) { // Gating effect - http://en.wikipedia.org/wiki/Noise_gate
			release.reset();
			attack.reset();
			int counter = 0;
			boolean muted = true;
			for (int sample = 0; sample < sampleCount; sample++) {
				int on = release.step(sampleCount);
				int off = attack.step(sampleCount);
				int threshold = release.start + ((release.end - release.start) * (muted ? on : off) >> 8);

				if ((counter += 256) >= threshold) {
					counter = 0;
					muted = !muted;
				}

				if (muted) {
					samples[sample] = 0;
				}
			}
		}

		if (delayTime > 0 && delayDecay > 0) { // delay effect
			int delay = (int) (delayTime * samplesPerStep);
			for (int index = delay; index < sampleCount; index++) {
				samples[index] += samples[index - delay] * delayDecay / 100;
			}
		}

		if (filter.pairs[0] > 0 || filter.pairs[1] > 0) { // Filter process
			filterEnvelope.reset();
			int change = filterEnvelope.step(sampleCount + 1); // http://en.wikipedia.org/wiki/IIR_filter
			int forwardOrder = filter.compute(0, change / 65536F);
			int backOrder = filter.compute(1, change / 65536F);

			if (sampleCount >= forwardOrder + backOrder) {
				int index = 0;
				int delay = backOrder;

				if (delay > sampleCount - forwardOrder) {
					delay = sampleCount - forwardOrder;
				}

				for (; index < delay; index++) {
					int sample = samples[index + forwardOrder] * Filter.forwardMultiplier >> 16;
					for (int k8 = 0; k8 < forwardOrder; k8++) {
						sample += samples[index + forwardOrder - 1 - k8] * Filter.coefficients[0][k8] >> 16;
					}

					for (int j9 = 0; j9 < index; j9++) {
						sample -= samples[index - 1 - j9] * Filter.coefficients[1][j9] >> 16;
					}

					samples[index] = sample;
					change = filterEnvelope.step(sampleCount + 1);
				}

				delay = 128;
				do {
					if (delay > sampleCount - forwardOrder) {
						delay = sampleCount - forwardOrder;
					}
					for (; index < delay; index++) {
						int sample = samples[index + forwardOrder] * Filter.forwardMultiplier >> 16;
						for (int i = 0; i < forwardOrder; i++) {
							sample += samples[index + forwardOrder - 1 - i] * Filter.coefficients[0][i] >> 16;
						}

						for (int i = 0; i < backOrder; i++) {
							sample -= samples[index - 1 - i] * Filter.coefficients[1][i] >> 16;
						}

						samples[index] = sample;
						change = filterEnvelope.step(sampleCount + 1);
					}

					if (index >= sampleCount - forwardOrder) {
						break;
					}
					forwardOrder = filter.compute(0, change / 65536F);
					backOrder = filter.compute(1, change / 65536F);
					delay += 128;
				} while (true);
				for (; index < sampleCount; index++) {
					int sample = 0;
					for (int i = index + forwardOrder - sampleCount; i < forwardOrder; i++) {
						sample += samples[index + forwardOrder - 1 - i] * Filter.coefficients[0][i] >> 16;
					}

					for (int i = 0; i < backOrder; i++) {
						sample -= samples[index - 1 - i] * Filter.coefficients[1][i] >> 16;
					}

					samples[index] = sample;
					filterEnvelope.step(sampleCount + 1);
				}
			}
		}

		for (int sample = 0; sample < sampleCount; sample++) { // clamp
			if (samples[sample] < Short.MIN_VALUE) {
				samples[sample] = Short.MIN_VALUE;
			} else if (samples[sample] > Short.MAX_VALUE) {
				samples[sample] = Short.MAX_VALUE;
			}
		}

		return samples;
	}

	private final int evaluateWave(int amplitude, int phase, int table) {
		if (table == 1) {
			if ((phase & 0x7fff) < 16384) {
				return amplitude;
			}
			return -amplitude;
		} else if (table == 2) {
			return SINE[phase & 0x7fff] * amplitude >> 14;
		} else if (table == 3) {
			return ((phase & 0x7fff) * amplitude >> 14) - amplitude;
		} else if (table == 4) {
			return NOISE[phase / 2607 & 0x7fff] * amplitude;
		}
		return 0;
	}

}