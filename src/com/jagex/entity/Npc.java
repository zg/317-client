package com.jagex.entity;

import com.jagex.cache.anim.Animation;
import com.jagex.cache.anim.Frame;
import com.jagex.cache.anim.Graphic;
import com.jagex.cache.def.NpcDefinition;
import com.jagex.entity.model.Model;

public final class Npc extends Mob {

	private NpcDefinition definition;

	@Override
	public final boolean isVisible() {
		return definition != null;
	}

	@Override
	public final Model model() {
		if (definition == null) {
			return null;
		}

		Model animated = getAnimatedModel();
		if (animated == null) {
			return null;
		}

		height = animated.modelHeight;
		if (super.graphic != -1 && currentAnimation != -1) {
			Graphic graphic = Graphic.graphics[super.graphic];
			Model graphicModel = graphic.getModel();

			if (graphicModel != null) {
				int frame = graphic.getAnimation().getPrimaryFrame(currentAnimation);
				Model model = new Model(graphicModel, true, Frame.isInvalid(frame), false);
				model.translate(0, -graphicHeight, 0);
				model.skin();
				model.apply(frame);
				model.faceGroups = null;
				model.vertexGroups = null;
				if (graphic.getBreadthScale() != 128 || graphic.getDepthScale() != 128) {
					model.scale(graphic.getBreadthScale(), graphic.getBreadthScale(), graphic.getDepthScale());
				}

				model.light(64 + graphic.getModelBrightness(), 850 + graphic.getModelShadow(), -30, -50, -30, true);
				animated = new Model(new Model[] { animated, model }, 2);
			}
		}

		if (definition.getSize() == 1) {
			animated.aBoolean1659 = true;
		}

		return animated;
	}

	private final Model getAnimatedModel() {
		if (emoteAnimation >= 0 && animationDelay == 0) {
			int emote = Animation.animations[emoteAnimation].getPrimaryFrame(displayedEmoteFrames);
			int movement = -1;

			if (movementAnimation >= 0 && movementAnimation != idleAnimation) {
				movement = Animation.animations[movementAnimation].getPrimaryFrame(displayedMovementFrames);
			}
			return definition.getAnimatedModel(emote, movement, Animation.animations[emoteAnimation].getInterleaveOrder());
		}

		int movement = -1;
		if (movementAnimation >= 0) {
			movement = Animation.animations[movementAnimation].getPrimaryFrame(displayedMovementFrames);
		}

		return definition.getAnimatedModel(movement, -1, null);
	}

	/**
	 * Gets the NpcDefinition.
	 *
	 * @return The definition.
	 */
	public NpcDefinition getDefinition() {
		return definition;
	}

	/**
	 * Sets the NpcDefinition.
	 *
	 * @param definition The definition.
	 */
	public void setDefinition(NpcDefinition definition) {
		this.definition = definition;
	}

}