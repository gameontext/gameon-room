package net.wasdev.gameon.room.common;

import java.util.List;

public class Room {

	private String roomName;
	
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public Room(String roomName) {
		setRoomName(roomName);
	}
	public String getAttribute(String attributeName) {
		return "easy";
	}
	public void setAttribute(String name, String value) {
		// TODO Auto-generated method stub
		
	}
	public List<String> getExits() {
		return null;
	}
}
