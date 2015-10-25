package net.wasdev.gameon.concierge;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.wasdev.gameon.room.common.Room;
import net.wasdev.gameon.room.common.RoomToEndpoints;
import net.wasdev.gameon.room.common.RoomToEndpointsWrapper;

@Path("/")
public class ConciergeEndpoint {

	@Inject
	Concierge c;

	@GET
	@Path("startingRoom")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStartingRoom() {
		RoomToEndpoints startingRoom = c.getStartingRoom();

		if ( startingRoom == null )
			return Response.status(404).build();

		RoomToEndpointsWrapper ew = new RoomToEndpointsWrapper();
		ew.setRel(startingRoom);
		return Response.ok(ew).build();
	}

	@GET
	@Path("rooms/{roomId}/{exitName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response exitRoom(@PathParam("roomId") String roomId, @PathParam("exitName") String exitName) {
		RoomToEndpoints ec = c.exitRoom(roomId, exitName);

		if ( ec.getEndpoints().isEmpty() )
			return Response.status(404).build();

		RoomToEndpointsWrapper ew = new RoomToEndpointsWrapper();
		ew.setRel(ec);
		return Response.ok(ew).build();
	}

	@GET
	@Path("rooms/{roomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getARoom(@PathParam("roomId") String roomId) {
		RoomToEndpointsWrapper ew = new RoomToEndpointsWrapper();
		ew.setRel(c.getRoom(roomId));
		return Response.ok(ew).build();
	}



	@POST
	@Path("registerRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerRoom(Room room) {
		return Response.ok(c.registerRoom(room)).build();
	}


}
