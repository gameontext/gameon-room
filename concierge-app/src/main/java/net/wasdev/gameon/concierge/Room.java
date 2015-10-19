package net.wasdev.gameon.concierge;

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
}
