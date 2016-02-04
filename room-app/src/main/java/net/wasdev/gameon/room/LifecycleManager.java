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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
 * Manages the registration of all rooms in the Engine with the concierge
 */
@ApplicationScoped
public class LifecycleManager implements ServerApplicationConfig {
    private static final String ENV_MAP_SVC = "service_map";
    private static final String ENV_ROOM_SVC = "service_room";
    private String conciergeLocation = null;
    private String registrationSecret;

    Engine e = Engine.getEngine();

    public static class SessionRoomResponseProcessor
            implements net.wasdev.gameon.room.engine.Room.RoomResponseProcessor {
        AtomicInteger counter = new AtomicInteger(0);

        private void generateEvent(Session session, JsonObject content, String userID, boolean selfOnly, int bookmark)
                throws IOException {
            JsonObjectBuilder response = Json.createObjectBuilder();
            response.add("type", "event");
            response.add("content", content);
            response.add("bookmark", bookmark);

            String msg = "player," + (selfOnly ? userID : "*") + "," + response.build().toString();
            System.out.println("ROOM(PE): sending to session " + session.getId() + " message:" + msg);
            session.getBasicRemote().sendText(msg);
        }

        @Override
        public void playerEvent(String senderId, String selfMessage, String othersMessage) {
            // System.out.println("Player message :: from("+senderId+")
            // onlyForSelf("+String.valueOf(selfMessage)+")
            // others("+String.valueOf(othersMessage)+")");
            JsonObjectBuilder content = Json.createObjectBuilder();
            boolean selfOnly = true;
            if (othersMessage != null && othersMessage.length() > 0) {
                content.add("*", othersMessage);
                selfOnly = false;
            }
            if (selfMessage != null && selfMessage.length() > 0) {
                content.add(senderId, selfMessage);
            }
            JsonObject json = content.build();
            int count = counter.incrementAndGet();
            for (Session s : activeSessions) {
                try {
                    generateEvent(s, json, senderId, selfOnly, count);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
        }

        private void generateRoomEvent(Session session, JsonObject content, int bookmark) throws IOException {
            JsonObjectBuilder response = Json.createObjectBuilder();
            response.add("type", "event");
            response.add("content", content);
            response.add("bookmark", bookmark);

            String msg = "player,*," + response.build().toString();
            System.out.println("ROOM(RE): sending to session " + session.getId() + " message:" + msg);

            session.getBasicRemote().sendText(msg);
        }

        @Override
        public void roomEvent(String s) {
            // System.out.println("Message sent to everyone :: "+s);
            JsonObjectBuilder content = Json.createObjectBuilder();
            content.add("*", s);
            JsonObject json = content.build();
            int count = counter.incrementAndGet();
            for (Session session : activeSessions) {
                try {
                    generateRoomEvent(session, json, count);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
        }

        public void chatEvent(String username, String msg) {
            JsonObjectBuilder content = Json.createObjectBuilder();
            content.add("type", "chat");
            content.add("username", username);
            content.add("content", msg);
            content.add("bookmark", counter.incrementAndGet());
            JsonObject json = content.build();
            for (Session session : activeSessions) {
                try {
                    String cmsg = "player,*," + json.toString();
                    System.out.println("ROOM(CE): sending to session " + session.getId() + " message:" + cmsg);

                    session.getBasicRemote().sendText(cmsg);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
        }

        @Override
        public void locationEvent(String senderId, String roomName, String roomDescription, Map<String, String> exits,
                List<String> objects, List<String> inventory) {
            JsonObjectBuilder content = Json.createObjectBuilder();
            content.add("type", "location");
            content.add("name", roomName);
            content.add("description", roomDescription);

            JsonObjectBuilder exitJson = Json.createObjectBuilder();
            for (Entry<String, String> e : exits.entrySet()) {
                exitJson.add(e.getKey(), e.getValue());
            }
            content.add("exits", exitJson.build());
            JsonArrayBuilder inv = Json.createArrayBuilder();
            for (String i : inventory) {
                inv.add(i);
            }
            content.add("pockets", inv.build());
            JsonArrayBuilder objs = Json.createArrayBuilder();
            for (String o : objects) {
                objs.add(o);
            }
            content.add("objects", objs.build());
            content.add("bookmark", counter.incrementAndGet());

            JsonObject json = content.build();
            for (Session session : activeSessions) {
                try {
                    String lmsg = "player," + senderId + "," + json.toString();
                    System.out.println("ROOM(LE): sending to session " + session.getId() + " message:" + lmsg);
                    session.getBasicRemote().sendText(lmsg);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
        }

        @Override
        public void listExitsEvent(String senderId, Map<String, String> exits) {

            JsonObjectBuilder exitMap = Json.createObjectBuilder();
            for (Entry<String, String> entry : exits.entrySet()) {
                exitMap.add(entry.getKey(), entry.getValue());
            }

            JsonObjectBuilder content = Json.createObjectBuilder();
            content.add(Constants.TYPE, Constants.EXITS);
            content.add(Constants.CONTENT, exitMap.build());

            String lmsg = "player," + senderId + "," + content.build().toString();
            for (Session session : activeSessions) {
                if (session.isOpen()) {
                    try {
                        System.out.println("ROOM(LEE): sending to session " + session.getId() + " message:" + lmsg);
                        session.getBasicRemote().sendText(lmsg);
                    } catch (IOException io) {
                        throw new RuntimeException(io);
                    }
                }
            }
        }

        @Override
        public void exitEvent(String senderId, String message, String exitID) {
            JsonObjectBuilder content = Json.createObjectBuilder();
            content.add("type", "exit");
            content.add("exitId", exitID);
            content.add("content", message);
            content.add("bookmark", counter.incrementAndGet());
            JsonObject json = content.build();
            for (Session session : activeSessions) {
                try {
                    String emsg = "playerLocation," + senderId + "," + json.toString();
                    System.out.println("ROOM(EE): sending to session " + session.getId() + " message:" + emsg);
                    session.getBasicRemote().sendText(emsg);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
        }

        Collection<Session> activeSessions = new HashSet<Session>();

        public void addSession(Session s) {
            activeSessions.add(s);
        }

        public void removeSession(Session s) {
            activeSessions.remove(s);
        }
    }

    private void getConfig() throws ServletException {
        conciergeLocation = System.getProperty(ENV_MAP_SVC, System.getenv(ENV_MAP_SVC));
        if (conciergeLocation == null) {
            throw new ServletException("The location for the map service cold not be "
                    + "found in a system property or environment variable named : " + ENV_MAP_SVC);
        }
        try {
            registrationSecret = (String) new InitialContext().lookup("registrationSecret");
        } catch (NamingException e) {
        }
        if (registrationSecret == null) {
            throw new ServletException("registrationSecret was not found, check server.xml/server.env");
        }
    }

    private static class RoomWSConfig extends ServerEndpointConfig.Configurator {
        private final Room room;
        private final SessionRoomResponseProcessor srrp;

        public RoomWSConfig(Room room, SessionRoomResponseProcessor srrp) {
            this.room = room;
            this.srrp = srrp;
            this.room.setRoomResponseProcessor(srrp);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) {
            RoomWS r = new RoomWS(this.room, this.srrp);
            return (T) r;
        }
    }

    private Set<ServerEndpointConfig> registerRooms(Collection<Room> rooms) {
        Client client = ClientBuilder.newClient();

        // add the apikey handler for the registration request.
        ApiKey apikey = new ApiKey("roomRegistration", registrationSecret);
        client.register(apikey);

        WebTarget target = client.target(conciergeLocation);
        Set<ServerEndpointConfig> endpoints = new HashSet<ServerEndpointConfig>();
        for (Room room : rooms) {
            
            //TODO: move registration test to a sensible method.. 
        
            //test if room is already registered.     
            try{
            String name = room.getRoomName();
            String userId = "game-on.org";
                       
            // build the apikey for the query request.
            String queryParams = "name=" + name + "&owner=" + userId + "&id=" + userId + "&stamp="
                    + System.currentTimeMillis();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(registrationSecret.getBytes("UTF-8"), "HmacSHA256"));
            String hash = javax.xml.bind.DatatypeConverter
                    .printBase64Binary(mac.doFinal(queryParams.getBytes("UTF-8")));

            // build the complete query url..
            System.out.println("Querying room registration using url " + conciergeLocation);
            URL u = new URL(conciergeLocation + "?" + queryParams + "&apikey=" + hash);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json;");
            con.setRequestProperty("Accept", "application/json,text/plain");
            con.setRequestProperty("Method", "GET");

            //initiate the request.
            int httpResult = con.getResponseCode();
            if (httpResult == 204) {
                System.out.println("Skipping registration for room "+room.getRoomName()+" because it is already known to the map service");
                continue;
            }
            }catch(Exception e){
                System.out.println("Error testing registration for room, will not try to register room");
                e.printStackTrace();
                continue;
            }
            
            System.out.println("Registering room " + room.getRoomName());
            Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);


            String endPoint = System.getProperty(ENV_ROOM_SVC, System.getenv(ENV_ROOM_SVC));
            if (endPoint == null) {
                throw new RuntimeException("The location for the room service cold not be "
                        + "found in a system property or environment variable named : " + ENV_ROOM_SVC);
            }
            
            // build the registration payload (post data)
            JsonObjectBuilder registrationPayload = Json.createObjectBuilder();
            // add the basic room info.
            registrationPayload.add("name", room.getRoomId());
            registrationPayload.add("fullName", room.getRoomName());
            registrationPayload.add("description", room.getRoomDescription());
            // add the doorway descriptions we'd like the game to use if it
            // wires us to other rooms.
            JsonObjectBuilder doors = Json.createObjectBuilder();
            doors.add("n", "A Large doorway to the north");
            doors.add("s", "A winding path leading off to the south");
            doors.add("e", "An overgrown road, covered in brambles");
            doors.add("w", "A shiny metal door, with a bright red handle");
            doors.add("u", "A spiral set of stairs, leading upward into the ceiling");
            doors.add("d", "A tunnel, leading down into the earth");            
            registrationPayload.add("doors", doors.build());
            
            // add the connection info for the room to connect back to us..
            JsonObjectBuilder connInfo = Json.createObjectBuilder();
            connInfo.add("type", "websocket"); // the only current supported
                                               // type.
            connInfo.add("target", endPoint + "/ws/" +room.getRoomId());
            registrationPayload.add("connectionDetails", connInfo.build());

            SessionRoomResponseProcessor srrp = new SessionRoomResponseProcessor();
            ServerEndpointConfig.Configurator config = new RoomWSConfig(room, srrp);

            endpoints.add(ServerEndpointConfig.Builder.create(RoomWS.class, "/ws/" + room.getRoomId())
                    .configurator(config).build());

            Response response = builder.post(Entity.json(registrationPayload.build().toString()));
            try {
                if (Status.OK.getStatusCode() == response.getStatus()) {
                    String resp = response.readEntity(String.class);
                    System.out.println("Registration returned " + resp);
                } else {
                    String resp = response.readEntity(String.class);
                    System.out.println("Error registering room provider : " + room.getRoomName() + " : status code "
                            + response.getStatus()+"\n"+ resp);
                }
            } finally {
                response.close();
            }
        }

        return endpoints;
    }

    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
        try {
            getConfig();
            return registerRooms(e.getRooms());
        } catch (ServletException e) {
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
