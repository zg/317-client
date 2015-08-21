package com.jagex.setting;

import com.jagex.cache.Archive;
import com.jagex.io.Buffer;

public class VariableParameter {

	public static VariableParameter[] parameters;
	private static int count;

	public static int getCount() {
		return count;
	}

	public static void init(Archive archive) {
		Buffer buffer = new Buffer(archive.getEntry("varp.dat"));
		count = buffer.readUShort();
		if (parameters == null) {
			parameters = new VariableParameter[count];
		}

		for (int id = 0; id < count; id++) {
			if (parameters[id] == null) {
				parameters[id] = new VariableParameter();
			}
			parameters[id].decode(buffer);
		}

		if (buffer.getPosition() != buffer.getPayload().length) {
			System.out.println("varptype load mismatch");
		}
	}

	private int parameter;

	public void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				return;
			}

			if (opcode == 1 || opcode == 2) {
				buffer.readUByte();
			} else if (opcode == 5) {
				parameter = buffer.readUShort();
			} else if (opcode == 7) {
				buffer.readInt();
			} else if (opcode == 10) {
				buffer.readString();
			} else if (opcode == 12) {
				buffer.readInt();
			} else if (opcode != 4 || opcode != 6 || opcode != 8 || opcode != 11 || opcode != 13) {
				System.out.println("Error unrecognised config code: " + opcode);
			}
		} while (true);
	}

	public int getParameter() {
		return parameter;
	}

}