package net.wasdev.gameon.room.engine.sample.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Examine extends CommandHandler {

	private static final CommandTemplate examineInventoryItem = new CommandTemplateBuilder().build(Type.VERB, "Examine").build(Type.INVENTORY_ITEM).build();
	private static final CommandTemplate examineRoomItem = new CommandTemplateBuilder().build(Type.VERB, "Examine").build(Type.ROOM_ITEM).build();
	private static final CommandTemplate examineItemInContainer = new CommandTemplateBuilder().build(Type.VERB, "Examine").build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();
	
	private static final Set<CommandTemplate> templates = Collections.unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[]{
			examineInventoryItem,
			examineRoomItem,
			examineItemInContainer
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
		String key = command.key;
		User u = room.getUserById(execBy);
		if(u!=null){
			//in all 3 supported cases, we expect an item argument. 
			Item i = (Item)command.args.get(0);
			//if we examine something in the room, tell the room we did so.
			if(key.equals(examineRoomItem.key)){		
				if(i.item instanceof ContainerDesc){
					ContainerDesc box = (ContainerDesc)i.item;
					StringBuilder result = new StringBuilder();
					result.append(i.item.getDescription(execBy, command.originalCommand, room));
					result.append(" ");
					boolean accessAllowed = true;
					if(box.access !=null ){
						accessAllowed = box.access.verifyAccess(box, execBy, room);
					}
					if(accessAllowed){
						if(box.items.isEmpty()){
							result.append("The "+box.name+" appears to be empty.");
						}else{
							result.append("There appear to be the following items inside the "+box.name);
							List<String> itemNames = new ArrayList<String>();
							for(ItemDesc id : box.items){
								itemNames.add(id.name);
							}
							result.append(itemNames.toString());
						}
					}else{
						result.append("Maybe there's something inside, you can't tell.");
					}
					room.playerEvent(execBy, result.toString(), u.username+" examines the "+i.item.name);
				}else{
					room.playerEvent(execBy, i.item.getDescription(execBy, command.originalCommand, room), u.username+" examines the "+i.item.name);
				}
			}else{
				//for the other cases, we don't say.. no need to tell other people if you are examining items you hold.
				//or if you are examining items in boxes they may not be able to see in.
				room.playerEvent(execBy, i.item.getDescription(execBy, command.originalCommand, room), null);
			}
		}
	}

	@Override
	public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
		room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to examine "+cmdWithoutVerb, null);
	}

}
