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
public class WebSocket {

    @OnOpen
    public void onOpen(Session session, EndpointConfig ec) {
        // (lifecycle) Called when the connection is opened
        Log.endPoint(this, "I'm open!");

        // Store the endpoint id in the session so that when we log and push
        // messages around, we have something more user-friendly to look at.
        session.getUserProperties().put("endptId", "player id!");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        // (lifecycle) Called when the connection is closed
        Log.endPoint(this, "I'm closed!");
    }

    @OnMessage
    public void receiveMessage(String message, Session session) throws IOException {
        // Called when a message is received.
        if ("stop".equals(message)) {
            Log.endPoint(this, "I was asked to stop, " + this);
            session.close();
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
    void broadcast(Session session, int id, String message) {

        // Look, Ma! Broadcast!
        // Easy as pie to send the same data around to different sessions.
        for (Session s : session.getOpenSessions()) {
            try {
                if (s.isOpen()) {
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
