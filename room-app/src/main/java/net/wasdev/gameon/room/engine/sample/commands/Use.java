package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Use extends RoomCommand {
	public Use(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "USE";
	}
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			String restOfCommand = getCommandWithoutVerbAsString(cmd);
			String itemName = getItemNameFromCommand(restOfCommand, room, u);
			//see if we can find the item in the room or inventory
			ItemDesc item = findItemInRoomOrInventory(u, itemName, room);
			if(item==null){
				if(restOfCommand.trim().length()==0){
					room.playerEvent(execBy, "Normally, in these text adventurey things, you'd specify the item you wish to use, but you win a prize for being different.",null);
				}else{
					room.playerEvent(execBy, "You search for the "+restOfCommand+" to use, but cannot seem to locate it anywhere!",null);
				}
			}else{
				if(item.handler!=null){
					item.handler.processCommand(item, execBy, cmd, room);
				}else{
					room.playerEvent(execBy, "You stare confused at the "+itemName+" unsure quite what to do with it!",null);
				}
			}
		}else{
			System.out.println("Cannot process use command for user "+execBy+" as they are not known to the room");
		}
	}
}