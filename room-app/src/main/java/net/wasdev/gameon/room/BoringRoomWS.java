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

/**
 * WebSocket endpoint for player's interacting with the room
 */
@ServerEndpoint(value = "/ws")
public class BoringRoomWS extends BoringRoom implements RoomProvider {

    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        // (lifecycle) Called when the connection is opened
        Log.endPoint(this, "This room is starting up");

        // Store the endpoint id in the session so that when we log and push
        // messages around, we have something more user-friendly to look at.
        session.getUserProperties().put("endptId", "player id!");
        try {
			session.getBasicRemote().sendText(toJSON().build().toString());
		} catch (IOException e) {
			Log.endPoint(this, "Error processing connection : " + e.getMessage());
			tryToClose(session);
		}
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // (lifecycle) Called when the connection is closed, treat this as the player has left the room
        Log.endPoint(this, "A player has left the room");
    }

    @OnMessage
    public void receiveMessage(String message, Session session) throws IOException {
    	JsonObject msg = Json.createReader(new StringReader(message)).readObject();
    	if(session.getUserProperties().get(Constants.USERNAME) == null) {
    		//we have a new user so tell everyone that a new player is in town
    		if(msg.containsKey("username")) {
    			String username = msg.get("username").toString();
    			session.getUserProperties().put(Constants.USERNAME, username);
    			broadcast(session, "Player " + username + " has entered the room", "You have entered the room");
        	}
    	}
    	if(msg.containsKey("content")) {
    		Log.endPoint(this, "Command received from the user, " + this);
    		if(msg.get("content").toString().equalsIgnoreCase("\"look\"")) {
    			JsonObjectBuilder response = Json.createObjectBuilder();
    			response.add("type", "chat");
    			response.add("username", msg.get("username"));
    			response.add("content", description);
    			session.getBasicRemote().sendText(response.build().toString());
    			return;
    		}
    		
    	}
        // Called when a message is received.
        if ("stop".equals(message)) {
            Log.endPoint(this, "I was asked to stop, " + this);
            tryToClose(session);
        } else {
            Log.endPoint(this, "I got a message: " + message);
            // Send something back to the client for feedback
            session.getBasicRemote().sendText("server received:  " + message);
        }
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
