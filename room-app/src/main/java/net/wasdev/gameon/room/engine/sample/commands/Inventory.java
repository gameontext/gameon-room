package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Inventory extends RoomCommand {
	public Inventory(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "INVENTORY";
	}
	public void process(String execBy, String cmd, Room room){
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
}