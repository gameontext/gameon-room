package net.wasdev.gameon.room.engine.parser;

import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class InventoryItem extends Item {
	public InventoryItem(ItemDesc item) {
		super(item);
	}

	public Type getType() {
		return Type.INVENTORY_ITEM;
	}

	public String getKey() {
		return "/I:";
	}
}