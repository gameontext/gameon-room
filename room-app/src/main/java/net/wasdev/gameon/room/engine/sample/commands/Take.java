package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Take extends RoomCommand {
	public Take(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "TAKE";
	}
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			String restOfCommand = getCommandWithoutVerbAsString(cmd);
			String itemName = getItemNameFromCommand(restOfCommand, room, u);
			//see if we can find the item in the room.
			ItemDesc item = findItemInRoom(itemName, room);
			if(item!=null){
				restOfCommand = getCommandWithoutVerbAndItemAsString(cmd, item).trim();
				if("".equals(restOfCommand.trim())){					
					if(item.takeable){
						//we have found a match!						
						//add to the player
						u.inventory.add(item);
						//remove from the room. 
						//(using copy on write, so it's safe to call remove on list, else we'd call remove on the iter)
						room.getItems().remove(item);
						
						room.playerEvent(execBy, "You pick up the "+item.name, u.username+" picks up the "+item.name);
					}else{
						room.playerEvent(execBy, "You try really hard to pick up the "+item.name+" but it's just too tiring.", u.username+" tries to pick up the "+item.name+" and fails.");
					}
				}else{
					room.playerEvent(execBy, "You reach out to take the "+item.name+" but then are confused by what you meant by '"+restOfCommand+"' so leave it there instead.",null);
				}
			}else{
				//item was not in room.. this gets a little messy.. as we now need to find items inside containers
				item = findItemInContainers(itemName, room);
				if(item!=null){
					restOfCommand = getCommandWithoutVerbAndItemAsString(cmd, item);
					String nextWord = getFirstWordFromCommand(restOfCommand);
					if("FROM".equalsIgnoreCase(nextWord)){
						//skip from
						restOfCommand = getCommandWithoutVerbAsString(restOfCommand);
						//from what?
						String otherItemName = getItemNameFromCommand(restOfCommand, room, u);
						if(otherItemName!=null){
							ItemDesc otherItem = findItemInRoomOrInventory(u, otherItemName, room);
							if(otherItem instanceof ContainerDesc){
								ContainerDesc box = (ContainerDesc)otherItem;
								//access check..
								boolean accessAllowed = true;
								if(box.access !=null ){
									accessAllowed = box.access.verifyAccess(box, execBy, room);
								}
								if(accessAllowed){
									if(box.items.contains(item)){
										room.playerEvent(execBy, "You take the "+item.name+" from the "+otherItem.name, u.username+" takes the "+item.name+" from the "+otherItem.name);
										box.items.remove(item);
										u.inventory.add(item);
									}else{
										room.playerEvent(execBy, "You look in the "+otherItem.name+" but the "+item.name+" does not appear to be there to take.", null);
									}
								}else{
									room.playerEvent(execBy, "You appear unable to take things from "+box.name, null);
								}
							}else{
								room.playerEvent(execBy, "The "+otherItemName+" doesn't look like the kind of thing you should be rummaging around inside.", null);
							}
						}else{
							if(restOfCommand.trim().length()>0){
								room.playerEvent(execBy, "I'm really not sure where to find '"+restOfCommand+"' to do that with", null);
							}else{
								room.playerEvent(execBy, "You want to take the "+item.name+" from where??!", null);
							}
						}
					}else{
						//if we got here, the item was in a container in the room, and the player didn't use the word 'from'
						//but 
						String originalInputWithoutCommand = getCommandWithoutVerbAsString(cmd);
						if(restOfCommand.trim().length()>0){
							room.playerEvent(execBy, "You reach out to take the "+originalInputWithoutCommand+" but then are confused by what you meant by '"+restOfCommand+"' so leave it there instead.",null);
						}else{
							room.playerEvent(execBy, "You search for the "+originalInputWithoutCommand+" to pick up, but cannot seem to locate it anywhere!",null);
						}
					}
				}else{
					if(restOfCommand.trim().length()>0){
						room.playerEvent(execBy, "You search for the "+restOfCommand+" to pick up, but cannot seem to locate it anywhere!",null);
					}else{
						room.playerEvent(execBy, "Here is a list of words that rhyme with Take, Rake, Lake, Bake, Fake. If you wish to pick up an item, you need to say which item you wish to Take.",null);
					}
				}
			}
		}else{
			System.out.println("Cannot process take command for user "+execBy+" as they are not known to the room");
		}
	}
}