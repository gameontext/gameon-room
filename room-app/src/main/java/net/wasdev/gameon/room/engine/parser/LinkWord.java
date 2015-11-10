package net.wasdev.gameon.room.engine.parser;

public class LinkWord extends Node {
	public final String word;

	public LinkWord(String word) {
		this.word = word.trim().toUpperCase();
		if (word.contains(":") || word.contains("/")) {
			throw new RuntimeException("Forbidden characters : or / found in linkword name");
		}
	}

	public Type getType() {
		return Type.LINKWORD;
	}

	public String getKey() {
		return "/L:" + word;
	}
}