package net.wasdev.gameon.concierge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.wasdev.gameon.room.common.Room;

public class DynamicGrowthPlacement implements PlacementStrategy {
	
	List<String> exitNodes = new ArrayList<String>();
	private UUID roomUUID;
	

	@Override
	public UUID getConnectingRoom(UUID currentRoom, String exit) {
		
		return roomUUID;
	}

	@Override
	public void placeRoom(UUID roomUUID, Room room) {
		this.roomUUID = roomUUID;

	}

}
