package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;

public class Reset extends RoomCommand {
	public Reset(){
	}
	public boolean isHidden(){ return true; }
	public String getVerb(){
		return "RESET";
	}
	public void process(String execBy, String cmd, Room room){
		//we'll add the ability to identify admin users via the db later
		if("twitter:281946000".equals(execBy)){
			room.resetRoom();
			room.playerEvent(execBy, "Reset executed. Verify room contents.","An odd feeling comes over you, like you just felt a glitch in the matrix");
		}else{
			User u = room.getUserById(execBy);
			if(u!=null){
				room.playerEvent(execBy, "You hit the invisble reset button, hoping it does something. A beige futuristic archway appears.", u.username+" says \"Computer. Arch!\" and an archway appears, "+u.username+" enters the archway and requests a holodeck reset.");
				room.roomEvent("Access denied: "+u.username+" is not recognised by the ships computer as being a command officer. This infraction has been noted.");
				room.roomEvent("The archway disappears");
			}
		}
	}
}