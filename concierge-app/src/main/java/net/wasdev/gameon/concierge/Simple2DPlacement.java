package net.wasdev.gameon.concierge;

import java.util.UUID;

import net.wasdev.gameon.room.common.Room;

public class Simple2DPlacement implements PlacementStrategy {
	
	UUID[][] roomGrid = new UUID[10][10];

	@Override
	public UUID getConnectingRoom(UUID currentRoom, String exit) {
		int row = -1;
		int column = -1;
		UUID returnedUUID = null;
		for (int j =0; j<10; j++) {
			for (int i =0 ; i< 10; i++) {
				UUID currentUUID = roomGrid[i][j];
				if (currentUUID != null && currentUUID.equals(currentRoom)) {
					row = i;
					column = j;
					break;
				}
			}
			if (row > -1) break;
		}
		
		if (row > -1 && column > -1) {
			if ("North".equals(exit)) {
				column ++;
			} else if ("South".equals(exit)) {
				column --;
			} else if ("East".equals(exit)) {
				row ++;
			} else if ("West".equals(exit)) {
				row --;
			}
			
			if (column > -1 && row > -1 && column < 10 && row < 10) {
				returnedUUID = roomGrid[row][column];
			}
		}
		return returnedUUID;
	}

	@Override
	public void placeRoom(UUID roomUUID, Room room) {
		for (int j =0; j<10; j++) {
			for (int i =0 ; i< 10; i++) {
				if (roomGrid[i][j] == null) {
					roomGrid[i][j] = roomUUID;
					return;
				}
			}
		}
	}

}
