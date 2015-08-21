package com.jagex.cache.def;

import com.jagex.Client;
import com.jagex.cache.Archive;
import com.jagex.cache.anim.Frame;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.ReferenceCache;
import com.jagex.net.ResourceProvider;
import com.jagex.setting.VariableBits;

public final class ObjectDefinition {

	// Thanks Super_

	public static ReferenceCache baseModels = new ReferenceCache(500);
	public static Client client;
	public static boolean lowMemory;
	public static ReferenceCache models = new ReferenceCache(30);

	private static ObjectDefinition[] cache;
	private static int cacheIndex;
	private static int count;
	private static Buffer data;
	private static int[] indices;
	private static Model[] parts = new Model[4];

	public static void dispose() {
		baseModels = null;
		models = null;
		indices = null;
		cache = null;
		data = null;
	}

	public static void init(Archive archive) {
		data = new Buffer(archive.getEntry("loc.dat"));
		Buffer buffer = new Buffer(archive.getEntry("loc.idx"));
		count = buffer.readUShort();
		indices = new int[count];
		int offset = 2;
		for (int index = 0; index < count; index++) {
			indices[index] = offset;
			offset += buffer.readUShort();
		}

		cache = new ObjectDefinition[20];
		for (int index = 0; index < 20; index++) {
			cache[index] = new ObjectDefinition();
		}
	}

	public static ObjectDefinition lookup(int id) {
		for (int index = 0; index < 20; index++) {
			if (cache[index].id == id) {
				return cache[index];
			}
		}

		cacheIndex = (cacheIndex + 1) % 20;
		ObjectDefinition definition = cache[cacheIndex];
		data.setPosition(indices[id]);
		definition.id = id;
		definition.reset();
		definition.decode(data);
		return definition;
	}

	private byte ambientLighting;
	private int animation;
	private boolean castsShadow;
	private boolean contouredGround;
	private int decorDisplacement;
	private boolean delayShading;
	private byte[] description;
	private boolean hollow;
	private int id = -1;
	private boolean impenetrable;
	private String[] interactions;
	private boolean interactive;
	private boolean inverted;
	private int length;
	private byte lightDiffusion;
	private int mapscene;
	private int minimapFunction;
	private int[] modelIds;
	private int[] modelTypes;
	private int[] morphisms;
	private int morphVarbitIndex;
	private int morphVariableIndex;
	private String name;
	private boolean obstructsGround;
	private boolean occludes;
	private int[] originalColours;
	private int[] replacementColours;
	private int scaleX;
	private int scaleY;
	private int scaleZ;
	private boolean solid;
	private int supportItems;
	private int surroundings;
	private int translateX;
	private int translateY;
	private int translateZ;
	private int width;

	public boolean castsShadow() {
		return castsShadow;
	}

	public boolean contoursGround() {
		return contouredGround;
	}

	public int getAnimation() {
		return animation;
	}

	public int getDecorDisplacement() {
		return decorDisplacement;
	}

	public byte[] getDescription() {
		return description;
	}

	public int getId() {
		return id;
	}

	public String[] getInteractions() {
		return interactions;
	}

	public String getInteraction(int index) {
		return interactions[index];
	}

	public int getLength() {
		return length;
	}

	public int getMapscene() {
		return mapscene;
	}

	public int getMinimapFunction() {
		return minimapFunction;
	}

	public int[] getMorphisms() {
		return morphisms;
	}

	public int getMorphVarbitIndex() {
		return morphVarbitIndex;
	}

	public int getMorphVariableIndex() {
		return morphVariableIndex;
	}

	public String getName() {
		return name;
	}

	public int getSurroundings() {
		return surroundings;
	}

	public int getWidth() {
		return width;
	}

	public boolean isImpenetrable() {
		return impenetrable;
	}

	public boolean isInteractive() {
		return interactive;
	}

	public boolean isSolid() {
		return solid;
	}

	public final void loadModels(ResourceProvider provider) {
		if (modelIds != null) {
			for (int id : modelIds) {
				provider.loadExtra(0, id & 0xffff);
			}
		}
	}

	public final Model modelAt(int type, int orientation, int aY, int bY, int cY, int dY, int frameId) {
		Model model = model(type, frameId, orientation);
		if (model == null) {
			return null;
		}

		if (contouredGround || delayShading) {
			model = new Model(contouredGround, delayShading, model);
		}

		if (contouredGround) {
			int y = (aY + bY + cY + dY) / 4;
			for (int vertex = 0; vertex < model.vertices; vertex++) {
				int x = model.vertexX[vertex];
				int z = model.vertexZ[vertex];
				int l2 = aY + (bY - aY) * (x + 64) / 128;
				int i3 = dY + (cY - dY) * (x + 64) / 128;
				int j3 = l2 + (i3 - l2) * (z + 64) / 128;
				model.vertexY[vertex] += j3 - y;
			}

			model.computeSphericalBounds();
		}
		return model;
	}

