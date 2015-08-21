package com.jagex.entity.model;

import com.jagex.cache.Archive;
import com.jagex.io.Buffer;

public class IdentityKit {

	public static int count;
	public static IdentityKit[] kits;

	public static void init(Archive archive) {
		Buffer buffer = new Buffer(archive.getEntry("idk.dat"));
		count = buffer.readUShort();
		if (kits == null) {
			kits = new IdentityKit[count];
		}

		for (int id = 0; id < count; id++) {
			if (kits[id] == null) {
				kits[id] = new IdentityKit();
			}
			
			kits[id].decode(buffer);
		}
	}

	private int[] bodyModels;
	private int[] headModels = { -1, -1, -1, -1, -1 };
	private int[] originalColours = new int[6];
	private int part = -1;
	private int[] replacementColours = new int[6];
	private boolean validStyle = false;

	public boolean bodyLoaded() {
		if (bodyModels == null) {
			return true;
		}

		for (int part = 0; part < bodyModels.length; part++) {
			if (!Model.loaded(bodyModels[part])) {
				return false;
			}
		}

		return true;
	}

	public Model bodyModel() {
		if (bodyModels == null) {
			return null;
		}

		Model[] models = new Model[bodyModels.length];
		for (int part = 0; part < bodyModels.length; part++) {
			models[part] = Model.lookup(bodyModels[part]);
		}

		Model model;
		if (models.length == 1) {
			model = models[0];
		} else {
			model = new Model(models.length, models);
		}

		for (int part = 0; part < 6; part++) {
			if (originalColours[part] == 0) {
				break;
			}

			model.recolour(originalColours[part], replacementColours[part]);
		}

		return model;
	}

	public void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				return;
			}

			if (opcode == 1) {
				part = buffer.readUByte();
			} else if (opcode == 2) {
				int count = buffer.readUByte();
				bodyModels = new int[count];
				for (int part = 0; part < count; part++) {
					bodyModels[part] = buffer.readUShort();
				}
			} else if (opcode == 3) {
				validStyle = true;
			} else if (opcode >= 40 && opcode < 50) {
				originalColours[opcode - 40] = buffer.readUShort();
			} else if (opcode >= 50 && opcode < 60) {
				replacementColours[opcode - 50] = buffer.readUShort();
			} else if (opcode >= 60 && opcode < 70) {
				headModels[opcode - 60] = buffer.readUShort();
			} else {
				System.out.println("Error unrecognised config code: " + opcode);
			}
		} while (true);
	}

	public int[] getBodyModels() {
		return bodyModels;
	}

	public int[] getHeadModels() {
		return headModels;
	}

	public int[] getOriginalColours() {
		return originalColours;
	}

	public int getPart() {
		return part;
	}

	public int[] getReplacementColours() {
		return replacementColours;
	}

	public Model headModel() {
		Model[] models = new Model[5];
		int count = 0;
		for (int part = 0; part < 5; part++) {
			if (headModels[part] != -1) {
				models[count++] = Model.lookup(headModels[part]);
			}
		}

		Model model = new Model(count, models);
		for (int part = 0; part < 6; part++) {
			if (originalColours[part] == 0) {
				break;
			}

			model.recolour(originalColours[part], replacementColours[part]);
		}

		return model;
	}

	public boolean isValidStyle() {
		return validStyle;
	}

	public boolean loaded() {
		for (int part = 0; part < 5; part++) {
			if (headModels[part] != -1 && !Model.loaded(headModels[part])) {
				return false;
			}
		}

		return true;
	}

	public void setBodyModels(int[] bodyModels) {
		this.bodyModels = bodyModels;
	}

	public void setHeadModels(int[] headModels) {
		this.headModels = headModels;
	}

	public void setOriginalColours(int[] originalColours) {
		this.originalColours = originalColours;
	}

	public void setPart(int part) {
		this.part = part;
	}

	public void setReplacementColours(int[] replacementColours) {
		this.replacementColours = replacementColours;
	}

	public void setValidStyle(boolean validStyle) {
		this.validStyle = validStyle;
	}
}