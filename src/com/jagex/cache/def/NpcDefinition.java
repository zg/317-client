package com.jagex.cache.def;

import com.jagex.Client;
import com.jagex.cache.Archive;
import com.jagex.cache.anim.Frame;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.ReferenceCache;
import com.jagex.setting.VariableBits;

public final class NpcDefinition {

	public static Client client;
	public static ReferenceCache modelCache = new ReferenceCache(30);
	private static NpcDefinition[] cache;
	private static int cacheIndex;
	private static int count;
	private static Buffer data;
	private static int[] offsets;

	public static int getCount() {
		return count;
	}

	public static void init(Archive archive) {
		data = new Buffer(archive.getEntry("npc.dat"));
		Buffer indices = new Buffer(archive.getEntry("npc.idx"));
		count = indices.readUShort();
		offsets = new int[count];

		int index = 2;
		for (int id = 0; id < count; id++) {
			offsets[id] = index;
			index += indices.readUShort();
		}

		cache = new NpcDefinition[20];
		for (int id = 0; id < 20; id++) {
			cache[id] = new NpcDefinition();
		}
	}

	public static NpcDefinition lookup(int id) {
		for (int index = 0; index < 20; index++) {
			if (cache[index].id == id) {
				return cache[index];
			}
		}

		cacheIndex = (cacheIndex + 1) % 20;
		NpcDefinition definition = cache[cacheIndex] = new NpcDefinition();
		data.setPosition(offsets[id]);
		definition.id = id;
		definition.decode(data);
		return definition;
	}

	public static void reset() {
		modelCache = null;
		offsets = null;
		cache = null;
		data = null;
	}

	public static void setCount(int count) {
		NpcDefinition.count = count;
	}

	private int[] additionalModels;
	private boolean clickable = true;
	private int combat = -1;
	private byte[] description;
	private boolean drawMinimapDot = true;
	private int halfTurnAnimation = -1;
	private int headIcon = -1;
	private long id = -1;
	private int idleAnimation = -1;
	private String[] actions;
	private int lightModifier;
	private int[] modelIds;
	private int[] morphisms;
	private int varbit = -1;
	private int varp = -1;
	private String name;
	private int[] originalColours;
	private boolean priorityRender = false;
	private int[] replacementColours;
	private int rotateAntiClockwiseAnimation = -1;
	private int rotateClockwiseAnimation = -1;
	private int rotation = 32;
	private int scaleXY = 128;
	private int scaleZ = 128;
	private int shadowModifier;
	private byte size = 1;
	private int walkingAnimation = -1;

	public boolean drawMinimapDot() {
		return drawMinimapDot;
	}

	public int[] getAdditionalModels() {
		return additionalModels;
	}

	public final Model getAnimatedModel(int primaryFrame, int secondaryFrame, int[] interleaveOrder) {
		if (morphisms != null) {
			NpcDefinition definition = morph();
			if (definition == null) {
				return null;
			}

			return definition.getAnimatedModel(primaryFrame, secondaryFrame, interleaveOrder);
		}

		Model model = (Model) modelCache.get(id);
		if (model == null) {
			boolean unprepared = false;
			for (int part = 0; part < modelIds.length; part++) {
				if (!Model.loaded(modelIds[part])) {
					unprepared = true;
				}
			}

			if (unprepared) {
				return null;
			}
			Model[] models = new Model[this.modelIds.length];
			for (int part = 0; part < this.modelIds.length; part++) {
				models[part] = Model.lookup(this.modelIds[part]);
			}

			if (models.length == 1) {
				model = models[0];
			} else {
				model = new Model(models.length, models);
			}

			if (originalColours != null) {
				for (int i = 0; i < originalColours.length; i++) {
					model.recolour(originalColours[i], replacementColours[i]);
				}
			}

			model.skin();
			model.light(64 + lightModifier, 850 + shadowModifier, -30, -50, -30, true);
			modelCache.put(id, model);
		}

		Model empty = Model.EMPTY_MODEL;
		empty.method464(model, Frame.isInvalid(primaryFrame) & Frame.isInvalid(secondaryFrame));

		if (primaryFrame != -1 && secondaryFrame != -1) {
			empty.apply(primaryFrame, secondaryFrame, interleaveOrder);
		} else if (primaryFrame != -1) {
			empty.apply(primaryFrame);
		}

		if (scaleXY != 128 || scaleZ != 128) {
			empty.scale(scaleXY, scaleXY, scaleZ);
		}

		empty.method466();
		empty.faceGroups = null;
		empty.vertexGroups = null;
		if (size == 1) {
			empty.aBoolean1659 = true;
		}
		return empty;
	}

	public int getCombat() {
		return combat;
	}

	public byte[] getDescription() {
		return description;
	}

	public int getHalfTurnAnimation() {
		return halfTurnAnimation;
	}

	public int getHeadIcon() {
		return headIcon;
	}

	public long getId() {
		return id;
	}

	public int getIdleAnimation() {
		return idleAnimation;
	}

	public String getInteraction(int index) {
		return actions[index];
	}

	public String[] getInteractions() {
		return actions;
	}

	public int[] getMorphisms() {
		return morphisms;
	}

	public int getMorphVarbitIndex() {
		return varbit;
	}

