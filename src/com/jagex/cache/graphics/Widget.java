package com.jagex.cache.graphics;

import com.jagex.Client;
import com.jagex.cache.Archive;
import com.jagex.cache.anim.Frame;
import com.jagex.cache.def.ItemDefinition;
import com.jagex.cache.def.NpcDefinition;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.ReferenceCache;
import com.jagex.util.StringUtils;

public class Widget {

	public static final int OPTION_OK = 1;
	public static final int OPTION_USABLE = 2;
	public static final int OPTION_CLOSE = 3;
	public static final int OPTION_TOGGLE_SETTING = 4;
	public static final int OPTION_RESET_SETTING = 5;
	public static final int OPTION_CONTINUE = 6;

	public static final int TYPE_CONTAINER = 0;
	public static final int TYPE_MODEL_LIST = 1;
	public static final int TYPE_INVENTORY = 2;
	public static final int TYPE_RECTANGLE = 3;
	public static final int TYPE_TEXT = 4;
	public static final int TYPE_SPRITE = 5;
	public static final int TYPE_MODEL = 6;
	public static final int TYPE_ITEM_LIST = 7;

	public static Widget[] widgets;
	private static ReferenceCache models = new ReferenceCache(30);
	private static ReferenceCache spriteCache;

	public static void clearModels(int id, int type, Model model) {
		models.clear();

		if (model != null && type != 4) {
			models.put((type << 16) | id, model);
		}
	}

