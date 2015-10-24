package net.wasdev.gameon.concierge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import net.wasdev.gameon.room.common.RegistrationResponse;
import net.wasdev.gameon.room.common.Room;
import net.wasdev.gameon.room.common.RoomToEndpoints;

@ApplicationScoped
public class Concierge {
	Map<String, RoomToEndpoints> roomDirectory = new HashMap<String, RoomToEndpoints>();
	Set<String> startingRooms = new HashSet<String>();
	
	PlacementStrategy ps = new Simple2DPlacement();

	public Concierge(PlacementStrategy placementStrategy) {
		ps = placementStrategy;
	}

	public Concierge() {
		System.out.println("CONCIERGE IS STARTING");
		ps = new Simple2DPlacement();
	}

	public RoomToEndpoints getStartingRoom() {
		for (String roomId : startingRooms) {
			RoomToEndpoints roomCollection = roomDirectory.get(roomId);
			if (roomCollection != null) {
				System.out.println("Request for starting room : \n" + roomCollection + "\n" + roomDirectory);
				return roomCollection;
				
			}
		}
		return null;
	}

	public RoomToEndpoints exitRoom(String currentRoomId, String exitName) {
		String roomId = ps.getConnectingRooms(currentRoomId, exitName);
		return roomDirectory.get(roomId);
	}

	public RegistrationResponse registerRoom(Room room) {
		System.out.println("Processing registration for : \n" + room.toString());
		RoomToEndpoints rte = roomDirectory.get(room.getRoomName());
		if (rte == null) {
			rte = new RoomToEndpoints();
			rte.setRoomId(room.getRoomName());
		}
		
		List<String> endpoints = rte.getEndpoints();
		endpoints.add(room.getAttribute("endPoint"));
		roomDirectory.put(room.getRoomName(), rte);
		boolean startLocation = true;
		String setStartLocation = room.getAttribute("startLocation");
		if (setStartLocation != null) {
			startLocation = Boolean.valueOf(setStartLocation);
		}
		
		ps.placeRoom(room);
		if (startLocation) {
			startingRooms.add(room.getRoomName());
		}
		RegistrationResponse rr = new RegistrationResponse();
		return rr;
	}

	public RoomToEndpoints getRoom(String roomId) {
		RoomToEndpoints rte = new RoomToEndpoints();
		return null;
	}
}
