package net.wasdev.gameon.room.engine.parser;

import java.util.List;

public class CommandTemplate {
	public static class ParseNode {
		public Node.Type type;
		public String data;
	}

	public final List<CommandTemplate.ParseNode> template;
	public final String key;

	public CommandTemplate(String key, List<CommandTemplate.ParseNode> template) {
		this.key = key;
		this.template = template;
	}
}