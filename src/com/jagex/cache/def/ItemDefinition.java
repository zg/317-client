package com.jagex.cache.def;

import com.jagex.cache.Archive;
import com.jagex.cache.graphics.Sprite;
import com.jagex.draw.Raster;
import com.jagex.draw.Rasterizer;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.ReferenceCache;

public final class ItemDefinition {

	// Thanks Super_
	// Class8

	public static boolean membersServer = true;
	public static ReferenceCache models = new ReferenceCache(50);
	public static ReferenceCache sprites = new ReferenceCache(100);
	private static ItemDefinition[] cache;
	private static int cacheIndex;
	private static int count;
	private static Buffer data;
	private static int[] offsets;

	public static void dispose() {
		models = null;
		sprites = null;
		offsets = null;
		cache = null;
		data = null;
	}

	public static int getCount() {
		return count;
	}

	public static void init(Archive archive) {
		data = new Buffer(archive.getEntry("obj.dat"));
		Buffer buffer = new Buffer(archive.getEntry("obj.idx"));
		count = buffer.readUShort();
		offsets = new int[count];
		int offset = 2;
		for (int i = 0; i < count; i++) {
			offsets[i] = offset;
			offset += buffer.readUShort();
		}

		cache = new ItemDefinition[10];
		for (int i = 0; i < 10; i++) {
			cache[i] = new ItemDefinition();
		}
	}

	public static ItemDefinition lookup(int id) {
		for (int i = 0; i < 10; i++) {
			if (cache[i].id == id) {
				return cache[i];
			}
		}

		cacheIndex = (cacheIndex + 1) % 10;
		ItemDefinition definition = cache[cacheIndex];
		data.setPosition(offsets[id]);
		definition.id = id;
		definition.reset();
		definition.decode(data);
		if (definition.notedTemplateId != -1) {
			definition.toNote();
		}

		if (!membersServer && definition.members) {
			definition.name = "Members Object";
			definition.description = "Login to a members' server to use this object.".getBytes();
			definition.groundActions = null;
			definition.widgetActions = null;
			definition.team = 0;
		}
		return definition;
	}

	public static void setCount(int count) {
		ItemDefinition.count = count;
	}

