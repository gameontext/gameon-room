package net.wasdev.gameon.room.engine.parser;

import net.wasdev.gameon.room.engine.meta.ExitDesc;

public class Exit extends Node {
	public final ExitDesc exit;

	public Exit(ExitDesc exit) {
		this.exit = exit;
	}

	public Type getType() {
		return Type.EXIT;
	}

	public String getKey() {
		return "/E:";
	}
}