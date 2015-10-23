package net.wasdev.gameon.concierge;

import net.wasdev.gameon.room.common.Room;

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

	public Room getEndRoom() {
		return endRoom;
	}

	public String getStartingEntrance() {
		return startingEntrance;
	}

}