	public static Sprite sprite(int id, int stackSize, int backColour) {
		if (backColour == 0) {
			Sprite sprite = (Sprite) sprites.get(id);
			if (sprite != null && sprite.getResizeHeight() != stackSize && sprite.getResizeHeight() != -1) {
				sprite.unlink();
				sprite = null;
			}
			if (sprite != null) {
				return sprite;
			}
		}

		ItemDefinition definition = lookup(id);
		if (definition.stackIds == null) {
			stackSize = -1;
		}

		if (stackSize > 1) {
			int stackId = -1;
			for (int i = 0; i < 10; i++) {
				if (stackSize >= definition.stackAmounts[i] && definition.stackAmounts[i] != 0) {
					stackId = definition.stackIds[i];
				}
			}

			if (stackId != -1) {
				definition = lookup(stackId);
			}
		}

		Model model = definition.asGroundStack(1);
		if (model == null) {
			return null;
		}
		Sprite notedSprite = null;
		if (definition.notedTemplateId != -1) {
			notedSprite = sprite(definition.noteInfoId, 10, -1);
			if (notedSprite == null) {
				return null;
			}
		}

		Sprite rendered = new Sprite(32, 32);
		int centreX = Rasterizer.originViewX;
		int centreY = Rasterizer.originViewY;
		int[] scanOffsets = Rasterizer.scanOffsets;
		int[] raster = Raster.raster;
		int width = Raster.width;
		int height = Raster.height;
		int clipLeft = Raster.getClipLeft();
		int clipRight = Raster.getClipRight();
		int clipBottom = Raster.getClipBottom();
		int clipTop = Raster.getClipTop();
		Rasterizer.approximateAlphaBlending = false;
		Raster.init(32, 32, rendered.getRaster());
		Raster.fillRectangle(0, 0, 32, 32, 0);
		Rasterizer.useViewport();
		int scale = definition.spriteScale;

		if (backColour == -1) {
			scale = (int) (scale * 1.5D);
		}
		if (backColour > 0) {
			scale = (int) (scale * 1.04D);
		}

		int sin = Rasterizer.SINE[definition.spritePitch] * scale >> 16;
		int cos = Rasterizer.COSINE[definition.spritePitch] * scale >> 16;
		model.render(0, definition.spriteCameraRoll, definition.spriteCameraYaw, definition.spritePitch,
				definition.spriteTranslateX, sin + model.getModelHeight() / 2 + definition.spriteTranslateY, cos
						+ definition.spriteTranslateY);

		for (int x = 31; x >= 0; x--) {
			for (int y = 31; y >= 0; y--) {
				if (rendered.getRaster()[x + y * 32] == 0) {
					if (x > 0 && rendered.getRaster()[x - 1 + y * 32] > 1) {
						rendered.getRaster()[x + y * 32] = 1;
					} else if (y > 0 && rendered.getRaster()[x + (y - 1) * 32] > 1) {
						rendered.getRaster()[x + y * 32] = 1;
					} else if (x < 31 && rendered.getRaster()[x + 1 + y * 32] > 1) {
						rendered.getRaster()[x + y * 32] = 1;
					} else if (y < 31 && rendered.getRaster()[x + (y + 1) * 32] > 1) {
						rendered.getRaster()[x + y * 32] = 1;
					}
				}
			}
		}

		if (backColour > 0) {
			for (int x = 31; x >= 0; x--) {
				for (int y = 31; y >= 0; y--) {
					if (rendered.getRaster()[x + y * 32] == 0) {
						if (x > 0 && rendered.getRaster()[x - 1 + y * 32] == 1) {
							rendered.getRaster()[x + y * 32] = backColour;
						} else if (y > 0 && rendered.getRaster()[x + (y - 1) * 32] == 1) {
							rendered.getRaster()[x + y * 32] = backColour;
						} else if (x < 31 && rendered.getRaster()[x + 1 + y * 32] == 1) {
							rendered.getRaster()[x + y * 32] = backColour;
						} else if (y < 31 && rendered.getRaster()[x + (y + 1) * 32] == 1) {
							rendered.getRaster()[x + y * 32] = backColour;
						}
					}
				}
			}
		} else if (backColour == 0) {
			for (int x = 31; x >= 0; x--) {
				for (int y = 31; y >= 0; y--) {
					if (rendered.getRaster()[x + y * 32] == 0 && x > 0 && y > 0 && rendered.getRaster()[x - 1 + (y - 1) * 32] > 0) {
						rendered.getRaster()[x + y * 32] = 0x302020;
					}
				}
			}
		}

		if (definition.notedTemplateId != -1) {
			int resizeWidth = notedSprite.getResizeWidth();
			int resizeHeight = notedSprite.getResizeHeight();
			notedSprite.setResizeWidth(32);
			notedSprite.setResizeHeight(32);
			notedSprite.drawSprite(0, 0);
			notedSprite.setResizeWidth(resizeWidth);
			notedSprite.setResizeHeight(resizeHeight);
		}

		if (backColour == 0) {
			sprites.put(id, rendered);
		}

		Raster.init(height, width, raster);
		Raster.setBounds(clipTop, clipLeft, clipRight, clipBottom);
		Rasterizer.originViewX = centreX;
		Rasterizer.originViewY = centreY;
		Rasterizer.scanOffsets = scanOffsets;
		Rasterizer.approximateAlphaBlending = true;

		if (definition.stackable) {
			rendered.setResizeWidth(33);
		} else {
			rendered.setResizeWidth(32);
		}
		rendered.setResizeHeight(stackSize);
		return rendered;
	}

	private byte[] description;
	private byte femaleTranslation;
	private String[] groundActions;
	private int groundScaleX;
	private int groundScaleY;
	private int groundScaleZ;
	private int id = -1;
	private String[] widgetActions;
	private int ambience;
	private int diffusion;
	private byte maleTranslation;
	private boolean members;
	private int modelId;
	private String name;
	private int notedTemplateId;
	private int noteInfoId;
	private int[] originalColours;
	private int primaryFemaleModel;
	private int primaryFemaleHeadPiece;
	private int primaryMaleModel;
	private int primaryMaleHeadPiece;
	private int[] replacementColours;
	private int secondaryFemaleModel;
	private int secondaryFemaleHeadPiece;
	private int secondaryMaleModel;
	private int secondaryMaleHeadPiece;
	private int spriteCameraRoll;
	private int spriteCameraYaw;
	private int spritePitch;
	private int spriteScale;
	private int spriteTranslateX;
	private int spriteTranslateY;
	private boolean stackable;
	private int[] stackAmounts;
	private int[] stackIds;
	private int team;
	private int tertiaryFemaleEquipmentModel;
	private int tertiaryMaleEquipmentModel;
	private int value;

