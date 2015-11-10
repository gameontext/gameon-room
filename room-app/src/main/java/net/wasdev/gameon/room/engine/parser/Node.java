package net.wasdev.gameon.room.engine.parser;

public abstract class Node {
	public enum Type {
		VERB, LINKWORD, ROOM_ITEM, INVENTORY_ITEM, CONTAINER_ITEM, ITEM_INSIDE_CONTAINER_ITEM, EXIT, USER
	}

	public abstract Node.Type getType();

	public abstract String getKey();
}