package net.wasdev.gameon.concierge;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.wasdev.gameon.room.common.Room;

public class Simple2DPlacement implements PlacementStrategy {
	
	String[][] roomGrid = new String[10][10];
	
	

	@Override
	public String getConnectingRooms(String currentRoomId, String exit) {
		int row = -1;
		int column = -1;
		String returnedId = null;
		for (int j =0; j<10; j++) {
			for (int i =0 ; i< 10; i++) {
				String currentId = roomGrid[i][j];
				if (currentId != null && currentId.equals(currentRoomId)) {
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
				returnedId = roomGrid[row][column];
			}
		}
		return returnedId;
	}

	@Override
	public void placeRoom(Room room) {
		int firstI = -1;
		int firstJ = -1;
		for (int j =0; j<10; j++) {
			for (int i =0 ; i< 10; i++) {
				if (roomGrid[i][j] == null && firstI < 0 && firstJ < 0) {
					firstI = i;
					firstJ = j;
					break;
				} else if (room.getRoomName().equals(roomGrid[i][j])) {
					return;
				}
			}
		}
		
		if (firstI > -1 && firstJ > -1) {
			roomGrid[firstI][firstJ] = room.getRoomName();
		}
		
		
	}

}
