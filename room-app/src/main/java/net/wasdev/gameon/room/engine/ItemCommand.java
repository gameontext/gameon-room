package net.wasdev.gameon.room.engine;

import net.wasdev.gameon.room.engine.meta.ItemDesc;

public abstract class ItemCommand extends CommandProcessor {
	public abstract void processCommand(ItemDesc item, String execBy, String cmd, Room room);
}