	public final Model asEquipment(int gender) {
		int primaryId = primaryMaleModel;
		int secondaryId = secondaryMaleModel;
		int tertiaryId = tertiaryMaleEquipmentModel;

		if (gender == 1) {
			primaryId = primaryFemaleModel;
			secondaryId = secondaryFemaleModel;
			tertiaryId = tertiaryFemaleEquipmentModel;
		}
		if (primaryId == -1) {
			return null;
		}

		Model primary = Model.lookup(primaryId);
		if (secondaryId != -1) {
			if (tertiaryId != -1) {
				Model secondary = Model.lookup(secondaryId);
				Model tertiary = Model.lookup(tertiaryId);
				Model parts[] = { primary, secondary, tertiary };
				primary = new Model(3, parts);
			} else {
				Model secondary = Model.lookup(secondaryId);
				Model parts[] = { primary, secondary };
				primary = new Model(2, parts);
			}
		}

		if (gender == 0 && maleTranslation != 0) {
			primary.translate(0, maleTranslation, 0);
		}
		if (gender == 1 && femaleTranslation != 0) {
			primary.translate(0, femaleTranslation, 0);
		}

		if (originalColours != null) {
			for (int i = 0; i < originalColours.length; i++) {
				primary.recolour(originalColours[i], replacementColours[i]);
			}
		}
		return primary;
	}

	public final Model asGroundStack(int amount) {
		if (stackIds != null && amount > 1) {
			int id = -1;
			for (int i = 0; i < 10; i++) {
				if (amount >= stackAmounts[i] && stackAmounts[i] != 0) {
					id = stackIds[i];
				}
			}

			if (id != -1) {
				return lookup(id).asGroundStack(1);
			}
		}

		Model model = (Model) models.get(id);
		if (model != null) {
			return model;
		}
		model = Model.lookup(modelId);
		if (model == null) {
			return null;
		}

		if (groundScaleX != 128 || groundScaleY != 128 || groundScaleZ != 128) {
			model.scale(groundScaleX, groundScaleZ, groundScaleY);
		}
		if (originalColours != null) {
			for (int i = 0; i < originalColours.length; i++) {
				model.recolour(originalColours[i], replacementColours[i]);
			}

		}

		model.light(64 + ambience, 768 + diffusion, -50, -10, -50, true);
		model.aBoolean1659 = true;
		models.put(id, model);
		return model;
	}

	public final Model asHeadPiece(int gender) {
		int primaryId = primaryMaleHeadPiece;
		int secondaryId = secondaryMaleHeadPiece;

		if (gender == 1) {
			primaryId = primaryFemaleHeadPiece;
			secondaryId = secondaryFemaleHeadPiece;
		}
		if (primaryId == -1) {
			return null;
		}

		Model primary = Model.lookup(primaryId);
		if (secondaryId != -1) {
			Model secondary = Model.lookup(secondaryId);
			primary = new Model(2, new Model[] { primary, secondary });
		}

		if (originalColours != null) {
			for (int index = 0; index < originalColours.length; index++) {
				primary.recolour(originalColours[index], replacementColours[index]);
			}

		}
		return primary;
	}

	public final Model asStack(int stackSize) {
		if (stackIds != null && stackSize > 1) {
			int id = -1;
			for (int i = 0; i < 10; i++) {
				if (stackSize >= stackAmounts[i] && stackAmounts[i] != 0) {
					id = stackIds[i];
				}
			}

			if (id != -1) {
				return lookup(id).asStack(1);
			}
		}

		Model model = Model.lookup(modelId);
		if (model == null) {
			return null;
		}

		if (originalColours != null) {
			for (int i = 0; i < originalColours.length; i++) {
				model.recolour(originalColours[i], replacementColours[i]);
			}
		}

		return model;
	}

