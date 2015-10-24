package net.wasdev.gameon.concierge;

<<<<<<< Upstream, based on origin/manualWiring
<<<<<<< Upstream, based on origin/manualWiring
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
=======
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
=======
import javax.inject.Inject;
>>>>>>> a9f29e3 something going on... trying to get concierge to remember between requests
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.wasdev.gameon.room.common.EndpointCollection;
import net.wasdev.gameon.room.common.Room;

<<<<<<< Upstream, based on origin/manualWiring
@ApplicationPath("")
@Path("concierge")
@ApplicationScoped
=======
@Path("/")
<<<<<<< Upstream, based on origin/manualWiring
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
public class ConciergeEndpoint extends Application {
<<<<<<< Upstream, based on origin/manualWiring
	

	@Inject
	Concierge c;
=======
=======
public class ConciergeEndpoint {
>>>>>>> a9f29e3 something going on... trying to get concierge to remember between requests

<<<<<<< Upstream, based on origin/manualWiring
	Concierge c = new Concierge(new Simple2DPlacement());
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
=======
	@Inject
	Concierge c;
>>>>>>> a9f29e3 something going on... trying to get concierge to remember between requests

	@GET
	@Path("startingRoom")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStartingRoom() {
		Room startingRoom = c.getStartingRoom();

		if ( startingRoom == null )
			return Response.status(404).build();

		return Response.ok(startingRoom).build();
	}
	
	@GET
	@Path("rooms/{roomId}/{exitName}")
	@Produces(MediaType.APPLICATION_JSON)
<<<<<<< Upstream, based on origin/manualWiring
	public Response exitRoom(@PathParam("roomId") String roomId, @PathParam("exitName") String exitName) {
		return Response.ok(c.exitRoom(roomId, exitName)).build();
=======
	public Response exitRoom(@PathParam("roomId") String roomId, @PathParam("roomID") String exitName) {
		EndpointCollection ec = c.exitRoom(roomId, exitName);

		if ( ec.getEndpoints().isEmpty() )
			return Response.status(404).build();

		return Response.ok(ec).build();
>>>>>>> 0cc092c Adjustments to Concierge endpoint (rest paths, 404)
	}
	
	@GET
	@Path("rooms/{roomId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getARoom(@PathParam("roomId") String roomId) {
		return Response.ok(c.getRoom(roomId)).build();
	}
	
	

	@POST
	@Path("registerRoom")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerRoom(Room room) {
		return Response.ok(c.registerRoom(room)).build();
	}
	
	
}
