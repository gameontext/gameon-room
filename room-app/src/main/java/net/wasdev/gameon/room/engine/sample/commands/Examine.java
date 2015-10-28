package net.wasdev.gameon.room.engine.sample.commands;

import java.util.ArrayList;
import java.util.List;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Examine extends RoomCommand {
	public Examine(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "EXAMINE";
	}
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			String itemName = getCommandWithoutVerbAsString(cmd);
			//see if we can find the item in the room or inventory
			ItemDesc item = findItemInRoomOrInventory(u, itemName, room);
			if(item!=null){
				if(item instanceof ContainerDesc){
					ContainerDesc box = (ContainerDesc)item;
					StringBuilder result = new StringBuilder();
					result.append(item.getDescription(execBy, cmd, room));
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
							for(ItemDesc i : box.items){
								itemNames.add(i.name);
							}
							result.append(itemNames.toString());
						}
					}else{
						result.append("Maybe there's something inside, you can't tell.");
					}
					room.playerEvent(execBy, result.toString(), null);
				}else{
					room.playerEvent(execBy, item.getDescription(execBy, cmd, room), null);
				}
			}else{
				if(itemName.trim().length()==0){
					room.playerEvent(execBy, "You want to examine what??!",null);
				}else{
					room.playerEvent(execBy, "You search for the "+itemName+" to examine, but cannot seem to locate it anywhere!",null);
				}
			}
		}else{
			System.out.println("Cannot process examine command for user "+execBy+" as they are not known to the room");
		}

	}
}