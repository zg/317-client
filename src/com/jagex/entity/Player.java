package com.jagex.entity;

import com.jagex.Client;
import com.jagex.cache.anim.Animation;
import com.jagex.cache.anim.Frame;
import com.jagex.cache.anim.Graphic;
import com.jagex.cache.def.ItemDefinition;
import com.jagex.cache.def.NpcDefinition;
import com.jagex.entity.model.IdentityKit;
import com.jagex.entity.model.Model;
import com.jagex.io.Buffer;
import com.jagex.link.ReferenceCache;
import com.jagex.util.StringUtils;

public final class Player extends Mob {

	public static ReferenceCache models = new ReferenceCache(260);
	public boolean aBoolean1699 = false;
	public int anInt1709;
	public int anInt1711;
	public int anInt1712;
	public int anInt1713;
	public int anInt1719;
	public int anInt1720;
	public int anInt1721;
	public int anInt1722;
	public int[] appearanceColours = new int[5];
	public int[] appearanceModels = new int[12];
	public int combat;
	public int gender;
	public int headIcon;
	public long modelKey = -1;
	public String name;
	public NpcDefinition npcDefinition;
	public int objectAppearanceEndTick; // the tick when the player stops appearing as an object
	public int objectAppearanceStartTick; // the tick when the player appear as an object
	public Model objectModel; // if the player is appearing as an object
	public int skill;
	public int team;
	public boolean visible = false;
	long appearanceHash;

	public final Model getBodyModel() {
		if (!visible) {
			return null;
		}

		if (npcDefinition != null) {
			return npcDefinition.model();
		}

		boolean unprepared = false;
		for (int i = 0; i < 12; i++) {
			int model = appearanceModels[i];
			if (model >= 256 && model < 512 && !IdentityKit.kits[model - 256].loaded()) {
				unprepared = true;
			}
			if (model >= 512 && !ItemDefinition.lookup(model - 512).headPieceReady(gender)) {
				unprepared = true;
			}
		}

		if (unprepared) {
			return null;
		}

		Model[] bodyModels = new Model[12];
		int count = 0;
		for (int part = 0; part < 12; part++) {
			int modelId = appearanceModels[part];

			if (modelId >= 256 && modelId < 512) {
				Model model = IdentityKit.kits[modelId - 256].headModel();
				if (model != null) {
					bodyModels[count++] = model;
				}
			}

			if (modelId >= 512) {
				Model model = ItemDefinition.lookup(modelId - 512).asHeadPiece(gender);
				if (model != null) {
					bodyModels[count++] = model;
				}
			}
		}

		Model model = new Model(count, bodyModels);
		for (int part = 0; part < 5; part++) {
			if (appearanceColours[part] != 0) {
				model.recolour(Client.PLAYER_BODY_RECOLOURS[part][0], Client.PLAYER_BODY_RECOLOURS[part][appearanceColours[part]]);

				if (part == 1) {
					model.recolour(Client.SKIN_COLOURS[0], Client.SKIN_COLOURS[appearanceColours[part]]);
				}
			}
		}

		return model;
	}

	@Override
	public final boolean isVisible() {
		return visible;
	}

	@Override
	public final Model model() {
		if (!visible) {
			return null;
		}

		Model appearanceModel = method452();
		if (appearanceModel == null) {
			return null;
		}

		height = ((Renderable) appearanceModel).modelHeight;
		appearanceModel.aBoolean1659 = true;
		if (aBoolean1699) {
			return appearanceModel;
		}

		if (graphic != -1 && currentAnimation != -1) {
			Graphic graphic = Graphic.graphics[super.graphic];
			Model graphicModel = graphic.getModel();
			if (graphicModel != null) {
				Model model = new Model(graphicModel, true, Frame.isInvalid(currentAnimation), false);
				model.translate(0, -graphicHeight, 0);
				model.skin();
				model.apply(graphic.getAnimation().getPrimaryFrame(currentAnimation));
				model.faceGroups = null;
				model.vertexGroups = null;
				if (graphic.getBreadthScale() != 128 || graphic.getDepthScale() != 128) {
					model.scale(graphic.getBreadthScale(), graphic.getBreadthScale(), graphic.getDepthScale());
				}
				model.light(64 + graphic.getModelBrightness(), 850 + graphic.getModelShadow(), -30, -50, -30, true);
				appearanceModel = new Model(new Model[] { appearanceModel, model }, 2);
			}
		}

		if (objectModel != null) {
			if (Client.tick >= objectAppearanceEndTick) {
				objectModel = null;
			}

			if (Client.tick >= objectAppearanceStartTick && Client.tick < objectAppearanceEndTick) {
				Model model = objectModel;
				model.translate(anInt1711 - worldX, anInt1712 - anInt1709, anInt1713 - worldY);

				if (nextStepOrientation == 512) {
					model.rotateClockwise();
					model.rotateClockwise();
					model.rotateClockwise();
				} else if (nextStepOrientation == 1024) {
					model.rotateClockwise();
					model.rotateClockwise();
				} else if (nextStepOrientation == 1536) {
					model.rotateClockwise();
				}

				appearanceModel = new Model(new Model[] { appearanceModel, model }, 2);
				if (nextStepOrientation == 512) {
					model.rotateClockwise();
				} else if (nextStepOrientation == 1024) {
					model.rotateClockwise();
					model.rotateClockwise();
				} else if (nextStepOrientation == 1536) {
					model.rotateClockwise();
					model.rotateClockwise();
					model.rotateClockwise();
				}
				model.translate(worldX - anInt1711, anInt1709 - anInt1712, worldY - anInt1713);
			}
		}

		appearanceModel.aBoolean1659 = true;
		return appearanceModel;
	}

