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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import net.wasdev.gameon.room.engine.Room;

/**
 * WebSocket endpoint for player's interacting with the room
 */
public class RoomWS extends Endpoint {
    private final Room room;
    private final LifecycleManager.SessionRoomResponseProcessor srrp;

    public RoomWS(Room room, LifecycleManager.SessionRoomResponseProcessor srrp) {
        this.room = room;
        this.srrp = srrp;
    }

    private Map<Session, MessageHandler.Whole<String>> handlersBySession = new ConcurrentHashMap<Session, MessageHandler.Whole<String>>();

    private static class SessionMessageHandler implements MessageHandler.Whole<String> {
        private final Session session;
        private final RoomWS owner;

        public SessionMessageHandler(Session session, RoomWS owner) {
            this.session = session;
            this.owner = owner;
        }

        @Override
        public void onMessage(String message) {
            try {
                owner.receiveMessage(message, session);
            } catch (IOException io) {
                System.err.println("IO Exception sending message to session " + io);
            }
        }
    }

    @Override
    public void onOpen(final Session session, EndpointConfig ec) {

        Log.endPoint(this, "onOpen called against room " + this.room.getRoomId());
        if (srrp.activeSessions.size() == 0) {
            Log.endPoint(this, " No sessions known.");
        }
        for (Session s : srrp.activeSessions) {
            Log.endPoint(this, " Session: " + s.getId());
            Log.endPoint(this, "   handlers: " + s.getMessageHandlers().size());
            int mhc = 0;
            for (MessageHandler m : s.getMessageHandlers()) {
                if (m instanceof SessionMessageHandler) {
                    SessionMessageHandler smh = (SessionMessageHandler) m;
                    Log.endPoint(this, "    [" + mhc + "] SessionMessageHandler for session " + smh.session.getId()
                            + " linked to room " + smh.owner.room.getRoomId());
                } else {
                    Log.endPoint(this, "    [" + mhc + "] unknown handler");
                }
                mhc++;
            }
        }

        // (lifecycle) Called when the connection is opened
        srrp.addSession(session);

        MessageHandler.Whole<String> handlerForSession = new SessionMessageHandler(session, this);

        MessageHandler.Whole<String> fromMap = handlersBySession.get(session);
        MessageHandler.Whole<String> chosen = fromMap != null ? fromMap : handlerForSession;
        handlersBySession.put(session, chosen);

        session.addMessageHandler(String.class, chosen);

        Log.endPoint(this, "after opOpen room " + this.room.getRoomId());
        for (Session s : srrp.activeSessions) {
            Log.endPoint(this, " Session: " + s.getId());
            Log.endPoint(this, "   handlers: " + s.getMessageHandlers().size());
            int mhc = 0;
            for (MessageHandler m : s.getMessageHandlers()) {
                if (m instanceof SessionMessageHandler) {
                    SessionMessageHandler smh = (SessionMessageHandler) m;
                    Log.endPoint(this, "    [" + mhc + "] SessionMessageHandler for session " + smh.session.getId()
                            + " linked to room " + smh.owner.room.getRoomId());
                } else {
                    Log.endPoint(this, "    [" + mhc + "] unknown handler");
                }
                mhc++;
            }
        }

    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        // (lifecycle) Called when the connection is closed, treat this as the
        // player has left the room
        srrp.removeSession(session);
        MessageHandler handler = handlersBySession.remove(session);
        if (handler != null) {
            session.removeMessageHandler(handler);
        }

        Log.endPoint(this, "onClose called against room " + this.room.getRoomId());
        for (Session s : srrp.activeSessions) {
            Log.endPoint(this, " Session: " + s.getId());
            Log.endPoint(this, "   handlers: " + s.getMessageHandlers().size());
            int mhc = 0;
            for (MessageHandler m : s.getMessageHandlers()) {
                if (m instanceof SessionMessageHandler) {
                    SessionMessageHandler smh = (SessionMessageHandler) m;
                    Log.endPoint(this, "    [" + mhc + "] SessionMessageHandler for session " + smh.session.getId()
                            + " linked to room " + smh.owner.room.getRoomId());
                } else {
                    Log.endPoint(this, "    [" + mhc + "] unknown handler");
                }
                mhc++;
            }
        }
    }

    public void receiveMessage(String message, Session session) throws IOException {
        System.out.println("ROOMX: [" + this.hashCode() + ":" + this.room.getRoomId() + "] sess:[" + session.hashCode()
                + ":" + session.getId() + " mess:" + message);
        String[] contents = Message.splitRouting(message);
        if (contents[0].equals("roomHello")) {
            addNewPlayer(session, contents[2]);
            return;
        }
        if (contents[0].equals("room")) {
            processCommand(session, contents[2]);
            return;
        }
        if (contents[0].equals("roomGoodbye")) {
            removePlayer(session, contents[2]);
            return;
        }
        System.out.println("ERR: Unknown message type for room " + room.getRoomId() + " message:" + message);
    }

    // process a command
    private void processCommand(Session session, String json) throws IOException {
        Log.endPoint(this, "Command received from the user, " + this);
        JsonObject msg = Json.createReader(new StringReader(json)).readObject();

        String content = Message.getValue(msg.get("content"));
        String userid = Message.getValue(msg.get(Constants.USERID));
        String username = Message.getValue(msg.get(Constants.USERNAME));

        if (content.startsWith("/")) {
            room.command(userid, content.substring(1));
        } else {
            // everything else is chat.
            srrp.chatEvent(username, content);
        }
    }

    // add a new player to the room
    private void addNewPlayer(Session session, String json) throws IOException {

        JsonObject msg = Json.createReader(new StringReader(json)).readObject();
        String username = Message.getValue(msg.get(Constants.USERNAME));
        String userid = Message.getValue(msg.get(Constants.USERID));

        System.out.println(
                "*** Adding player " + userid + " from room " + room.getRoomId() + " via session " + session.getId());
        room.addUserToRoom(userid, username);
        room.command(userid, "look");
    }

    private void removePlayer(Session session, String json) throws IOException {
        JsonObject msg = Json.createReader(new StringReader(json)).readObject();
        String userid = Message.getValue(msg.get(Constants.USERID));
        System.out.println("*** Removing player " + userid + " from room " + room.getRoomId() + " from session "
                + session.getId());
        room.removeUserFromRoom(userid);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        // (lifecycle) Called if/when an error occurs and the connection is
        // disrupted
        Log.endPoint(this, "oops: " + thr);
    }

}
