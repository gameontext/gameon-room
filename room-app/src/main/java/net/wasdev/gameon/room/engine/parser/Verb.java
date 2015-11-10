package net.wasdev.gameon.room.engine.parser;

public class Verb extends Node {
	public final String name;

	public Verb(String name) {
		this.name = name.trim().toUpperCase();
		if (name.contains(":") || name.contains("/")) {
			throw new RuntimeException("Forbidden characters : or / found in verb name");
		}
	}

	public Type getType() {
		return Type.VERB;
	}

	public String getKey() {
		return "/V:" + name;
	}
}