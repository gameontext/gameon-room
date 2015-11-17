package net.wasdev.gameon.room.engine.sample.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Exit;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Go extends CommandHandler {

	private static final CommandTemplate go = new CommandTemplateBuilder().build(Type.VERB, "Go").build(Type.EXIT).build();

	private static final Set<CommandTemplate> templates = Collections.unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[] { 
			go 
	})));

	@Override
	public Set<CommandTemplate> getTemplates() {
		return templates;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public void processCommand(Room room, String execBy, ParsedCommand command) {
		User u = room.getUserById(execBy);
		if (u != null) {
			Exit e = (Exit) command.args.get(0);
			if (e.exit.handler.isVisible() && e.exit.handler.isTraversable(execBy, e.exit, room)) {
				room.exitEvent(execBy, e.exit.handler.getSelfDepartMessage(execBy, e.exit, room), e.exit.direction.toString());
			} else {
				room.playerEvent(execBy, "You don't appear able to go " + e.exit.direction.toLongString(), null);
			}
		} else {
			System.out.println("Cannot process go command for user " + execBy + " as they are not known to the room");
		}
	}

	@Override
	public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
		room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to go " + cmdWithoutVerb, null);
	}

}
