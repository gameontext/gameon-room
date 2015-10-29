package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ExitDesc;

public class Go extends RoomCommand {
	public Go(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "GO";
	}
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			String direction = getCommandWithoutVerbAsString(cmd).toUpperCase().trim();
			for(ExitDesc ed : room.getExits()){
				if( ed.direction.toString().toUpperCase().equals(direction.toUpperCase()) ||
					ed.direction.toLongString().toUpperCase().equals(direction.toUpperCase())){
					if(ed.handler.isVisible() && ed.handler.isTraversable(execBy,ed, room)){
						room.exitEvent(execBy,ed.handler.getSelfDepartMessage(execBy, ed, room), ed.direction.toString());
					}else{
						room.playerEvent(execBy, "You don't appear able to go "+ed.direction.toLongString(),null);
					}
				}
			}
		}else{
			System.out.println("Cannot process examine command for user "+execBy+" as they are not known to the room");
		}

	}
}