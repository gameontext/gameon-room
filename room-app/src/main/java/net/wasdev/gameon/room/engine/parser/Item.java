package net.wasdev.gameon.room.engine.parser;

import net.wasdev.gameon.room.engine.meta.ItemDesc;

public abstract class Item extends Node {
	public final ItemDesc item;

	protected Item(ItemDesc item) {
		this.item = item;
	}

	public abstract Type getType();
}