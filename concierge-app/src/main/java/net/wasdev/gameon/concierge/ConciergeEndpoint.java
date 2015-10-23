package net.wasdev.gameon.concierge;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.wasdev.gameon.room.common.Room;

@ApplicationPath("")
@Path("concierge")
@ApplicationScoped
public class ConciergeEndpoint extends Application {
	

	@Inject
	Concierge c;

	@GET
	@Path("startingRoom")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStartingRoom() {
		return Response.ok(c.getStartingRoom()).build();
	}
	
	@GET
	@Path("rooms/{roomId}/{exitName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response exitRoom(@PathParam("roomId") String roomId, @PathParam("exitName") String exitName) {
		return Response.ok(c.exitRoom(roomId, exitName)).build();
	}
	
	@GET
	@Path("rooms/{roomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getARoom(@PathParam("roomId") String roomId) {
		return Response.ok(c.getRoom(roomId)).build();
	}
	
	

	@POST
	@Path("rooms")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerRoom(Room room) {
		return Response.ok(c.registerRoom(room)).build();
	}
	
	
}
