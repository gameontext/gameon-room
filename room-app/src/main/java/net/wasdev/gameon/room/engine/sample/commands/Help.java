package net.wasdev.gameon.room.engine.sample.commands;

import java.util.ArrayList;
import java.util.List;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Engine;
import net.wasdev.gameon.room.engine.Room;

public class Help extends RoomCommand {
	public Help(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "HELP";
	}
	public void process(String execBy, String cmd, Room room){
		List<String> currentCmds = new ArrayList<String>();
		for(RoomCommand c: room.getCommands()){
			if(!c.isHidden()){
				currentCmds.add("/"+c.getVerb());
			}
		}
		room.playerEvent(execBy, "The following commands are supported: "+currentCmds,null);
	}
}