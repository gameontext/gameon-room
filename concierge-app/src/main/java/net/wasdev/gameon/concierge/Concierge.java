package net.wasdev.gameon.concierge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

@ApplicationPath("")
@Path("concierge")
public class Concierge extends Application {


	List<Connection> connections = new ArrayList<Connection>();
	Map<String, Room> roomDirectory = new HashMap<String, Room>();
	Map<String, Room> startingRooms = new HashMap<String, Room>();
	
	public Concierge() {
		roomDirectory.put("North Room", new Room("North Room"));
		roomDirectory.put("East Room", new Room("East Room"));
		roomDirectory.put("Starting Room", new Room("Starting Room"));

		Room startingRoom = roomDirectory.get("Starting Room");
		Room northRoom = roomDirectory.get("North Room");
		Room eastRoom = roomDirectory.get("East Room");
		
		connections.add(new Connection(startingRoom, northRoom, "North"));
		connections.add(new Connection(northRoom, eastRoom, "East"));
		connections.add(new Connection(eastRoom, northRoom, "West"));
	}
	
	
	@GET
	@Path("startingRoom")
	@Produces(MediaType.APPLICATION_JSON)
	public Room getStartingRoom() {
		if (!startingRooms.isEmpty()) {
			return startingRooms.values().iterator().next();
		}
		return null;
	}
	
	
	@GET
	@Path("findConnnectedRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Room exitRoom(Room currentRoom, String exitName) {
		Room returnedRoom = null;
		for (Connection connection : connections) {
			if (connection.getStartRoom().getRoomName().equals(currentRoom.getRoomName()) && exitName.equals(connection.getStartingEntrance())) {
				returnedRoom = connection.getEndRoom();
			}
		}
		return returnedRoom;
	}

	@GET
	@Path("registerRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	public void registerRoom(Room room) {
		roomDirectory.put(room.getRoomName(), room);
		String difficulty = room.getAttribute("difficulty");
		if (difficulty != null) {
			difficulty = "easy";
		}
			
		if (difficulty.equals("easy")) {
			startingRooms.put(room.getRoomName(), room);
		}

		// General policy is that we hook rooms in graduated stages of complexity.

		// Medium room, medium room, hard room, easy room
		// Hard room - hard room, medium room, easy room
		if (difficulty.equals("easy")) {
			// Easy room - easy room, medium room, hard room
			
		}
		
		
		
	}
	
	
}
