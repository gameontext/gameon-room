package net.wasdev.gameon.concierge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.wasdev.gameon.room.common.Exit;
import net.wasdev.gameon.room.common.Room;

public class ManualWiringPlacement implements PlacementStrategy {
	
	List<String> exitNodes = new ArrayList<String>();
	Map<UUID, List<Exit>> exitMap = new HashMap<UUID, List<Exit>>(); 
	
	private Map<String, UUID> roomNameToUUIDMapping = new HashMap<String, UUID>();
	
	@Override
	public UUID getConnectingRoom(UUID currentRoom, String exitName) {
		List<Exit> currentRoomExits = exitMap.get(currentRoom);
		String roomName = null;
		UUID nextRoom = null;
		for (Exit exit : currentRoomExits) {
			if (exit.getName().equals(exitName)) {
				roomName = exit.getRoom();
			}
		}
		if (roomName != null) {
			nextRoom = roomNameToUUIDMapping.get(roomName);
		}
		return nextRoom;
	}

	@Override
	public void placeRoom(UUID roomUUID, Room room) {
		roomNameToUUIDMapping.put(room.getRoomName(), roomUUID);
		
		exitMap.put(roomUUID, room.getExits());
	}

}
