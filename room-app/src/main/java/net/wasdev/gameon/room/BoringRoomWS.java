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

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for player's interacting with the room
 */
@ServerEndpoint(value = "/ws")
public class BoringRoomWS {
	
	@Inject
	BoringRoom room;
	
	public static class SessionRoomResponseProcessor implements Engine.Room.RoomResponseProcessor{
		AtomicInteger counter = new AtomicInteger(0);
		
	    private void generateEvent(Session session, JsonObject content, String userID) throws IOException {
	    	JsonObjectBuilder response = Json.createObjectBuilder();
	    	response.add("type", "event");
	    	response.add("content", content);
	    	session.getBasicRemote().sendText("player," + userID + "," + response.build().toString());
	    }
		public void playerEvent(String senderId, String selfMessage, String othersMessage){
			//System.out.println("Player message :: from("+senderId+") onlyForSelf("+String.valueOf(selfMessage)+") others("+String.valueOf(othersMessage)+")");
			JsonObjectBuilder content = Json.createObjectBuilder();
			if(selfMessage!=null && selfMessage.length()>0){
				content.add(senderId, selfMessage);
			}
			if(othersMessage!=null && othersMessage.length()>0){
				content.add("*", othersMessage);
			}
			content.add("bookmark", counter.incrementAndGet());
			JsonObject json = content.build();
			for(Session s : activeSessions){
				try{
					generateEvent(s, json, senderId);
				}catch(IOException io){
					throw new RuntimeException(io);
				}
			}		
		}
	    private void generateRoomEvent(Session session, JsonObject content) throws IOException {
	    	JsonObjectBuilder response = Json.createObjectBuilder();
	    	response.add("type", "event");
	    	response.add("content", content);
	    	session.getBasicRemote().sendText("player, *," + response.build().toString());
	    }
		public void roomEvent(String s){
			//System.out.println("Message sent to everyone :: "+s);
			JsonObjectBuilder content = Json.createObjectBuilder();
			content.add("*", s);
			content.add("bookmark", counter.incrementAndGet());
			JsonObject json = content.build();
			for(Session session : activeSessions){
				try{
					generateRoomEvent(session, json);
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
					session.getBasicRemote().sendText("player, *,"+json.toString());
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
    public void onOpen(Session session, EndpointConfig ec) {
        // (lifecycle) Called when the connection is opened
        Log.endPoint(this, "This room is starting up");
        room.r.setRoomResponseProcessor(srrp);
        srrp.addSession(session);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // (lifecycle) Called when the connection is closed, treat this as the player has left the room
        Log.endPoint(this, "A player has left the room");
        room.r.setRoomResponseProcessor(srrp);
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
		
		//standard response header directed back to the client
		JsonObjectBuilder response = Json.createObjectBuilder();
		String content = Message.getValue(msg.get("content").toString().toLowerCase());
		String userid = Message.getValue(msg.get(Constants.USERID));
		String username = Message.getValue(msg.get(Constants.USERNAME));
		
		if(content.startsWith("/")){
			room.r.command(userid,content.substring(1));
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
		
		if(room.addPlayer(userid, username)) {			
			room.r.command(userid, "look");
		}

    }
    
    private void removePlayer(Session session, String json) throws IOException {
    	JsonObject msg = Json.createReader(new StringReader(json)).readObject();
    	String userid = Message.getValue(msg.get(Constants.USERID));
    	room.removePlayer(userid);
    }
    
    @OnError
    public void onError(Throwable t) {
        // (lifecycle) Called if/when an error occurs and the connection is
        // disrupted
        Log.endPoint(this, "oops: " + t);
    }


    
    /**
     * Simple text based broadcast. This does some additional munging of the
     * message text, to make it more obvious where the message originated (is an
     * endpoint getting its own message back, or has it been forwarded from
     * another endpoint).
     *
     * @param session
     * @param id
     * @param message
     */
    void broadcast(Session session, String message, String clientMsg) {

        // Look, Ma! Broadcast!
        // Easy as pie to send the same data around to different sessions.
        for (Session s : session.getOpenSessions()) {
            try {
                if (s.isOpen()) {
                	if(session.getId().equals(s.getId())) {
                		//this is potentially a specific broadcast to the client
                		if(clientMsg != null) {
                			s.getBasicRemote().sendText(clientMsg);
                			continue;
                		}
                	}
                    Log.endPoint(this, "--> ep=" + s.getUserProperties().get("endptId") + ": " + message);
                    s.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                tryToClose(s);
            }
        }
    }

    /**
     * Try to close a session (usually once an error has already occurred).
     *
     * @param c
     */
    private final void tryToClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e1) {
            }
        }
    }
    
    
}