	public final ObjectDefinition morph() {
		int morphismIndex = -1;
		if (morphVarbitIndex != -1) {
			VariableBits bits = VariableBits.bits[morphVarbitIndex];
			int variable = bits.getSetting();
			int low = bits.getLow();
			int high = bits.getHigh();
			int mask = Client.BIT_MASKS[high - low];
			morphismIndex = client.settings[variable] >> low & mask;
		} else if (morphVariableIndex != -1) {
			morphismIndex = client.settings[morphVariableIndex];
		}
		if (morphismIndex < 0 || morphismIndex >= morphisms.length || morphisms[morphismIndex] == -1) {
			return null;
		}
		return lookup(morphisms[morphismIndex]);
	}

	public boolean obstructsGround() {
		return obstructsGround;
	}

	public boolean occludes() {
		return occludes;
	}

	public final boolean ready() {
		if (modelIds == null) {
			return true;
		}
		boolean ready = true;
		for (int id : modelIds) {
			ready &= Model.loaded(id & 0xffff);
		}

		return ready;
	}

	public final boolean ready(int position) {
		if (modelTypes == null) {
			if (modelIds == null) {
				return true;
			}

			if (position != 10) {
				return true;
			}

			boolean ready = true;
			for (int id : modelIds) {
				ready &= Model.loaded(id & 0xFFFF);
			}

			return ready;
		}

		for (int index = 0; index < modelTypes.length; index++) {
			if (modelTypes[index] == position) {
				return Model.loaded(modelIds[index] & 0xFFFF);
			}
		}

		return true;
	}

