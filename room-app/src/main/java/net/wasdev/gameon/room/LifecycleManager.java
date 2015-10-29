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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.websocket.Endpoint;
import javax.websocket.Session;
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
<<<<<<< HEAD
 * Manages the registration of all rooms in the Engine with the concierge */
public class LifecycleManager implements ServerApplicationConfig {
	private static final String ENV_CONCIERGE_SVC = "service_concierge";
	private static final String ENV_ROOM_SVC = "service_room";

	private String conciergeLocation = null;
	
	Engine e = Engine.getEngine();
	
	public static class SessionRoomResponseProcessor implements net.wasdev.gameon.room.engine.Room.RoomResponseProcessor{
		AtomicInteger counter = new AtomicInteger(0);
		
	    private void generateEvent(Session session, JsonObject content, String userID, boolean selfOnly, int bookmark) throws IOException {
	    	JsonObjectBuilder response = Json.createObjectBuilder();
	    	response.add("type", "event");
	    	response.add("content", content);
	    	response.add("bookmark", bookmark);
	    	
	    	session.getBasicRemote().sendText("player," + (selfOnly?userID:"*") + "," + response.build().toString());
	    }
		public void playerEvent(String senderId, String selfMessage, String othersMessage){
			//System.out.println("Player message :: from("+senderId+") onlyForSelf("+String.valueOf(selfMessage)+") others("+String.valueOf(othersMessage)+")");
			JsonObjectBuilder content = Json.createObjectBuilder();
			boolean selfOnly=true;
			if(othersMessage!=null && othersMessage.length()>0){
				content.add("*", othersMessage);
				selfOnly=false;
			}
			if(selfMessage!=null && selfMessage.length()>0){
				content.add(senderId, selfMessage);
			}
			JsonObject json = content.build();
			int count = counter.incrementAndGet();
			for(Session s : activeSessions){
				try{
					generateEvent(s, json, senderId, selfOnly, count);
				}catch(IOException io){
					throw new RuntimeException(io);
				}
			}		
		}
	    private void generateRoomEvent(Session session, JsonObject content, int bookmark) throws IOException {
	    	JsonObjectBuilder response = Json.createObjectBuilder();
	    	response.add("type", "event");
	    	response.add("content", content);
	    	response.add("bookmark", bookmark);
	    	session.getBasicRemote().sendText("player,*," + response.build().toString());
	    }
		public void roomEvent(String s){
			//System.out.println("Message sent to everyone :: "+s);
			JsonObjectBuilder content = Json.createObjectBuilder();
			content.add("*", s);
			JsonObject json = content.build();
			int count = counter.incrementAndGet();
			for(Session session : activeSessions){
				try{
					generateRoomEvent(session, json, count);
				}catch(IOException io){
					throw new RuntimeException(io);
				}
			}	
		}
		public void chatEvent(String username, String msg){
			JsonObjectBuilder content = Json.createObjectBuilder();
			content.add("type", "chat");
			content.add("username", username);
			content.add("content", msg);
			content.add("bookmark", counter.incrementAndGet());
			JsonObject json = content.build();
			for(Session session : activeSessions){
				try{
					session.getBasicRemote().sendText("player,*,"+json.toString());
				}catch(IOException io){
					throw new RuntimeException(io);
				}
			}
		}
		public void locationEvent(String senderId, String roomName, String roomDescription, Map<String,String> exits, List<String>objects, List<String>inventory){
			JsonObjectBuilder content = Json.createObjectBuilder();
			content.add("type", "location");
			content.add("name", roomName);
			content.add("description", roomDescription);
			
			JsonObjectBuilder exitJson = Json.createObjectBuilder();
			for( Entry<String, String> e : exits.entrySet()){
				exitJson.add(e.getKey(),e.getValue());
			}	
			content.add("exits", exitJson.build());
			JsonArrayBuilder inv = Json.createArrayBuilder();
			for(String i : inventory){
				inv.add(i);
			}
			content.add("pockets", inv.build());
			JsonArrayBuilder objs = Json.createArrayBuilder();
			for(String o : objects){
				objs.add(o);
			}
			content.add("objects", objs.build());
			content.add("bookmark", counter.incrementAndGet());
			
			JsonObject json = content.build();
			for(Session session : activeSessions){
				try{
					session.getBasicRemote().sendText("player,"+senderId+","+json.toString());
				}catch(IOException io){
					throw new RuntimeException(io);
				}
			}
		}
		public void exitEvent(String senderId, String message, String exitID){
			JsonObjectBuilder content = Json.createObjectBuilder();
			content.add("type", "exit");
			content.add("exitId", exitID);
			content.add("content", message);
			content.add("bookmark", counter.incrementAndGet());
			JsonObject json = content.build();
			for(Session session : activeSessions){
				try{
					session.getBasicRemote().sendText("playerLocation,"+senderId+","+json.toString());
				}catch(IOException io){
					throw new RuntimeException(io);
				}
			}
		}
		
		Collection<Session> activeSessions = new HashSet<Session>();
		public void addSession(Session s){
			activeSessions.add(s);
		}
		public void removeSession(Session s){
			activeSessions.remove(s);
		}
	}
	
	private void getConfig() throws ServletException {
		conciergeLocation = System.getProperty(ENV_CONCIERGE_SVC, System.getenv(ENV_CONCIERGE_SVC));
		if(conciergeLocation == null) {
			throw new ServletException("The location for the concierge service cold not be "
					+ "found in a system property or environment variable named : " + ENV_CONCIERGE_SVC);
		}
	}

	private static class RoomWSConfig extends ServerEndpointConfig.Configurator {
		private final Room room;
		private final SessionRoomResponseProcessor srrp;
		public RoomWSConfig(Room room, SessionRoomResponseProcessor srrp ){
			this.room=room;
			this.srrp=srrp;
			this.room.setRoomResponseProcessor(srrp);
		}
		@SuppressWarnings("unchecked")
		@Override
		public <T> T getEndpointInstance(Class<T> endpointClass){
			RoomWS r = new RoomWS(this.room,this.srrp);
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
					e.setDescription(ed.handler.getDescription(null, ed, room));
					e.setRoom(ed.targetRoomId);
					exits.add(e);
				}
			}
			commonRoom.setExits(exits);
			
			SessionRoomResponseProcessor srrp = new SessionRoomResponseProcessor();
			ServerEndpointConfig.Configurator config = new RoomWSConfig(room,srrp);
			
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
