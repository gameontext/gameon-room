package net.wasdev.gameon.room.engine.parser;

public class User extends Node {
	public final net.wasdev.gameon.room.engine.User user;

	public User(net.wasdev.gameon.room.engine.User user) {
		this.user = user;
	}

	public Type getType() {
		return Type.USER;
	}

	public String getKey() {
		return "/U:";
	}
}