package net.wasdev.gameon.concierge;

import java.util.UUID;

public class Connection {

	private UUID startRoom;
	private UUID endRoom;
	private String startingEntrance;
	public Connection(UUID startingRoom, UUID endRoom, String startingEntrance) {
		this.startingEntrance = startingEntrance;
		this.startRoom = startingRoom;
		this.endRoom = endRoom;
	}
	public UUID getStartRoom() {
		return startRoom;
	}

	public UUID getEndRoom() {
		return endRoom;
	}

	public String getStartingEntrance() {
		return startingEntrance;
	}

}
