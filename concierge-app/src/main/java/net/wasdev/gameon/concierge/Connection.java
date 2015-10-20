package net.wasdev.gameon.concierge;

<<<<<<< Upstream, based on origin/master
import net.wasdev.gameon.room.common.Room;
=======
import java.util.UUID;
>>>>>>> 174a249 Simple 2D room placement

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
