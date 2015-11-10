package net.wasdev.gameon.room.engine.parser;

import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class RoomItem extends Item {
	public RoomItem(ItemDesc item) {
		super(item);
	}

	public Type getType() {
		return Type.ROOM_ITEM;
	}

	public String getKey() {
		return "/R:";
	}
}