	public final void decode(Buffer buffer) {
		do {
			int opcode = buffer.readUByte();
			if (opcode == 0) {
				return;
			}

			if (opcode == 1) {
				modelId = buffer.readUShort();
			} else if (opcode == 2) {
				name = buffer.readString();
			} else if (opcode == 3) {
				description = buffer.readStringBytes();
			} else if (opcode == 4) {
				spriteScale = buffer.readUShort();
			} else if (opcode == 5) {
				spritePitch = buffer.readUShort();
			} else if (opcode == 6) {
				spriteCameraRoll = buffer.readUShort();
			} else if (opcode == 7) {
				spriteTranslateX = buffer.readUShort();
				if (spriteTranslateX > 32767) {
					spriteTranslateX -= 0x10000;
				}
			} else if (opcode == 8) {
				spriteTranslateY = buffer.readUShort();
				if (spriteTranslateY > 32767) {
					spriteTranslateY -= 0x10000;
				}
			} else if (opcode == 10) {
				buffer.readUShort();
			} else if (opcode == 11) {
				stackable = true;
			} else if (opcode == 12) {
				value = buffer.readInt();
			} else if (opcode == 16) {
				members = true;
			} else if (opcode == 23) {
				primaryMaleModel = buffer.readUShort();
				maleTranslation = buffer.readByte();
			} else if (opcode == 24) {
				secondaryMaleModel = buffer.readUShort();
			} else if (opcode == 25) {
				primaryFemaleModel = buffer.readUShort();
				femaleTranslation = buffer.readByte();
			} else if (opcode == 26) {
				secondaryFemaleModel = buffer.readUShort();
			} else if (opcode >= 30 && opcode < 35) {
				if (groundActions == null) {
					groundActions = new String[5];
				}
				groundActions[opcode - 30] = buffer.readString();
				if (groundActions[opcode - 30].equalsIgnoreCase("hidden")) {
					groundActions[opcode - 30] = null;
				}
			} else if (opcode >= 35 && opcode < 40) {
				if (widgetActions == null) {
					widgetActions = new String[5];
				}
				widgetActions[opcode - 35] = buffer.readString();
			} else if (opcode == 40) {
				int count = buffer.readUByte();
				originalColours = new int[count];
				replacementColours = new int[count];
				for (int i = 0; i < count; i++) {
					originalColours[i] = buffer.readUShort();
					replacementColours[i] = buffer.readUShort();
				}
			} else if (opcode == 78) {
				tertiaryMaleEquipmentModel = buffer.readUShort();
			} else if (opcode == 79) {
				tertiaryFemaleEquipmentModel = buffer.readUShort();
			} else if (opcode == 90) {
				primaryMaleHeadPiece = buffer.readUShort();
			} else if (opcode == 91) {
				primaryFemaleHeadPiece = buffer.readUShort();
			} else if (opcode == 92) {
				secondaryMaleHeadPiece = buffer.readUShort();
			} else if (opcode == 93) {
				secondaryFemaleHeadPiece = buffer.readUShort();
			} else if (opcode == 95) {
				spriteCameraYaw = buffer.readUShort();
			} else if (opcode == 97) {
				noteInfoId = buffer.readUShort();
			} else if (opcode == 98) {
				notedTemplateId = buffer.readUShort();
			} else if (opcode >= 100 && opcode < 110) {
				if (stackIds == null) {
					stackIds = new int[10];
					stackAmounts = new int[10];
				}
				
				stackIds[opcode - 100] = buffer.readUShort();
				stackAmounts[opcode - 100] = buffer.readUShort();
			} else if (opcode == 110) {
				groundScaleX = buffer.readUShort();
			} else if (opcode == 111) {
				groundScaleY = buffer.readUShort();
			} else if (opcode == 112) {
				groundScaleZ = buffer.readUShort();
			} else if (opcode == 113) {
				ambience = buffer.readByte();
			} else if (opcode == 114) {
				diffusion = buffer.readByte() * 5;
			} else if (opcode == 115) {
				team = buffer.readUByte();
			}
		} while (true);
	}

	public final boolean equipmentReady(int gender) {
		int primary = primaryMaleModel;
		int secondary = secondaryMaleModel;
		int tertiary = tertiaryMaleEquipmentModel;
		if (gender == 1) {
			primary = primaryFemaleModel;
			secondary = secondaryFemaleModel;
			tertiary = tertiaryFemaleEquipmentModel;
		}

		if (primary == -1) {
			return true;
		}

		boolean ready = true;
		if (!Model.loaded(primary)) {
			ready = false;
		}
		if (secondary != -1 && !Model.loaded(secondary)) {
			ready = false;
		}
		if (tertiary != -1 && !Model.loaded(tertiary)) {
			ready = false;
		}

		return ready;
	}

	public byte[] getDescription() {
		return description;
	}

	public byte getFemaleEquipmentTranslation() {
		return femaleTranslation;
	}

	public String[] getGroundMenuActions() {
		return groundActions;
	}

	public int getGroundScaleX() {
		return groundScaleX;
	}

	public int getGroundScaleY() {
		return groundScaleY;
	}

	public int getGroundScaleZ() {
		return groundScaleZ;
	}

	public int getId() {
		return id;
	}

	public String[] getInventoryMenuActions() {
		return widgetActions;
	}

	public String getInventoryMenuAction(int index) {
		return widgetActions[index];
	}

	public int getLightAmbience() {
		return ambience;
	}

	public int getLightDiffusion() {
		return diffusion;
	}

	public byte getMaleEquipmentTranslation() {
		return maleTranslation;
	}

	public int getModelId() {
		return modelId;
	}

	public String getName() {
		return name;
	}

	public int getNotedTemplateId() {
		return notedTemplateId;
	}