	public static void load(Archive interfaces, Archive graphics, Font[] fonts) {
		spriteCache = new ReferenceCache(50000);
		Buffer buffer = new Buffer(interfaces.getEntry("data"));
		widgets = new Widget[buffer.readUShort()];

		while (buffer.getPosition() < buffer.getPayload().length) {
			int id = buffer.readUShort(), parent = -1;
			if (id == 65535) {
				parent = buffer.readUShort();
				id = buffer.readUShort();
			}

			Widget widget = widgets[id] = new Widget();
			widget.id = id;
			widget.parent = parent;
			widget.group = buffer.readUByte();
			widget.optionType = buffer.readUByte();
			widget.contentType = buffer.readUShort();
			widget.width = buffer.readUShort();
			widget.height = buffer.readUShort();
			widget.alpha = (byte) buffer.readUByte();

			int hover = buffer.readUByte();
			widget.hoverId = (hover != 0) ? (hover - 1 << 8) | buffer.readUByte() : -1;

			int operators = buffer.readUByte();
			if (operators > 0) {
				widget.scriptOperators = new int[operators];
				widget.scriptDefaults = new int[operators];

				for (int index = 0; index < operators; index++) {
					widget.scriptOperators[index] = buffer.readUByte();
					widget.scriptDefaults[index] = buffer.readUShort();
				}
			}

			int scripts = buffer.readUByte();
			if (scripts > 0) {
				widget.scripts = new int[scripts][];

				for (int script = 0; script < scripts; script++) {
					int instructions = buffer.readUShort();
					widget.scripts[script] = new int[instructions];

					for (int instruction = 0; instruction < instructions; instruction++) {
						widget.scripts[script][instruction] = buffer.readUShort();
					}
				}
			}

			if (widget.group == TYPE_CONTAINER) {
				widget.scrollLimit = buffer.readUShort();
				widget.hidden = buffer.readUByte() == 1;

				int children = buffer.readUShort();
				widget.children = new int[children];
				widget.childX = new int[children];
				widget.childY = new int[children];

				for (int index = 0; index < children; index++) {
					widget.children[index] = buffer.readUShort();
					widget.childX[index] = buffer.readShort();
					widget.childY[index] = buffer.readShort();
				}
			}

			if (widget.group == TYPE_MODEL_LIST) {
				buffer.readUShort();
				buffer.readUByte(); // == 1
			}

			if (widget.group == TYPE_INVENTORY) {
				widget.inventoryIds = new int[widget.width * widget.height];
				widget.inventoryAmounts = new int[widget.width * widget.height];

				widget.swappableItems = buffer.readUByte() == 1;
				widget.hasActions = buffer.readUByte() == 1;
				widget.usableItems = buffer.readUByte() == 1;
				widget.replaceItems = buffer.readUByte() == 1;

				widget.spritePaddingX = buffer.readUByte();
				widget.spritePaddingY = buffer.readUByte();

				widget.spriteX = new int[20];
				widget.spriteY = new int[20];
				widget.sprites = new Sprite[20];

				for (int index = 0; index < 20; index++) {
					int exists = buffer.readUByte();
					if (exists == 1) {
						widget.spriteX[index] = buffer.readShort();
						widget.spriteY[index] = buffer.readShort();
						String name = buffer.readString();

						if (graphics != null && name.length() > 0) {
							int position = name.lastIndexOf(",");
							widget.sprites[index] = getSprite(graphics, name.substring(0, position),
									Integer.parseInt(name.substring(position + 1)));
						}
					}
				}

				widget.actions = new String[5];
				for (int index = 0; index < 5; index++) {
					widget.actions[index] = buffer.readString();

					if (widget.actions[index].isEmpty()) {
						widget.actions[index] = null;
					}
				}
			}

			if (widget.group == TYPE_RECTANGLE) {
				widget.filled = buffer.readUByte() == 1;
			}

			if (widget.group == TYPE_TEXT || widget.group == TYPE_MODEL_LIST) {
				widget.centeredText = buffer.readUByte() == 1;
				int font = buffer.readUByte();

				if (fonts != null) {
					widget.font = fonts[font];
				}

				widget.shadowedText = buffer.readUByte() == 1;
			}

			if (widget.group == TYPE_TEXT) {
				widget.defaultText = buffer.readString();
				widget.secondaryText = buffer.readString();
			}

			if (widget.group == TYPE_MODEL_LIST || widget.group == TYPE_RECTANGLE || widget.group == TYPE_TEXT) {
				widget.defaultColour = buffer.readInt();
			}

			if (widget.group == TYPE_RECTANGLE || widget.group == TYPE_TEXT) {
				widget.secondaryColour = buffer.readInt();
				widget.defaultHoverColour = buffer.readInt();
				widget.secondaryHoverColour = buffer.readInt();
			} else if (widget.group == TYPE_SPRITE) {
				String name = buffer.readString();
				if (graphics != null && name.length() > 0) {
					int index = name.lastIndexOf(",");
					widget.defaultSprite = getSprite(graphics, name.substring(0, index),
							Integer.parseInt(name.substring(index + 1)));
				}

				name = buffer.readString();
				if (graphics != null && name.length() > 0) {
					int index = name.lastIndexOf(",");
					widget.secondarySprite = getSprite(graphics, name.substring(0, index),
							Integer.parseInt(name.substring(index + 1)));
				}
			} else if (widget.group == TYPE_MODEL) {
				int content = buffer.readUByte();
				if (content != 0) {
					widget.defaultMediaType = 1;
					widget.defaultMedia = (content - 1 << 8) + buffer.readUByte();
				}

				content = buffer.readUByte();
				if (content != 0) {
					widget.secondaryMediaType = 1;
					widget.secondaryMedia = (content - 1 << 8) + buffer.readUByte();
				}

				content = buffer.readUByte();
				widget.defaultAnimationId = (content != 0) ? (content - 1 << 8) + buffer.readUByte() : -1;

				content = buffer.readUByte();
				widget.secondaryAnimationId = (content != 0) ? (content - 1 << 8) + buffer.readUByte() : -1;

				widget.spriteScale = buffer.readUShort();
				widget.spritePitch = buffer.readUShort();
				widget.spriteRoll = buffer.readUShort();
			} else if (widget.group == TYPE_ITEM_LIST) {
				widget.inventoryIds = new int[widget.width * widget.height];
				widget.inventoryAmounts = new int[widget.width * widget.height];
				widget.centeredText = buffer.readUByte() == 1;

				int font = buffer.readUByte();
				if (fonts != null) {
					widget.font = fonts[font];
				}

				widget.shadowedText = buffer.readUByte() == 1;
				widget.defaultColour = buffer.readInt();
				widget.spritePaddingX = buffer.readShort();
				widget.spritePaddingY = buffer.readShort();
				widget.hasActions = buffer.readUByte() == 1;
				widget.actions = new String[5];

				for (int index = 0; index < 5; index++) {
					widget.actions[index] = buffer.readString();

					if (widget.actions[index].isEmpty()) {
						widget.actions[index] = null;
					}
				}
			}

			if (widget.optionType == OPTION_USABLE || widget.group == TYPE_INVENTORY) {
				widget.optionCircumfix = buffer.readString();
				widget.optionText = buffer.readString();
				widget.optionAttributes = buffer.readUShort();
			}

			if (widget.optionType == OPTION_OK || widget.optionType == OPTION_TOGGLE_SETTING
					|| widget.optionType == OPTION_RESET_SETTING || widget.optionType == OPTION_CONTINUE) {
				widget.hover = buffer.readString();

				if (widget.hover.isEmpty()) {
					if (widget.optionType == OPTION_OK) {
						widget.hover = "Ok";
					} else if (widget.optionType == OPTION_TOGGLE_SETTING) {
						widget.hover = "Select";
					} else if (widget.optionType == OPTION_RESET_SETTING) {
						widget.hover = "Select";
					} else if (widget.optionType == OPTION_CONTINUE) {
						widget.hover = "Continue";
					}
				}
			}
		}
	}

