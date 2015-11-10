package net.wasdev.gameon.room.engine.parser;

import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class ItemInContainerItem extends Item {
	public final ContainerDesc container;

	public ItemInContainerItem(ContainerDesc container, ItemDesc itemInsideContainer) {
		super(itemInsideContainer);
		this.container = container;
	}

	public Type getType() {
		return Type.ITEM_INSIDE_CONTAINER_ITEM;
	}

	public String getKey() {
		return "/B:";
	}
}