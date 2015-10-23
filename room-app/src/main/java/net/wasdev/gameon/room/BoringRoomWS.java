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

import net.wasdev.gameon.room.common.Exit;

/**
 * WebSocket endpoint for player's interacting with the room
 */
@ServerEndpoint(value = "/ws")
public class BoringRoomWS extends BoringRoom implements RoomProvider {

    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        // (lifecycle) Called when the connection is opened
        Log.endPoint(this, "This room is starting up");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // (lifecycle) Called when the connection is closed, treat this as the player has left the room
        Log.endPoint(this, "A player has left the room");
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
		if(content.equals("look")) {
			response.add("type", "chat");
			response.add(Constants.USERNAME, msg.get(Constants.USERNAME));
			response.add("content", description);
			session.getBasicRemote().sendText("player," + Message.getValue(msg.get(Constants.USERID)) + "," + response.build().toString());
			return;
		}
		String exitCmd = "exit ";
		if(content.startsWith(exitCmd)) {
			String exitName = content.substring(exitCmd.length(), content.length());
			for(Exit exit : room.getExits()) {		//look for a matching exit to the one specified
				if(exit.getName().equalsIgnoreCase(exitName)) {
					response.add(Constants.TYPE, "exit");
					response.add(Constants.CONTENT, "You exit out the door to freedom and a more exciting life ... ");
					response.add(Constants.EXITID, exit.getRoom());
					session.getBasicRemote().sendText("playerLocation," + Message.getValue(msg.get(Constants.USERID)) + "," + response.build().toString());
					return;
				}
			}
			
		}
		response.add("type", "chat");
		response.add(Constants.USERNAME, Message.getValue(msg.get(Constants.USERNAME)));
		response.add("content", "Unrecognised command - sorry :-(");
		session.getBasicRemote().sendText("player," + Message.getValue(msg.get(Constants.USERID)) + "," + response.build().toString());
    }

    //add a new player to the room
    private void addNewPlayer(Session session, String json) throws IOException {
    	if(session.getUserProperties().get(Constants.USERNAME) != null) {
    		return;		//already seen this user before on this socket
    	}
    	JsonObject msg = Json.createReader(new StringReader(json)).readObject();
		String username = Message.getValue(msg.get(Constants.USERNAME));
		String userid = Message.getValue(msg.get(Constants.USERID));
		
		//broadcast that the user has entered the room
		JsonObjectBuilder content = Json.createObjectBuilder();
		content.add("*", "Player " + username +" has entered the room");
		content.add(userid, "You have entered the room");
		generateEvent(session, content.build(), userid);
		
		//now send the room info
		session.getUserProperties().put(Constants.USERNAME, username);
		session.getUserProperties().put(Constants.USERID, userid);
		session.getBasicRemote().sendText("player," + userid + "," + toJSON().build().toString());

    }
    
    private void removePlayer(Session session, String json) throws IOException {
    	JsonObject msg = Json.createReader(new StringReader(json)).readObject();
    	String username = Message.getValue(msg.get(Constants.USERNAME));
    	String userid = Message.getValue(msg.get(Constants.USERID));
    	
		//broadcast that the user has entered the room
		JsonObjectBuilder content = Json.createObjectBuilder();
		content.add("*", "Player " + username +" has left the room");
		generateEvent(session, content.build(), userid);
    }
    
    @OnError
    public void onError(Throwable t) {
        // (lifecycle) Called if/when an error occurs and the connection is
        // disrupted
        Log.endPoint(this, "oops: " + t);
    }

    private void generateEvent(Session session, JsonObject content, String userID) throws IOException {
    	JsonObjectBuilder response = Json.createObjectBuilder();
    	response.add("type", "event");
    	response.add("content", content);
    	session.getBasicRemote().sendText("player," + userID + "," + response.build().toString());
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
