package net.wasdev.gameon.concierge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import net.wasdev.gameon.room.common.Endpoint;
import net.wasdev.gameon.room.common.EndpointCollection;
import net.wasdev.gameon.room.common.RegistrationResponse;
import net.wasdev.gameon.room.common.Room;

@ApplicationScoped
public class Concierge {

	Map<UUID, Room> roomDirectory = new HashMap<UUID, Room>();
	Map<String, List<Room>> roomNameToCollection = new HashMap<String, List<Room>>();
	volatile Room startingRoom = null;

	PlacementStrategy ps = new Simple2DPlacement();

	public Concierge(PlacementStrategy placementStrategy) {
		ps = placementStrategy;
	}

	public Concierge() {
		System.out.println("CONCIERGE IS STARTING");
		ps = new Simple2DPlacement();
	}

	public Room getStartingRoom() {
		System.out.println("Request for starting room : \n" + startingRoom + "\n" + roomDirectory);

		return startingRoom;
	}

	public EndpointCollection exitRoom(String currentRoomId, String exitName) {
		List<Room> nextRooms = ps.getConnectingRooms(currentRoomId, exitName);
		List<Endpoint> endpoints = new ArrayList<Endpoint>();
		if (nextRooms != null) {
			for (Room room : nextRooms) {
				Endpoint endpoint = new Endpoint();
				endpoint.setUrl(room.getAttribute("endPoint"));
				endpoints.add(endpoint);
			}
		}
		EndpointCollection crr = new EndpointCollection();
		crr.setEndpoints(endpoints);
		return crr;
	}

	public RegistrationResponse registerRoom(Room room) {
		System.out.println("Processing registration for : \n" + room.toString());
		startingRoom = room;

		List<Room> roomMap = roomNameToCollection.get(room.getRoomName());
		if (roomMap == null) {
			roomMap = new ArrayList<Room>();
		}

		roomMap.add(room);
		roomNameToCollection.put(room.getRoomName(), roomMap);
		UUID roomUUID = UUID.randomUUID();
		room.setAssignedID(roomUUID);
		roomDirectory.put(roomUUID, room);
		ps.placeRoom(room);

		RegistrationResponse rr = new RegistrationResponse(roomUUID);
		return rr;
	}


}
