package net.wasdev.gameon.room.engine.sample.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class ListPlayers extends CommandHandler {

	private static final CommandTemplate listPlayers = new CommandTemplateBuilder().build(Type.VERB, "ListPlayers").build();
	
	private static final Set<CommandTemplate> templates = Collections.unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[]{
			listPlayers
	})));
	
	
	@Override
	public Set<CommandTemplate> getTemplates() {
		return templates;
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Override
	public void processCommand(Room room, String execBy, ParsedCommand command) {
		User u = room.getUserById(execBy);
		if(u!=null){
			List<String> players = new ArrayList<String>();
			for(User user : room.getAllUsersInRoom()){
				players.add(user.username);
			}
			room.playerEvent(execBy, "The following players are connected: "+players,null);
		}
	}

	@Override
	public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
		room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to listplayers "+cmdWithoutVerb, null);
	}

}
