package net.wasdev.gameon.room.engine.sample.commands;

import net.wasdev.gameon.room.engine.RoomCommand;
import net.wasdev.gameon.room.engine.Room;

public class Quit extends RoomCommand {
	public Quit(){
	}
	public boolean isHidden(){ return false; }
	public String getVerb(){
		return "QUIT";
	}
	public void process(String execBy, String cmd, Room room){
		room.playerEvent(execBy,"QUIT?? HA! You can NEVER leave !!! Muahahah!",null);
	}
}