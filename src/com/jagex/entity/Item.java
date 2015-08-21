package com.jagex.entity;

import com.jagex.cache.def.ItemDefinition;
import com.jagex.entity.model.Model;

public final class Item extends Renderable {

	// Class30_Sub2_Sub4_Sub2

	private int amount;
	private int id;

	public int getAmount() {
		return amount;
	}

	public int getId() {
		return id;
	}

	@Override
	public final Model model() {
		return ItemDefinition.lookup(id).asGroundStack(amount);
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setId(int id) {
		this.id = id;
	}
}