/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.gameon.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.wasdev.gameon.room.engine.Engine;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.meta.ExitDesc;

/**
 * Manages the registration of all rooms in the Engine with the concierge */
public class LifecycleManager implements ServerApplicationConfig {
	private static final String ENV_CONCIERGE_SVC = "service.concierge";
	private static final String ENV_ROOM_SVC = "service.room";
	private String conciergeLocation = null;
	
	Engine e = Engine.getEngine();
	
	private void getConfig() throws ServletException {
		conciergeLocation = System.getProperty(ENV_CONCIERGE_SVC, System.getenv(ENV_CONCIERGE_SVC));
		if(conciergeLocation == null) {
			throw new ServletException("The location for the concierge service cold not be "
					+ "found in a system property or environment variable named : " + ENV_CONCIERGE_SVC);
		}
	}

	private static class RoomWSConfig extends ServerEndpointConfig.Configurator {
		public final Room room;
		public RoomWSConfig(Room room){
			this.room=room;
		}
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getEndpointInstance(Class<T> endpointClass){
			RoomWS r = new RoomWS(this.room);
			return (T)r;
		}
	}
	
	private Set<ServerEndpointConfig> registerRooms(Collection<Room> rooms) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(conciergeLocation);
		Set<ServerEndpointConfig> endpoints = new HashSet<ServerEndpointConfig>();
		for(Room room : rooms) {
			System.out.println("Registering room " + room.getRoomName());
			Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
			
			net.wasdev.gameon.room.common.Room commonRoom=new net.wasdev.gameon.room.common.Room(room.getRoomId());			
			String endPoint = System.getProperty(ENV_ROOM_SVC, System.getenv(ENV_ROOM_SVC));
			if(endPoint == null) {
				throw new RuntimeException("The location for the room service cold not be "
						+ "found in a system property or environment variable named : " + ENV_ROOM_SVC);
			}
			commonRoom.setAttribute("endPoint", endPoint + "/ws/"+room.getRoomId());
			commonRoom.setAttribute("startLocation", ""+room.isStarterLocation());
			List<net.wasdev.gameon.room.common.Exit> exits = new ArrayList<net.wasdev.gameon.room.common.Exit>();
			for(ExitDesc ed : room.getExits()){
				if(ed.handler.isVisible()){
					net.wasdev.gameon.room.common.Exit e = new net.wasdev.gameon.room.common.Exit();
					e.setName(ed.direction.toString());
					e.setDescription(ed.handler.getDescription(null, room));
					e.setRoom(ed.targetRoomId);
					exits.add(e);
				}
			}
			commonRoom.setExits(exits);
			
			ServerEndpointConfig.Configurator config = new RoomWSConfig(room);
			
			endpoints.add(ServerEndpointConfig.Builder
            .create(RoomWS.class, "/ws/"+room.getRoomId())
            .configurator(config)
            .build());
		       			
			Response response = builder.post(Entity.json(commonRoom));
			try {
				if(Status.OK.getStatusCode()== response.getStatus()) {
					String resp = response.readEntity(String.class);
					System.out.println("Registration returned " + resp);									
				} else {
					System.out.println("Error registering room provider : " + room.getRoomName() + " : status code " + response.getStatus());
				}
			} finally {
				response.close();
			}

		}
		return endpoints;
	}

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
		try{
			getConfig();
			return registerRooms(e.getRooms());
		}catch(ServletException e){
			System.err.println("Error building endpoint configs for ro");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		  
    }

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		return null;
	}
	
}
