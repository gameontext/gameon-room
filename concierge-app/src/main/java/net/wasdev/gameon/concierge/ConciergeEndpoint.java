package net.wasdev.gameon.concierge;

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

import net.wasdev.gameon.room.common.Room;

@ApplicationPath("")
@Path("concierge")
public class ConciergeEndpoint extends Application {
	

	Concierge c = new Concierge(new Simple2DPlacement());

	@GET
	@Path("startingRoom")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStartingRoom() {
		return Response.ok(c.getStartingRoom()).build();
	}
	
	@GET
	@Path("findConnnectedRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exitRoom(UUID currentRoom, String exitName) {
		return Response.ok(c.exitRoom(currentRoom, exitName)).build();
	}

	@POST
	@Path("registerRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerRoom(Room room) {
		return Response.ok(c.registerRoom(room)).build();
	}
	
	
}
