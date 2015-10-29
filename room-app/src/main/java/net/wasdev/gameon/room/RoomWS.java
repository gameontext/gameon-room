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
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import net.wasdev.gameon.room.engine.Room;

/**
 * WebSocket endpoint for player's interacting with the room
 */
public class RoomWS extends Endpoint{
	private final Room room;
	
	public RoomWS(Room room){
		this.room=room;
        room.setRoomResponseProcessor(srrp);
	}
	
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
	
	private static SessionRoomResponseProcessor srrp = new SessionRoomResponseProcessor();	

    @OnOpen
    public void onOpen(final Session session, EndpointConfig ec) {
        // (lifecycle) Called when the connection is opened
        Log.endPoint(this, "This room is starting up");
        srrp.addSession(session);
        
        session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message){
				try{
					receiveMessage(message, session);
				}catch(IOException io){
					System.err.println("IO Exception sending message to session "+io);
				}
			}
		});
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // (lifecycle) Called when the connection is closed, treat this as the player has left the room
        Log.endPoint(this, "A player has left the room");
        srrp.removeSession(session);
    }

    @OnMessage
    public void receiveMessage(String message, Session session) throws IOException {
    	String[] contents = Message.splitRouting(message);
    	if(contents[0].equals("roomHello")) {
    		addNewPlayer(session, contents[2]);
    		return;
    	}
    	if(contents[0].equals("room")) {
    		processCommand(session, contents[2]);
    		return;
    	}
    	if(contents[0].equals("roomGoodbye")) {
    		removePlayer(session, contents[2]);
    		return;
    	}
    }
    
    //process a command 
    private void processCommand(Session session, String json) throws IOException {
		Log.endPoint(this, "Command received from the user, " + this);
		JsonObject msg = Json.createReader(new StringReader(json)).readObject();
		
		String content = Message.getValue(msg.get("content").toString().toLowerCase());
		String userid = Message.getValue(msg.get(Constants.USERID));
		String username = Message.getValue(msg.get(Constants.USERNAME));
		
		if(content.startsWith("/")){
			room.command(userid,content.substring(1));
		}else{
			//everything else is chat.
			srrp.chatEvent(username,content);
		}
    }
    
    //add a new player to the room
    private void addNewPlayer(Session session, String json) throws IOException {
    	if(session.getUserProperties().get(Constants.USERNAME) != null) {
    		return;		//already seen this user before on this socket
    	}
    	JsonObject msg = Json.createReader(new StringReader(json)).readObject();
		String username = Message.getValue(msg.get(Constants.USERNAME));
		String userid = Message.getValue(msg.get(Constants.USERID));
		
		room.addUserToRoom(userid, username);
		room.command(userid, "look");
    }
    
    private void removePlayer(Session session, String json) throws IOException {
    	JsonObject msg = Json.createReader(new StringReader(json)).readObject();
    	String userid = Message.getValue(msg.get(Constants.USERID));
    	room.removeUserFromRoom(userid);
    }
    
    @OnError
    public void onError(Throwable t) {
        // (lifecycle) Called if/when an error occurs and the connection is
        // disrupted
        Log.endPoint(this, "oops: " + t);
    }
    
}
