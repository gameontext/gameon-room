package net.wasdev.gameon.concierge;

import net.wasdev.gameon.room.common.Room;

public interface PlacementStrategy {
	void placeRoom(Room room);

	String getConnectingRooms(String currentRoomId, String exitName);
}