	public final void updateAppearance(Buffer buffer) {
		buffer.setPosition(0);
		gender = buffer.readUByte();
		headIcon = buffer.readUByte();
		npcDefinition = null;
		team = 0;

		for (int bodyPart = 0; bodyPart < 12; bodyPart++) {
			int reset = buffer.readUByte();

			if (reset == 0) {
				appearanceModels[bodyPart] = 0;
				continue;
			}

			int id = buffer.readUByte();
			appearanceModels[bodyPart] = (reset << 8) + id;
			if (bodyPart == 0 && appearanceModels[0] == 65535) {
				npcDefinition = NpcDefinition.lookup(buffer.readUShort());
				break;
			}

			if (appearanceModels[bodyPart] >= 512 && appearanceModels[bodyPart] - 512 < ItemDefinition.getCount()) {
				int team = ItemDefinition.lookup(appearanceModels[bodyPart] - 512).getTeam();
				if (team != 0) {
					this.team = team;
				}
			}
		}

		for (int part = 0; part < 5; part++) {
			int colour = buffer.readUByte();
			if (colour < 0 || colour >= Client.PLAYER_BODY_RECOLOURS[part].length) {
				colour = 0;
			}

			appearanceColours[part] = colour;
		}

		idleAnimation = buffer.readUShort();
		if (idleAnimation == 65535) {
			idleAnimation = -1;
		}

		turnAnimation = buffer.readUShort();
		if (turnAnimation == 65535) {
			turnAnimation = -1;
		}

		walkingAnimation = buffer.readUShort();
		if (walkingAnimation == 65535) {
			walkingAnimation = -1;
		}

		halfTurnAnimation = buffer.readUShort();
		if (halfTurnAnimation == 65535) {
			halfTurnAnimation = -1;
		}

		quarterClockwiseTurnAnimation = buffer.readUShort();
		if (quarterClockwiseTurnAnimation == 65535) {
			quarterClockwiseTurnAnimation = -1;
		}

		quarterAnticlockwiseTurnAnimation = buffer.readUShort();
		if (quarterAnticlockwiseTurnAnimation == 65535) {
			quarterAnticlockwiseTurnAnimation = -1;
		}

		runAnimation = buffer.readUShort();
		if (runAnimation == 65535) {
			runAnimation = -1;
		}

		name = StringUtils.format(StringUtils.decodeBase37(buffer.readLong()));
		combat = buffer.readUByte();
		skill = buffer.readUShort();
		visible = true;
		appearanceHash = 0;

		for (int model = 0; model < 12; model++) {
			appearanceHash <<= 4;
			if (appearanceModels[model] >= 256) {
				appearanceHash += appearanceModels[model] - 256;
			}
		}

		if (appearanceModels[0] >= 256) {
			appearanceHash += appearanceModels[0] - 256 >> 4;
		}
		if (appearanceModels[1] >= 256) {
			appearanceHash += appearanceModels[1] - 256 >> 8;
		}
		for (int colour = 0; colour < 5; colour++) {
			appearanceHash <<= 3;
			appearanceHash += appearanceColours[colour];
		}

		appearanceHash <<= 1;
		appearanceHash += gender;
	}

