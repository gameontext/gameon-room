package net.wasdev.gameon.concierge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

<<<<<<< Upstream, based on origin/manualWiring
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Application;

=======
import net.wasdev.gameon.room.common.Endpoint;
import net.wasdev.gameon.room.common.EndpointCollection;
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
import net.wasdev.gameon.room.common.RegistrationResponse;
import net.wasdev.gameon.room.common.Room;
import net.wasdev.gameon.room.common.RoomToEndpoints;

<<<<<<< Upstream, based on origin/manualWiring
@ApplicationScoped
public class Concierge extends Application {
=======
public class Concierge {
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)

<<<<<<< Upstream, based on origin/manualWiring
	Map<String, RoomToEndpoints> roomDirectory = new HashMap<String, RoomToEndpoints>();
	Set<String> startingRooms = new HashSet<String>();
	
=======
	Map<UUID, Room> roomDirectory = new HashMap<UUID, Room>();
	Map<String, List<Room>> roomNameToCollection = new HashMap<String, List<Room>>();
	Room startingRoom = null;

>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
	PlacementStrategy ps = new Simple2DPlacement();

	public Concierge(PlacementStrategy placementStrategy) {
		ps = placementStrategy;
	}

	public Concierge() {
	}

<<<<<<< Upstream, based on origin/manualWiring
	public RoomToEndpoints getStartingRoom() {
		for (String roomId : startingRooms) {
			RoomToEndpoints roomCollection = roomDirectory.get(roomId);
			if (roomCollection != null) {
				return roomCollection;
=======
	public Room getStartingRoom() {
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
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
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
<<<<<<< Upstream, based on origin/manualWiring
		
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
=======
		if (startingRoom == null) {
			startingRoom = room;
		}
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
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
		return rr;
	}

<<<<<<< Upstream, based on origin/manualWiring
	public RoomToEndpoints getRoom(String roomId) {
		RoomToEndpoints rte = new RoomToEndpoints();
		return null;
	}
	
	
=======

>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
}
