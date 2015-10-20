package net.wasdev.gameon.concierge;

import java.util.UUID;

import net.wasdev.gameon.room.common.Room;

public interface PlacementStrategy {
	UUID getConnectingRoom(UUID currentRoom, String exit);

	void placeRoom(UUID roomUUID, Room room);
}
