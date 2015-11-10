package net.wasdev.gameon.room.engine.parser;

import net.wasdev.gameon.room.engine.meta.ContainerDesc;

public class ContainerItem extends Item {
	public final ContainerDesc container;

	public ContainerItem(ContainerDesc container) {
		super(container);
		this.container = container;
	}

	public Type getType() {
		return Type.CONTAINER_ITEM;
	}

	public String getKey() {
		return "/C:";
	}
}