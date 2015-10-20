package net.wasdev.gameon.concierge;

import java.util.UUID;

public interface PlacementStrategy {
	UUID getConnectingRoom(UUID currentRoom, String exit);

	void placeRoom(UUID roomUUID, Room room);
}
