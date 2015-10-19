package net.wasdev.gameon.concierge;

public class Connection {

	private Room startRoom;
	private Room endRoom;
	private String startingEntrance;
	public Connection(Room startingRoom, Room endRoom, String startingEntrance) {
		this.startingEntrance = startingEntrance;
		this.startRoom = startingRoom;
		this.endRoom = endRoom;
	}
	public Room getStartRoom() {
		return startRoom;
	}
	public void setStartRoom(Room startRoom) {
		this.startRoom = startRoom;
	}
	public Room getEndRoom() {
		return endRoom;
	}
	public void setEndRoom(Room endRoom) {
		this.endRoom = endRoom;
	}
	public String getStartingEntrance() {
		return startingEntrance;
	}
	public void setStartingEntrance(String startingEntrance) {
		this.startingEntrance = startingEntrance;
	}
}
