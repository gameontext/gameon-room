package net.wasdev.gameon.concierge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.wasdev.gameon.room.common.RegistrationResponse;
import net.wasdev.gameon.room.common.Room;

@ApplicationPath("")
@Path("concierge")
public class Concierge extends Application {

	Map<UUID, Room> roomDirectory = new HashMap<UUID, Room>();
	Room startingRoom = null;
	
	PlacementStrategy ps = new Simple2DPlacement();
	
	public Concierge(PlacementStrategy placementStrategy) {
			ps = placementStrategy;
	}

	public Concierge() {
	}

	@GET
	@Path("startingRoom")
	@Produces(MediaType.APPLICATION_JSON)
	public Room getStartingRoom() {
		return startingRoom;
	}
	
	@GET
	@Path("findConnnectedRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Room exitRoom(UUID currentRoom, String exitName) {
		Room nextRoom = null;
		UUID nextRoomUUID = ps.getConnectingRoom(currentRoom, exitName);
		if (nextRoomUUID != null) {
			nextRoom = roomDirectory.get(nextRoomUUID);
		}
		return nextRoom;
	}

	@POST
	@Path("registerRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	public UUID registerRoom(Room room) {
		System.out.println("Processing registration for : \n" + room.toString());
		if (startingRoom == null) {
			startingRoom = room;
		}
		
		UUID roomUUID = UUID.randomUUID();
		room.setAssignedID(roomUUID);
		roomDirectory.put(roomUUID, room);
		ps.placeRoom(roomUUID, room);
		return roomUUID;
	}
	
	
}
