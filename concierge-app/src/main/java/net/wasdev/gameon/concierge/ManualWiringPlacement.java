package net.wasdev.gameon.concierge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wasdev.gameon.room.common.Exit;
import net.wasdev.gameon.room.common.Room;

public class ManualWiringPlacement implements PlacementStrategy {
	
	Map<String, List<Exit>> exitMap = new HashMap<String, List<Exit>>(); 
	
	@Override
	public String getConnectingRooms(String currentRoom, String exitName) {
		String roomName = null;
		List<Exit> currentRoomExits = exitMap.get(currentRoom);
		if (currentRoomExits != null) {
			for (Exit exit : currentRoomExits) {
				if (exit.getName().equals(exitName)) {
					roomName = exit.getRoom();
				}
			}
		}
		return roomName;
	}

	@Override
	public void placeRoom(Room room) {
		exitMap.put(room.getRoomName(), room.getExits());
	}

}
