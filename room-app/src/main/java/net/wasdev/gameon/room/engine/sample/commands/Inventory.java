package net.wasdev.gameon.room.engine.sample.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Inventory extends CommandHandler {

	private static final CommandTemplate inventory = new CommandTemplateBuilder().build(Type.VERB, "Inventory").build();
	
	private static final Set<CommandTemplate> templates = Collections.unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[]{
			inventory
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
			if(u.inventory.isEmpty()){
				room.playerEvent(execBy,"You do not appear to be carrying anything.",null);
			}else{
				StringBuilder sb = new StringBuilder();
				sb.append("You are carrying; ");
				boolean first=true;
				for(ItemDesc item : u.inventory){
					if(!first)sb.append(", ");
					sb.append(item.name);
					first=false;
				}
				room.playerEvent(execBy,sb.toString(),null);
			}				
		}
	}

	@Override
	public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
		room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to inventory "+cmdWithoutVerb, null);
	}

}