	public int getMorphVariableIndex() {
		return varp;
	}

	public String getName() {
		return name;
	}

	public int getRotateAntiClockwiseAnimation() {
		return rotateAntiClockwiseAnimation;
	}

	public int getRotateClockwiseAnimation() {
		return rotateClockwiseAnimation;
	}

	public int getRotation() {
		return rotation;
	}

	public byte getSize() {
		return size;
	}

	public int getWalkingAnimation() {
		return walkingAnimation;
	}

	public boolean isClickable() {
		return clickable;
	}

	public boolean isPriorityRender() {
		return priorityRender;
	}

	public final Model model() {
		if (morphisms != null) {
			NpcDefinition definition = morph();
			return (definition == null) ? null : definition.model();
		} else if (additionalModels == null) {
			return null;
		}

		boolean unprepared = false;
		for (int i = 0; i < additionalModels.length; i++) {
			if (!Model.loaded(additionalModels[i])) {
				unprepared = true;
			}
		}

		if (unprepared) {
			return null;
		}

		Model[] additional = new Model[additionalModels.length];
		for (int index = 0; index < additionalModels.length; index++) {
			additional[index] = Model.lookup(additionalModels[index]);
		}

		Model model = (additional.length == 1) ? additional[0] : new Model(additional.length, additional);

		if (originalColours != null) {
			for (int index = 0; index < originalColours.length; index++) {
				model.recolour(originalColours[index], replacementColours[index]);
			}

		}
		return model;
	}

	public final NpcDefinition morph() {
		int child = -1;
		if (varbit != -1) {
			VariableBits bits = VariableBits.bits[varbit];
			int variable = bits.getSetting();
			int low = bits.getLow();
			int high = bits.getHigh();
			int mask = Client.BIT_MASKS[high - low];
			child = client.settings[variable] >> low & mask;
		} else if (varp != -1) {
			child = client.settings[varp];
		}

		if (child < 0 || child >= morphisms.length || morphisms[child] == -1) {
			return null;
		}

		return lookup(morphisms[child]);
	}

	private final void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				return;
			}

			if (opcode == 1) {
				int count = buffer.readUByte();
				modelIds = new int[count];
				for (int i = 0; i < count; i++) {
					modelIds[i] = buffer.readUShort();
				}
			} else if (opcode == 2) {
				name = buffer.readString();
			} else if (opcode == 3) {
				description = buffer.readStringBytes();
			} else if (opcode == 12) {
				size = buffer.readByte();
			} else if (opcode == 13) {
				idleAnimation = buffer.readUShort();
			} else if (opcode == 14) {
				walkingAnimation = buffer.readUShort();
			} else if (opcode == 17) {
				walkingAnimation = buffer.readUShort();
				halfTurnAnimation = buffer.readUShort();
				rotateClockwiseAnimation = buffer.readUShort();
				rotateAntiClockwiseAnimation = buffer.readUShort();
			} else if (opcode >= 30 && opcode < 40) {
				if (actions == null) {
					actions = new String[5];
				}
				actions[opcode - 30] = buffer.readString();
				if (actions[opcode - 30].equalsIgnoreCase("hidden")) {
					actions[opcode - 30] = null;
				}
			} else if (opcode == 40) {
				int count = buffer.readUByte();
				originalColours = new int[count];
				replacementColours = new int[count];
				for (int i = 0; i < count; i++) {
					originalColours[i] = buffer.readUShort();
					replacementColours[i] = buffer.readUShort();
				}

			} else if (opcode == 60) {
				int count = buffer.readUByte();
				additionalModels = new int[count];

				for (int i = 0; i < count; i++) {
					additionalModels[i] = buffer.readUShort();
				}
			} else if (opcode == 90) {
				buffer.readUShort();
			} else if (opcode == 91) {
				buffer.readUShort();
			} else if (opcode == 92) {
				buffer.readUShort();
			} else if (opcode == 93) {
				drawMinimapDot = false;
			} else if (opcode == 95) {
				combat = buffer.readUShort();
			} else if (opcode == 97) {
				scaleXY = buffer.readUShort();
			} else if (opcode == 98) {
				scaleZ = buffer.readUShort();
			} else if (opcode == 99) {
				priorityRender = true;
			} else if (opcode == 100) {
				lightModifier = buffer.readByte();
			} else if (opcode == 101) {
				shadowModifier = buffer.readByte() * 5;
			} else if (opcode == 102) {
				headIcon = buffer.readUShort();
			} else if (opcode == 103) {
				rotation = buffer.readUShort();
			} else if (opcode == 106) {
				varbit = buffer.readUShort();
				if (varbit == 65535) {
					varbit = -1;
				}
				varp = buffer.readUShort();
				if (varp == 65535) {
					varp = -1;
				}

				int count = buffer.readUByte();
				morphisms = new int[count + 1];

				for (int i = 0; i <= count; i++) {
					morphisms[i] = buffer.readUShort();
					if (morphisms[i] == 65535) {
						morphisms[i] = -1;
					}
				}

			} else if (opcode == 107) {
				clickable = false;
			} else {
				System.out.println("Unrecognised opcode=" + opcode);
			}
		} while (true);
	}

}