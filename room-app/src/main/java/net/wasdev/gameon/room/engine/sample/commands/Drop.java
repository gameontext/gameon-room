package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Drop extends RoomCommand {
	public Drop(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "DROP";
	}
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			String itemName = getCommandWithoutVerbAsString(cmd);
			//see if we can find the item in the user.
			ItemDesc item = findItemInInventory(itemName, u);
			if(item!=null){
				//add to the room
				room.getItems().add(item);
				//remove from the user.
				//(using copy on write, so it's safe to call remove on list, else we'd call remove on the iter)
				u.inventory.remove(item);
				
				//if the item requested it.. clear its state.
				if(item.clearStateOnDrop){
					item.setState("");
				}						
				room.playerEvent(execBy, "You drop the "+item.name, u.username+" drops the "+item.name);
			}else{
				if(itemName.trim().length()>0){
					room.playerEvent(execBy, "You try to drop the "+itemName+" but it appears you don't actually have one of those.", null);
				}else{
					room.playerEvent(execBy, "The bassline drops away.. leaving the crowd without a beat. Alternatively, specify which item you wish to drop next time.", null);
				}
			}
			
		}else{
			System.out.println("Cannot process drop command for user "+execBy+" as they are not known to the room");
		}
	}
}