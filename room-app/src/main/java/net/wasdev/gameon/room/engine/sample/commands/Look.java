package net.wasdev.gameon.room.engine.sample.commands;

import java.util.ArrayList;
import java.util.List;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ExitDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Look extends RoomCommand {
	public Look(){
	}
	public String getVerb(){
		return "LOOK";
	}
	public boolean isHidden(){ return false; }
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			//did they do /look ? or /look object or /look at object ?
			String restOfCommand = getCommandWithoutVerbAsString(cmd);
			if(restOfCommand.length()==0){					
				List<String> invItems = new ArrayList<String>();
				List<String> roomItems = new ArrayList<String>();
				for(ItemDesc i : room.getItems()){
					roomItems.add(i.name);
				}
				for(ItemDesc i : u.inventory){
					invItems.add(i.name);
				}
				room.locationEvent(execBy, room, room.getRoomDescription(), room.getExits(), roomItems, invItems);
			}else{
				//priority goes to looking if we can match an item next.. in case anyone adds an "AT AT" as an item ;p
				String item = getItemNameFromCommand(restOfCommand, room, u);
				if(item==null){				
					String nextWord = getFirstWordFromCommand(restOfCommand);
					if("AT".equalsIgnoreCase(nextWord)){
						restOfCommand = getCommandWithoutVerbAsString(restOfCommand);
						item = getItemNameFromCommand(restOfCommand, room, u);
					}else{
						//wasn't at.. is it a direction?
						for(ExitDesc ed : room.getExits()){
							if(ed.direction.toString().toUpperCase().equals(nextWord.toUpperCase()) || ed.direction.toLongString().toUpperCase().equals(nextWord.toUpperCase())){
								room.playerEvent(execBy, ed.handler.getDescription(execBy, ed, room), null);
								//early return.. avoid sending the 'rest of command' error below =)
								return;
							}
						}
					}
				}
				if(item!=null){
					//delegate to examine.
					Examine examineCommand = new Examine();						
					examineCommand.process(execBy, "EXAMINE "+item, room);
				}else{
					room.playerEvent(execBy, "You pull out your magnifying glass to look at '"+restOfCommand+"' but realise you have no idea what that is.", null);
				}
				
			}
		}
	}
}