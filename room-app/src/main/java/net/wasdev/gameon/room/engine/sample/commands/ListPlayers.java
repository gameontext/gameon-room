package net.wasdev.gameon.room.engine.sample.commands;

import java.util.ArrayList;
import java.util.List;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;

public class ListPlayers extends RoomCommand {
	public ListPlayers(){
	}
	public boolean isHidden(){ return true; }
	public String getVerb(){
		return "LISTPLAYERS";
	}
	public void process(String execBy, String cmd, Room room){
		User u = room.getUserById(execBy);
		if(u!=null){
			List<String> players = new ArrayList<String>();
			for(User user : room.getAllUsersInRoom()){
				players.add(user.username);
			}
			room.playerEvent(execBy, "The following players are connected: "+players,null);
		}
	}
}