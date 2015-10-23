package net.wasdev.gameon.concierge;

import java.util.List;
import java.util.UUID;

import net.wasdev.gameon.room.common.Room;

public interface PlacementStrategy {
	void placeRoom(Room room);

	List<Room> getConnectingRooms(String currentRoomId, String exitName);
}
