package net.wasdev.gameon.room.engine;

public abstract class RoomCommand extends CommandProcessor {
	public abstract boolean isHidden();
	public abstract String getVerb();
	public abstract void process(String execBy, String cmd, Room room);
}
