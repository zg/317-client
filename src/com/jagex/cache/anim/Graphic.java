package com.jagex.cache.anim;

import com.jagex.cache.Archive;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.ReferenceCache;

public class Graphic {

	public static Graphic[] graphics;
	public static ReferenceCache models = new ReferenceCache(30);
	private static int count;

	public static void init(Archive archive) {
		Buffer buffer = new Buffer(archive.getEntry("spotanim.dat"));
		count = buffer.readUShort();
		if (graphics == null) {
			graphics = new Graphic[count];
		}

		for (int id = 0; id < count; id++) {
			if (graphics[id] == null) {
				graphics[id] = new Graphic();
			}

			graphics[id].id = id;
			graphics[id].decode(buffer);
		}
	}

	private Animation animation;
	private int animationId = -1;
	private int breadthScale = 128;
	private int depthScale = 128;
	private int id;
	/**
	 * The id of the model used by this Graphic.
	 */
	private int model;

	private int modelBrightness;
	private int modelShadow;
	private int orientation;
	private int[] originalColours = new int[6];
	private int[] replacementColours = new int[6];

	public void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				return;
			}

			if (opcode == 1) {
				model = buffer.readUShort();
			} else if (opcode == 2) {
				animationId = buffer.readUShort();
				if (Animation.animations != null) {
					animation = Animation.animations[animationId];
				}
			} else if (opcode == 4) {
				breadthScale = buffer.readUShort();
			} else if (opcode == 5) {
				depthScale = buffer.readUShort();
			} else if (opcode == 6) {
				orientation = buffer.readUShort();
			} else if (opcode == 7) {
				modelBrightness = buffer.readUByte();
			} else if (opcode == 8) {
				modelShadow = buffer.readUByte();
			} else if (opcode >= 40 && opcode < 50) {
				originalColours[opcode - 40] = buffer.readUShort();
			} else if (opcode >= 50 && opcode < 60) {
				replacementColours[opcode - 50] = buffer.readUShort();
			} else {
				System.out.println("Error unrecognised spotanim config code: " + opcode);
			}
		} while (true);
	}

	/**
	 * Gets the {@link Animation } used by this Graphic.
	 *
	 * @return The Animation.
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Gets the breadth scale.
	 *
	 * @return The breadth scale.
	 */
	public int getBreadthScale() {
		return breadthScale;
	}

	/**
	 * Gets the depth scale.
	 *
	 * @return The depth scale.
	 */
	public int getDepthScale() {
		return depthScale;
	}

	/**
	 * Gets the id of this Graphic.
	 *
	 * @return The id.
	 */
	public int getId() {
		return id;
	}

	public Model getModel() {
		Model model = (Model) models.get(id);
		if (model != null) {
			return model;
		}

		model = Model.lookup(this.model);
		if (model == null) {
			return null;
		}

		for (int part = 0; part < 6; part++) {
			if (originalColours[0] != 0) {
				model.recolour(originalColours[part], replacementColours[part]);
			}
		}

		models.put(id, model);
		return model;
	}

	/**
	 * Gets the model brightness.
	 *
	 * @return The model brightness.
	 */
	public int getModelBrightness() {
		return modelBrightness;
	}

	/**
	 * Gets the id of the model used by this Graphic.
	 *
	 * @return The model id.
	 */
	public int getModelId() {
		return model;
	}

	/**
	 * Gets the model shadow.
	 *
	 * @return The model shadow.
	 */
	public int getModelShadow() {
		return modelShadow;
	}

	/**
	 * Gets the orientation.
	 *
	 * @return The orientation.
	 */
	public int getOrientation() {
		return orientation;
	}

}