	public int getNoteInfoId() {
		return noteInfoId;
	}

	public int[] getOriginalColours() {
		return originalColours;
	}

	public int getPrimaryFemaleEquipmentModel() {
		return primaryFemaleModel;
	}

	public int getPrimaryFemaleHeadPiece() {
		return primaryFemaleHeadPiece;
	}

	public int getPrimaryMaleEquipmentModel() {
		return primaryMaleModel;
	}

	public int getPrimaryMaleHeadPiece() {
		return primaryMaleHeadPiece;
	}

	public int[] getReplacementColours() {
		return replacementColours;
	}

	public int getSecondaryFemaleEquipmentModel() {
		return secondaryFemaleModel;
	}

	public int getSecondaryFemaleHeadPiece() {
		return secondaryFemaleHeadPiece;
	}

	public int getSecondaryMaleEquipmentModel() {
		return secondaryMaleModel;
	}

	public int getSecondaryMaleHeadPiece() {
		return secondaryMaleHeadPiece;
	}

	public int getSpriteCameraRoll() {
		return spriteCameraRoll;
	}

	public int getSpriteCameraYaw() {
		return spriteCameraYaw;
	}

	public int getSpritePitch() {
		return spritePitch;
	}

	public int getSpriteScale() {
		return spriteScale;
	}

	public int getSpriteTranslateX() {
		return spriteTranslateX;
	}

	public int getSpriteTranslateY() {
		return spriteTranslateY;
	}

	public int[] getStackAmounts() {
		return stackAmounts;
	}

	public int[] getStackIds() {
		return stackIds;
	}

	public int getTeam() {
		return team;
	}

	public int getTertiaryFemaleEquipmentModel() {
		return tertiaryFemaleEquipmentModel;
	}

	public int getTertiaryMaleEquipmentModel() {
		return tertiaryMaleEquipmentModel;
	}

	public int getValue() {
		return value;
	}

	public final boolean headPieceReady(int gender) {
		int primary = primaryMaleHeadPiece;
		int secondary = secondaryMaleHeadPiece;
		if (gender == 1) {
			primary = primaryFemaleHeadPiece;
			secondary = secondaryFemaleHeadPiece;
		}
		
		if (primary == -1) {
			return true;
		}
		
		boolean ready = true;
		if (!Model.loaded(primary)) {
			ready = false;
		}
		if (secondary != -1 && !Model.loaded(secondary)) {
			ready = false;
		}
		
		return ready;
	}

	public boolean isMembers() {
		return members;
	}

	public boolean isStackable() {
		return stackable;
	}

	public final void reset() {
		modelId = 0;
		name = null;
		description = null;
		originalColours = null;
		replacementColours = null;
		spriteScale = 2000;
		spritePitch = 0;
		spriteCameraRoll = 0;
		spriteCameraYaw = 0;
		spriteTranslateX = 0;
		spriteTranslateY = 0;
		stackable = false;
		value = 1;
		members = false;
		groundActions = null;
		widgetActions = null;
		primaryMaleModel = -1;
		secondaryMaleModel = -1;
		maleTranslation = 0;
		primaryFemaleModel = -1;
		secondaryFemaleModel = -1;
		femaleTranslation = 0;
		tertiaryMaleEquipmentModel = -1;
		tertiaryFemaleEquipmentModel = -1;
		primaryMaleHeadPiece = -1;
		secondaryMaleHeadPiece = -1;
		primaryFemaleHeadPiece = -1;
		secondaryFemaleHeadPiece = -1;
		stackIds = null;
		stackAmounts = null;
		noteInfoId = -1;
		notedTemplateId = -1;
		groundScaleX = 128;
		groundScaleY = 128;
		groundScaleZ = 128;
		ambience = 0;
		diffusion = 0;
		team = 0;
	}

	public void toNote() {
		ItemDefinition template = lookup(notedTemplateId);
		modelId = template.modelId;
		spriteScale = template.spriteScale;
		spritePitch = template.spritePitch;
		spriteCameraRoll = template.spriteCameraRoll;
		spriteCameraYaw = template.spriteCameraYaw;
		spriteTranslateX = template.spriteTranslateX;
		spriteTranslateY = template.spriteTranslateY;
		originalColours = template.originalColours;
		replacementColours = template.replacementColours;
		ItemDefinition info = lookup(noteInfoId);
		name = info.name;
		members = info.members;
		value = info.value;
		String article = "a";
		
		char c = info.name.charAt(0);
		if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') {
			article = "an";
		}
		
		description = ("Swap this note at any bank for " + article + " " + info.name + ".").getBytes();
		stackable = true;
	}

}