	private final void decode(Buffer buffer) {
		int interactive = -1;

		do {
			int opcode;
			opcode = buffer.readUByte();
			if (opcode == 0) {
				break;
			}

			if (opcode == 1) {
				int count = buffer.readUByte();
				if (count > 0) {
					if (modelIds == null || lowMemory) {
						modelTypes = new int[count];
						modelIds = new int[count];

						for (int i = 0; i < count; i++) {
							modelIds[i] = buffer.readUShort();
							modelTypes[i] = buffer.readUByte();
						}
					} else {
						buffer.setPosition(buffer.getPosition() + count * 3);
					}
				}
			} else if (opcode == 2) {
				name = buffer.readString();
			} else if (opcode == 3) {
				description = buffer.readStringBytes();
			} else if (opcode == 5) {
				int count = buffer.readUByte();
				if (count > 0) {
					if (modelIds == null || lowMemory) {
						modelTypes = null;
						modelIds = new int[count];

						for (int i = 0; i < count; i++) {
							modelIds[i] = buffer.readUShort();
						}
					} else {
						buffer.setPosition(buffer.getPosition() + count * 2);
					}
				}
			} else if (opcode == 14) {
				width = buffer.readUByte();
			} else if (opcode == 15) {
				length = buffer.readUByte();
			} else if (opcode == 17) {
				solid = false;
			} else if (opcode == 18) {
				impenetrable = false;
			} else if (opcode == 19) {
				interactive = buffer.readUByte();
				if (interactive == 1) {
					this.interactive = true;
				}
			} else if (opcode == 21) {
				contouredGround = true;
			} else if (opcode == 22) {
				delayShading = true;
			} else if (opcode == 23) {
				occludes = true;
			} else if (opcode == 24) {
				animation = buffer.readUShort();
				if (animation == 65535) {
					animation = -1;
				}
			} else if (opcode == 28) {
				decorDisplacement = buffer.readUByte();
			} else if (opcode == 29) {
				ambientLighting = buffer.readByte();
			} else if (opcode == 39) {
				lightDiffusion = buffer.readByte();
			} else if (opcode >= 30 && opcode < 39) {
				if (interactions == null) {
					interactions = new String[5];
				}
				interactions[opcode - 30] = buffer.readString();
				if (interactions[opcode - 30].equalsIgnoreCase("hidden")) {
					interactions[opcode - 30] = null;
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
				minimapFunction = buffer.readUShort();
			} else if (opcode == 62) {
				inverted = true;
			} else if (opcode == 64) {
				castsShadow = false;
			} else if (opcode == 65) {
				scaleX = buffer.readUShort();
			} else if (opcode == 66) {
				scaleY = buffer.readUShort();
			} else if (opcode == 67) {
				scaleZ = buffer.readUShort();
			} else if (opcode == 68) {
				mapscene = buffer.readUShort();
			} else if (opcode == 69) {
				surroundings = buffer.readUByte();
			} else if (opcode == 70) {
				translateX = buffer.readShort();
			} else if (opcode == 71) {
				translateY = buffer.readShort();
			} else if (opcode == 72) {
				translateZ = buffer.readShort();
			} else if (opcode == 73) {
				obstructsGround = true;
			} else if (opcode == 74) {
				hollow = true;
			} else if (opcode == 75) {
				supportItems = buffer.readUByte();
			} else if (opcode == 77) {
				morphVarbitIndex = buffer.readUShort();
				if (morphVarbitIndex == 65535) {
					morphVarbitIndex = -1;
				}

				morphVariableIndex = buffer.readUShort();
				if (morphVariableIndex == 65535) {
					morphVariableIndex = -1;
				}

				int count = buffer.readUByte();
				morphisms = new int[count + 1];
				for (int i = 0; i <= count; i++) {
					morphisms[i] = buffer.readUShort();
					if (morphisms[i] == 65535) {
						morphisms[i] = -1;
					}
				}
			} else {
				System.out.println("Unrecognised object opcode " + opcode);
				continue;
			}
		} while (true);

		if (interactive == -1) {
			this.interactive = ((modelIds != null && (modelTypes == null || modelTypes[0] == 10)) || interactions != null);
		}

		if (hollow) {
			solid = false;
			impenetrable = false;
		}

		if (supportItems == -1) {
			supportItems = solid ? 1 : 0;
		}
	}

	private final Model model(int type, int frame, int orientation) {
		Model base = null;
		long key;
		if (modelTypes == null) {
			if (type != 10) {
				return null;
			}

			key = (frame + 1L << 32) | (id << 6) | orientation;
			Model model = (Model) models.get(key);
			if (model != null) {
				return model;
			}

			if (modelIds == null) {
				return null;
			}

			boolean invert = inverted ^ orientation > 3;
			int count = modelIds.length;
			for (int index = 0; index < count; index++) {
				int id = modelIds[index];
				if (invert) {
					id |= 0x10000;
				}

				base = (Model) baseModels.get(id);
				if (base == null) {
					base = Model.lookup(id & 0xFFFF);
					if (base == null) {
						return null;
					}

					if (invert) {
						base.invert();
					}

					baseModels.put(id, base);
				}

				if (count > 1) {
					parts[index] = base;
				}
			}

			if (count > 1) {
				base = new Model(count, parts);
			}
		} else {
			int index = -1;
			for (int i = 0; i < modelTypes.length; i++) {
				if (modelTypes[i] != type) {
					continue;
				}

				index = i;
				break;
			}

			if (index == -1) {
				return null;
			}

			key = (frame + 1L << 32) | (id << 6) | (index << 3) | orientation;
			Model model = (Model) models.get(key);
			if (model != null) {
				return model;
			}

			int id = modelIds[index];
			boolean invert = inverted ^ orientation > 3;
			if (invert) {
				id |= 0x10000;
			}

			base = (Model) baseModels.get(id);
			if (base == null) {
				base = Model.lookup(id & 0xFFFF);
				if (base == null) {
					return null;
				}

				if (invert) {
					base.invert();
				}

				baseModels.put(id, base);
			}
		}

		boolean scale = scaleX != 128 || scaleY != 128 || scaleZ != 128;
		boolean translate = translateX != 0 || translateY != 0 || translateZ != 0;

		Model model = new Model(base, originalColours == null, Frame.isInvalid(frame), orientation == 0 && frame == -1 && !scale
				&& !translate);
		if (frame != -1) {
			model.skin();
			model.apply(frame);
			model.faceGroups = null;
			model.vertexGroups = null;
		}

		while (orientation-- > 0) {
			model.rotateClockwise();
		}

		if (originalColours != null) {
			for (int colour = 0; colour < originalColours.length; colour++) {
				model.recolour(originalColours[colour], replacementColours[colour]);
			}

		}
		if (scale) {
			model.scale(scaleX, scaleZ, scaleY);
		}

		if (translate) {
			model.translate(translateX, translateY, translateZ);
		}

		model.light(64 + ambientLighting, 768 + lightDiffusion * 5, -50, -10, -50, !delayShading);
		if (supportItems == 1) {
			model.anInt1654 = model.getModelHeight();
		}
		models.put(key, model);
		return model;
	}

	private final void reset() {
		modelIds = null;
		modelTypes = null;
		name = null;
		description = null;
		originalColours = null;
		replacementColours = null;
		width = 1;
		length = 1;
		solid = true;
		impenetrable = true;
		interactive = false;
		contouredGround = false;
		delayShading = false;
		occludes = false;
		animation = -1;
		decorDisplacement = 16;
		ambientLighting = 0;
		lightDiffusion = 0;
		interactions = null;
		minimapFunction = -1;
		mapscene = -1;
		inverted = false;
		castsShadow = true;
		scaleX = 128;
		scaleY = 128;
		scaleZ = 128;
		surroundings = 0;
		translateX = 0;
		translateY = 0;
		translateZ = 0;
		obstructsGround = false;
		hollow = false;
		supportItems = -1;
		morphVarbitIndex = -1;
		morphVariableIndex = -1;
		morphisms = null;
	}

}