	private static Sprite getSprite(Archive archive, String name, int id) {
		long key = (StringUtils.hashSpriteName(name) << 8) | id;
		Sprite sprite = (Sprite) spriteCache.get(key);
		if (sprite != null) {
			return sprite;
		}

		try {
			sprite = new Sprite(archive, name, id);
			spriteCache.put(key, sprite);
		} catch (Exception ex) {
			return null;
		}

		return sprite;
	}

	public String[] actions;
	public byte alpha;
	public boolean centeredText;
	public int[] children;
	public int[] childX;
	public int[] childY;
	public int contentType;
	public int defaultAnimationId;
	public int defaultColour;
	public int defaultHoverColour;
	public int defaultMedia;
	public int defaultMediaType;
	public Sprite defaultSprite;
	public String defaultText;
	public int currentFrame;

	/**
	 * Indicates whether or not the widget should be drawn filled, or just as an outline.
	 */
	public boolean filled;
	public Font font;
	public int group;
	public boolean hasActions;
	public int height;
	public boolean hidden;
	public int horizontalDrawOffset;
	public String hover;
	public int hoverId;
	public int id;
	public int[] inventoryAmounts;
	public int[] inventoryIds;
	public int lastFrameTime;
	public int optionAttributes;
	public String optionCircumfix;
	public String optionText;
	public int optionType;
	public int parent;
	public boolean replaceItems;
	public int[] scriptDefaults;
	public int[] scriptOperators;
	public int[][] scripts;
	public int scrollLimit;
	public int scrollPosition;
	public int secondaryAnimationId;
	public int secondaryColour;
	public int secondaryHoverColour;
	public int secondaryMedia;
	public int secondaryMediaType;
	public Sprite secondarySprite;
	public String secondaryText;
	public boolean shadowedText;
	public int spritePaddingX;
	public int spritePaddingY;
	public int spritePitch;
	public int spriteRoll;
	public Sprite[] sprites;
	public int spriteScale;
	public int[] spriteX;
	public int[] spriteY;
	public boolean swappableItems;
	public boolean usableItems;
	public int verticalDrawOffset;
	public int width;

	public Model getAnimatedModel(int primaryFrame, int secondaryFrame, boolean secondary) {
		Model model = secondary ? getModel(secondaryMediaType, secondaryMedia) : getModel(defaultMediaType, defaultMedia);

		if (model == null) {
			return null;
		}

		if (primaryFrame == -1 && secondaryFrame == -1 && model.faceColours == null) {
			return model;
		}

		Model animated = new Model(model, true, Frame.isInvalid(primaryFrame) & Frame.isInvalid(secondaryFrame), false);
		if (primaryFrame != -1 || secondaryFrame != -1) {
			animated.skin();
		}

		if (primaryFrame != -1) {
			animated.apply(primaryFrame);
		}

		if (secondaryFrame != -1) {
			animated.apply(secondaryFrame);
		}

		animated.light(64, 768, -50, -10, -50, true);
		return animated;
	}

	public void swapInventoryItems(int first, int second) {
		int tmp = inventoryIds[first];
		inventoryIds[first] = inventoryIds[second];
		inventoryIds[second] = tmp;

		tmp = inventoryAmounts[first];
		inventoryAmounts[first] = inventoryAmounts[second];
		inventoryAmounts[second] = tmp;
	}

	private Model getModel(int type, int id) {
		Model model = (Model) models.get((type << 16) | id);
		if (model != null) {
			return model;
		}

		if (type == 1) {
			model = Model.lookup(id);
		} else if (type == 2) {
			model = NpcDefinition.lookup(id).model();
		} else if (type == 3) {
			model = Client.localPlayer.getBodyModel();
		} else if (type == 4) {
			model = ItemDefinition.lookup(id).asStack(50);
		}

		if (model != null) {
			models.put((type << 16) | id, model);
		}

		return model;
	}

}