package com.jagex.entity;

import com.jagex.Client;
import com.jagex.cache.anim.Animation;
import com.jagex.cache.def.ObjectDefinition;
import com.jagex.entity.model.Model;
import com.jagex.setting.VariableBits;

public class RenderableObject extends Renderable {

	public static Client client;
	int morphisms[];
	int morphVarbitsIndex;
	int morphVariableIndex;
	private Animation animation;
	private int anInt1603;
	private int anInt1605;
	private int anInt1606;
	private int centre;
	private int currentFrameId;
	private int id;
	private int currentFrameDuration;
	private int orientation;
	private int type;

	public RenderableObject(int id, int orientation, int type, int aY, int bY, int cY, int dY, int animationId,
			boolean randomFrame) {
		this.id = id;
		this.type = type;
		this.orientation = orientation;
		anInt1603 = aY;
		centre = bY;
		anInt1605 = cY;
		anInt1606 = dY;

		if (animationId != -1) {
			animation = Animation.animations[animationId];
			currentFrameId = 0;
			currentFrameDuration = Client.tick;
			if (randomFrame && animation.getLoopOffset() != -1) {
				currentFrameId = (int) (Math.random() * animation.getFrameCount());
				currentFrameDuration -= (int) (Math.random() * animation.duration(currentFrameId));
			}
		}

		ObjectDefinition definition = ObjectDefinition.lookup(id);
		morphVarbitsIndex = definition.getMorphVarbitIndex();
		morphVariableIndex = definition.getMorphVariableIndex();
		morphisms = definition.getMorphisms();
	}

	@Override
	public final Model model() {
		int lastFrame = -1;
		if (animation != null) {
			int tickDelta = Client.tick - currentFrameDuration;
			if (tickDelta > 100 && animation.getLoopOffset() > 0) {
				tickDelta = 100;
			}

			while (tickDelta > animation.duration(currentFrameId)) {
				tickDelta -= animation.duration(currentFrameId);
				currentFrameId++;
				if (currentFrameId < animation.getFrameCount()) {
					continue;
				}
				currentFrameId -= animation.getLoopOffset();
				if (currentFrameId >= 0 && currentFrameId < animation.getFrameCount()) {
					continue;
				}
				animation = null;
				break;
			}

			currentFrameDuration = Client.tick - tickDelta;
			if (animation != null) {
				lastFrame = animation.getPrimaryFrame(currentFrameId);
			}
		}

		ObjectDefinition definition = morphisms != null ? morph() : ObjectDefinition.lookup(id);

		return (definition == null) ? null : definition.modelAt(type, orientation, anInt1603, centre, anInt1605, anInt1606,
				lastFrame);
	}

	public final ObjectDefinition morph() {
		int state = -1;
		if (morphVarbitsIndex != -1) {
			VariableBits bits = VariableBits.bits[morphVarbitsIndex];
			int var = bits.getSetting();
			int low = bits.getLow();
			int high = bits.getHigh();
			int mask = Client.BIT_MASKS[high - low];
			state = client.settings[var] >> low & mask;
		} else if (morphVariableIndex != -1) {
			state = client.settings[morphVariableIndex];
		}

		if (state < 0 || state >= morphisms.length || morphisms[state] == -1) {
			return null;
		}
		return ObjectDefinition.lookup(morphisms[state]);
	}

}