	private final Model method452() {
		if (npcDefinition != null) {
			int frame = -1;
			if (emoteAnimation >= 0 && animationDelay == 0) {
				frame = Animation.animations[emoteAnimation].getPrimaryFrame(displayedEmoteFrames);
			} else if (movementAnimation >= 0) {
				frame = Animation.animations[movementAnimation].getPrimaryFrame(displayedMovementFrames);
			}
			Model model = npcDefinition.getAnimatedModel(frame, -1, null);
			return model;
		}

		long hash = appearanceHash;
		int primaryFrame = -1;
		int secondaryFrame = -1;
		int shieldModel = -1;
		int weaponModel = -1;
		if (emoteAnimation >= 0 && animationDelay == 0) {
			Animation emote = Animation.animations[emoteAnimation];
			primaryFrame = emote.getPrimaryFrame(displayedEmoteFrames);
			if (movementAnimation >= 0 && movementAnimation != idleAnimation) {
				secondaryFrame = Animation.animations[movementAnimation].getPrimaryFrame(displayedMovementFrames);
			}
			if (emote.getPlayerShieldDelta() >= 0) {
				shieldModel = emote.getPlayerShieldDelta();
				hash += shieldModel - appearanceModels[5] << 40;
			}
			if (emote.getPlayerWeaponDelta() >= 0) {
				weaponModel = emote.getPlayerWeaponDelta();
				hash += weaponModel - appearanceModels[3] << 48;
			}
		} else if (movementAnimation >= 0) {
			primaryFrame = Animation.animations[movementAnimation].getPrimaryFrame(displayedMovementFrames);
		}

		Model model = (Model) models.get(hash);
		if (model == null) {
			boolean invalid = false;
			for (int bodyPart = 0; bodyPart < 12; bodyPart++) {
				int appearanceModel = appearanceModels[bodyPart];
				if (weaponModel >= 0 && bodyPart == 3) {
					appearanceModel = weaponModel;
				}
				if (shieldModel >= 0 && bodyPart == 5) {
					appearanceModel = shieldModel;
				}
				if (appearanceModel >= 256 && appearanceModel < 512 && !IdentityKit.kits[appearanceModel - 256].bodyLoaded()) {
					invalid = true;
				}
				if (appearanceModel >= 512 && !ItemDefinition.lookup(appearanceModel - 512).equipmentReady(gender)) {
					invalid = true;
				}
			}

			if (invalid) {
				if (modelKey != -1L) {
					model = (Model) models.get(modelKey);
				}
				if (model == null) {
					return null;
				}
			}
		}

		if (model == null) {
			Model[] models = new Model[12];
			int count = 0;
			for (int index = 0; index < 12; index++) {
				int part = appearanceModels[index];
				if (weaponModel >= 0 && index == 3) {
					part = weaponModel;
				}
				if (shieldModel >= 0 && index == 5) {
					part = shieldModel;
				}
				if (part >= 256 && part < 512) {
					Model bodyModel = IdentityKit.kits[part - 256].bodyModel();
					if (bodyModel != null) {
						models[count++] = bodyModel;
					}
				}
				if (part >= 512) {
					Model equipment = ItemDefinition.lookup(part - 512).asEquipment(gender);
					if (equipment != null) {
						models[count++] = equipment;
					}
				}
			}

			model = new Model(count, models);
			for (int part = 0; part < 5; part++) {
				if (appearanceColours[part] != 0) {
					model.recolour(Client.PLAYER_BODY_RECOLOURS[part][0],
							Client.PLAYER_BODY_RECOLOURS[part][appearanceColours[part]]);
					if (part == 1) {
						model.recolour(Client.SKIN_COLOURS[0], Client.SKIN_COLOURS[appearanceColours[part]]);
					}
				}
			}

			model.skin();
			model.light(64, 850, -30, -50, -30, true);
			Player.models.put(hash, model);
			modelKey = hash;
		}

		if (aBoolean1699) {
			return model;
		}

		Model empty = Model.EMPTY_MODEL;
		empty.method464(model, Frame.isInvalid(primaryFrame) & Frame.isInvalid(secondaryFrame));
		if (primaryFrame != -1 && secondaryFrame != -1) {
			empty.apply(primaryFrame, secondaryFrame, Animation.animations[emoteAnimation].getInterleaveOrder());
		} else if (primaryFrame != -1) {
			empty.apply(primaryFrame);
		}

		empty.method466();
		empty.faceGroups = null;
		empty.vertexGroups = null;
		return empty;
	}
}