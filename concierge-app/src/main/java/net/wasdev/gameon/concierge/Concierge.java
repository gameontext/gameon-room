package net.wasdev.gameon.concierge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Application;

import net.wasdev.gameon.room.common.Room;

public class Concierge extends Application {

	Map<UUID, Room> roomDirectory = new HashMap<UUID, Room>();
	Room startingRoom = null;
	
	PlacementStrategy ps = new Simple2DPlacement();
	
	public Concierge(PlacementStrategy placementStrategy) {
			ps = placementStrategy;
	}

	public Concierge() {
	}

	public Room getStartingRoom() {
		return startingRoom;
	}
	
	public Room exitRoom(UUID currentRoom, String exitName) {
		Room nextRoom = null;
		UUID nextRoomUUID = ps.getConnectingRoom(currentRoom, exitName);
		if (nextRoomUUID != null) {
			nextRoom = roomDirectory.get(nextRoomUUID);
		}
		return nextRoom;
	}

	public UUID registerRoom(Room room) {
	
		if (startingRoom == null) {
			startingRoom = room;
		}
		
		UUID roomUUID = UUID.randomUUID();
		room.setAssignedID(roomUUID);
		roomDirectory.put(roomUUID, room);
		ps.placeRoom(roomUUID, room);
		return roomUUID;
	}
	
	
}
