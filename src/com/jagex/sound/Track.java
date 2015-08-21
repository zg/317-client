package com.jagex.sound;

import com.jagex.io.Buffer;

public class Track {

	public static int[] delays = new int[5000];
	private static byte[] output;
	private static Buffer riff;
	private static Track[] tracks = new Track[5000];

	public static Buffer data(int loops, int id) {
		Track track = tracks[id];
		return track == null ? null : track.pack(loops);
	}

	public static void load(Buffer buffer) {
		output = new byte[0x6baa8];
		riff = new Buffer(output);
		Synthesizer.init();

		do {
			int id = buffer.readUShort();
			if (id == 65535) {
				return;
			}

			tracks[id] = new Track();
			tracks[id].decode(buffer);
			delays[id] = tracks[id].calculateDelay();
		} while (true);
	}

	private int loopEnd;
	private int loopStart;
	private Synthesizer[] synthesizers = new Synthesizer[10];

	private Track() {

	}

	private final int calculateDelay() {
		int offset = 0x98967f;
		for (int syntheziser = 0; syntheziser < 10; syntheziser++) {
			if (synthesizers[syntheziser] != null && synthesizers[syntheziser].offset / 20 < offset) {
				offset = synthesizers[syntheziser].offset / 20;
			}
		}

		if (loopStart < loopEnd && loopStart / 20 < offset) {
			offset = loopStart / 20;
		}
		if (offset == 0x98967f || offset == 0) {
			return 0;
		}

		for (int synthesizer = 0; synthesizer < 10; synthesizer++) {
			if (synthesizers[synthesizer] != null) {
				synthesizers[synthesizer].offset -= offset * 20;
			}
		}

		if (loopStart < loopEnd) {
			loopStart -= offset * 20;
			loopEnd -= offset * 20;
		}

		return offset;
	}

	private final void decode(Buffer buffer) {
		for (int synthesizer = 0; synthesizer < 10; synthesizer++) {
			int valid = buffer.readUByte();

			if (valid != 0) {
				buffer.setPosition(buffer.getPosition() - 1);
				synthesizers[synthesizer] = new Synthesizer();
				synthesizers[synthesizer].decode(buffer);
			}
		}

		loopStart = buffer.readUShort();
		loopEnd = buffer.readUShort();
	}

	private final int mix(int loops) {
		int duration = 0;
		for (int synthesizer = 0; synthesizer < 10; synthesizer++) {
			if (synthesizers[synthesizer] != null
					&& synthesizers[synthesizer].duration + synthesizers[synthesizer].offset > duration) {
				duration = synthesizers[synthesizer].duration + synthesizers[synthesizer].offset;
			}
		}

		if (duration == 0) {
			return 0;
		}

		int sampleCount = 22050 * duration / 1000;
		int loopStart = 22050 * this.loopStart / 1000;
		int loopEnd = 22050 * this.loopEnd / 1000;
		if (loopStart < 0 || loopStart > sampleCount || loopEnd < 0 || loopEnd > sampleCount || loopStart >= loopEnd) {
			loops = 0;
		}

		int size = sampleCount + (loopEnd - loopStart) * (loops - 1);
		for (int offset = 44; offset < size + 44; offset++) {
			output[offset] = -128;
		}

		for (int synthesizer = 0; synthesizer < 10; synthesizer++) {
			if (synthesizers[synthesizer] != null) {
				int synthDuration = synthesizers[synthesizer].duration * 22050 / 1000;
				int synthOffset = synthesizers[synthesizer].offset * 22050 / 1000;
				int[] samples = synthesizers[synthesizer].synthesize(synthDuration, synthesizers[synthesizer].duration);

				for (int sample = 0; sample < synthDuration; sample++) {
					output[sample + synthOffset + 44] += (byte) (samples[sample] >> 8);
				}
			}
		}

		if (loops > 1) {
			loopStart += 44;
			loopEnd += 44;
			sampleCount += 44;
			int k2 = (size += 44) - sampleCount;
			for (int j3 = sampleCount - 1; j3 >= loopEnd; j3--) {
				output[j3 + k2] = output[j3];
			}

			for (int i = 1; i < loops; i++) {
				int loopDelta = (loopEnd - loopStart) * i;
				for (int j = loopStart; j < loopEnd; j++) {
					output[j + loopDelta] = output[j];
				}
			}

			size -= 44;
		}
		return size;
	}

	private final Buffer pack(int loops) {
		int size = mix(loops);
		riff.setPosition(0);
		riff.writeInt(0x52494646); // RIFF
		riff.writeLEInt(36 + size);
		riff.writeInt(0x57415645); // WAVE
		riff.writeInt(0x666d7420); // "fmt " (quotes not included, note the space)
		riff.writeLEInt(16); // size
		riff.writeLEShort(1); // format
		riff.writeLEShort(1); // channel count
		riff.writeLEInt(22050); // sample rate
		riff.writeLEInt(22050); // byte rate
		riff.writeLEShort(1); // block align
		riff.writeLEShort(8); // bits per sample
		riff.writeInt(0x64617461); // "data"
		riff.writeLEInt(size);
		riff.setPosition(riff.getPosition() + size);
		return riff;
	}

}