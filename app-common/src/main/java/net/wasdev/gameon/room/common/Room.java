package net.wasdev.gameon.room.common;

import java.util.List;
import java.util.UUID;

public class Room {

	private UUID assignedID;
	
	private String roomName;
	
	public Room() {
		//no-args constructor to allow JSON serialisation
	}
	
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
	}
	public List<String> getExits() {
		return null;
	}
	public UUID getAssignedID() {
		return assignedID;
	}
	
	public void setAssignedID(UUID assignedID) {
		this.assignedID = assignedID;
	}
	public void setExits(List<Exit> exits) {
		// TODO Auto-generated method stub
		
	}
}
