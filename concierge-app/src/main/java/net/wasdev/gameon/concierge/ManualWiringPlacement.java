package net.wasdev.gameon.concierge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.wasdev.gameon.room.common.Room;

public class ManualWiringPlacement implements PlacementStrategy {
	
	List<String> exitNodes = new ArrayList<String>();
	private UUID roomUUID;
	
	private Map<UUID, String> roomNameToUUIDMapping = new HashMap<UUID, String>();
	

	@Override
	public UUID getConnectingRoom(UUID currentRoom, String exit) {
		
		return roomUUID;
	}

	@Override
	public void placeRoom(UUID roomUUID, Room room) {
		roomNameToUUIDMapping.put(roomUUID, room.getRoomName());
		// Loop through exits
		
		this.roomUUID = roomUUID;

	}

}
