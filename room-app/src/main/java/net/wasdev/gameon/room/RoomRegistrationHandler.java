/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
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

import java.io.StringReader;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gameontext.signed.SignedClientRequestFilter;
import org.gameontext.signed.SignedWriterInterceptor;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.meta.DoorDesc;
import net.wasdev.gameon.room.engine.meta.ExitDesc;

public class RoomRegistrationHandler {

    private final String id;
    private final String secret;
    private final String endPoint;
    private final String mapLocation;
    private final Room room;
    private AtomicBoolean handling503 = new AtomicBoolean(false);
    private final String token;
    
    
    RoomRegistrationHandler(Room room, String id, String secret){
        this.id=id;
        this.room=room;
        this.secret=secret;
        
        endPoint = System.getProperty(Constants.ENV_ROOM_SVC, System.getenv(Constants.ENV_ROOM_SVC));
        if (endPoint == null) {
            throw new IllegalStateException("The location for the room service cold not be "
                    + "found in a system property or environment variable named : " + Constants.ENV_ROOM_SVC);
        }
        mapLocation = System.getProperty(Constants.ENV_MAP_SVC, System.getenv(Constants.ENV_MAP_SVC));
        if (mapLocation == null) {
            throw new IllegalStateException("The location for the map service cold not be "
                    + "found in a system property or environment variable named : " + Constants.ENV_MAP_SVC);
        }
        
        String value = "";
        try {
            value = (String) new InitialContext().lookup(room.TOKEN_ID);
        } catch (NamingException e) {
            //a token does not have to be defined for a given room
        }
        token = (value == null ? "" : value);
    }
    
    private static class RegistrationResult {
        enum Type { NOT_REGISTERED, REGISTERED, SERVICE_UNAVAILABLE };
        public Type type;
        public JsonObject registeredObject;
    }
    
    /**
     * Obtain current registration for this room
     * @param roomId
     * @return
     */
    private RegistrationResult checkExistingRegistration() throws Exception {
        RegistrationResult result = new RegistrationResult();
        try {
            Client queryClient = ClientBuilder.newClient();

            // add our request signer
            queryClient.register(new SignedClientRequestFilter(id, secret));

            // create the jax-rs 2.0 client
            WebTarget queryRoot = queryClient.target(mapLocation);
            
            // add the lookup arg for this room..
            WebTarget target = queryRoot.queryParam("owner", id).queryParam("name", room.getRoomId());
            Response r = null;

            r = target.request(MediaType.APPLICATION_JSON).get(); // .accept(MediaType.APPLICATION_JSON).get();
            int code = r.getStatusInfo().getStatusCode();
            switch (code) {
                case 204: {
                    // room is unknown to map
                    result.type = RegistrationResult.Type.NOT_REGISTERED;
                    return result;
                }
                case 200: {
                    // request succeeded.. we need to parse the result into a JsonObject..
                    // query url always returns an array, so we need to reach in to obtain our 
                    // hit. There should only ever be the one, becase we searched by owner and 
                    // name, and rooms should be unique by owner & name;
                    String respString = r.readEntity(String.class);
                    JsonReader reader = Json.createReader(new StringReader(respString));
                    JsonArray resp = reader.readArray();              
                    JsonObject queryResponse = resp.getJsonObject(0);
                    
                    //get the id for our already-registered room.
                    String roomId = queryResponse.getString("_id");
                    
                    // now we have our id.. make a new request to get our exit wirings.. 
                    queryClient = ClientBuilder.newClient();
                    queryClient.register(new SignedClientRequestFilter(id, secret));
                    
                    WebTarget lookup = queryClient.target(mapLocation);
                    Invocation.Builder builder = lookup.path("{roomId}").resolveTemplate("roomId", roomId).request(MediaType.APPLICATION_JSON);
                    Response response = builder.get();
                    respString = response.readEntity(String.class);    
                    
                    Log.log(Level.FINE, this, "EXISTING_INFO({0})({1}):{2}", id, room.getRoomId(), respString);
                    
                    reader = Json.createReader(new StringReader(respString));                    
                    queryResponse = reader.readObject();
                                        
                    //save the full response with exit info into the result var.
                    result.type = RegistrationResult.Type.REGISTERED;
                    result.registeredObject = queryResponse;
                    return result;
                }
                case 404:// fall through to 503.
                case 503: {
                    // service was unavailable.. we need to reschedule ourselves
                    // to try again later..
                    if (handling503.compareAndSet(false, true)) {
                        handle503();
                    }
                    result.type = RegistrationResult.Type.SERVICE_UNAVAILABLE;
                    return result;
                }
                default: {
                    throw new Exception("Unknown response code from map " + code);
                }
            }
        } catch (ProcessingException e){
            if(e.getCause() instanceof ConnectException){
                if (handling503.compareAndSet(false, true)) {
                    handle503();
                }
                result.type = RegistrationResult.Type.SERVICE_UNAVAILABLE;
                return result;
            }else{
                throw e;
            }
        } catch (Exception e) {
            throw new Exception("Error querying room registration", e);
        }
    }
    
    private void handle503() throws Exception{
        try{
            Log.log(Level.INFO, this, "Scheduling room {0} to be registered via bg thread.", room.getRoomId());
            ManagedScheduledExecutorService executor;
            executor = (ManagedScheduledExecutorService) new InitialContext().lookup("concurrent/execSvc");         
            
            Thread r = new Thread(){
                public void run() {
                    try{
                        Log.log(Level.INFO, this, "Registration thread for room {0} has awoken.", room.getRoomId());
                        if(performRegistration()){
                            executor.shutdown();
                        }
                    }catch(Exception e){
                        //we're in a thread.. documentation for the scheduled executor service says 
                        //to throw an exception to terminate the scheduler.. here we go.
                        throw new RuntimeException("Registration Thread Fail",e);
                    }
                };
            };
            
            executor.scheduleAtFixedRate(r, 10, 10, TimeUnit.SECONDS);
        }catch(Exception e){
            throw new Exception("Error creating scheduler to handle 503 response from map",e);
        }
    }
    
    public boolean performRegistration() throws Exception{
        RegistrationResult existingRegistration = checkExistingRegistration();
        switch(existingRegistration.type){
            case REGISTERED:{
                RegistrationResult updatedRegistration = compareRoomAndUpdateIfRequired(existingRegistration.registeredObject);
                if(updatedRegistration.type == RegistrationResult.Type.REGISTERED){
                    updateRoomWithExits(updatedRegistration.registeredObject);
                }else{
                    Log.log(Level.SEVERE, this, "Unable to update room registration for room {0}", room.getRoomId());
                    //use old registered room exit info.
                    updateRoomWithExits(existingRegistration.registeredObject);
                }
                return true;
            }
            case NOT_REGISTERED:{
                RegistrationResult newRegistration = registerRoom();
                if(newRegistration.type == RegistrationResult.Type.REGISTERED){
                    updateRoomWithExits(newRegistration.registeredObject);
                }
                return true;
            }
            case SERVICE_UNAVAILABLE:{
                //background thread has been scheduled to re-attempt registration later.                
                return false;
            }
            default:{
                throw new IllegalStateException("Unknown enum value "+existingRegistration.type.toString());
            }               
        }
    }
    
    private void updateRoomWithExits(JsonObject registeredObject) {
        JsonObject exits = registeredObject.getJsonObject("exits");
        Map<String,ExitDesc> exitMap = new HashMap<String,ExitDesc>();
        for(Entry<String, JsonValue> e : exits.entrySet()){
            try{
                JsonObject j = (JsonObject)e.getValue();
                //can be null, eg when linking back to firstroom
                JsonObject c = j.getJsonObject("connectionDetails");
                ExitDesc exit = new ExitDesc(e.getKey(), 
                        j.getString("name"), 
                        j.getString("fullName"), 
                        j.getString("door"), 
                        j.getString("_id"),
                        c!=null?c.getString("type"):null,
                        c!=null?c.getString("target"):null);
                exitMap.put(e.getKey(), exit);
                Log.log(Level.FINER, this, "Added exit {0} to {1} : {2}", e.getKey(), room.getRoomId(), exit);
            }catch(Exception ex){
                Log.log(Level.SEVERE, this, "Unexpected issue reading exit description from room registration",ex);
                //maybe the next exit is good?
            }
        }
        room.setExits(exitMap);
    }

    private RegistrationResult compareRoomAndUpdateIfRequired(JsonObject registeredRoom) throws Exception{
        JsonObject info = registeredRoom.getJsonObject("info");
        
        boolean needsUpdate = true;
        if(   room.getRoomId().equals(info.getString("name"))
           && room.getRoomName().equals(info.getString("fullName"))
           && room.getRoomDescription().equals(info.getString("description"))
                )
        {
            //all good so far =)
            JsonObject doors = info.getJsonObject("doors");
            int count = room.getDoors().size();
            if(doors!=null && doors.size()==count){                
                for(DoorDesc door : room.getDoors()){
                    String description = doors.getString(door.direction.toString().toLowerCase());
                    if(description.equals(door.description)){
                        count--;
                    }
                }
            }else{
                Log.log(Level.INFO,this,"Door count mismatch.");
            }
            //if all the doors matched.. lets check the connection details..
            if(count==0){
                JsonObject connectionDetails = info.getJsonObject("connectionDetails");
                String existingToken = "";
                try {
                    existingToken = connectionDetails.getString("token");
                } catch (NullPointerException e) {
                    //token is optional so if it's not set let the NPE fall through
                }
                if(connectionDetails!=null){
                    if("websocket".equals(connectionDetails.getString("type"))
                       && getEndpointForRoom().equals(connectionDetails.getString("target"))
                       && token.equals(existingToken)){
                        
                        //all good.. no need to update this one.
                        needsUpdate = false;
                        
                    }else{
                        Log.log(Level.INFO,this,"ConnectionDetails mismatch.");
                    }
                }else{
                    Log.log(Level.INFO,this,"ConnectionDetails absent.");
                }
            }else{
                Log.log(Level.INFO,this,"Doors content mismatch.");
            }
        }else{
            Log.log(Level.INFO,this,"Basic room compare failed.");
        }
        
        if(needsUpdate){         
            System.out.println("Update required for "+room.getRoomId());
            Log.log(Level.INFO,this,"Update required for {0}",room.getRoomId());
            return updateRoom(registeredRoom.getString("_id"));
        }else{
            Log.log(Level.INFO,this,"Room {0} is still up to date in Map, no update required.",room.getRoomId());
            RegistrationResult r = new RegistrationResult();
            r.type = RegistrationResult.Type.REGISTERED;
            r.registeredObject = registeredRoom;
            return r;
        }      
    }
    
    private RegistrationResult registerRoom() throws Exception{
        return registerOrUpdateRoom(Mode.REGISTER, null);
    }
    
    private RegistrationResult updateRoom(String roomId) throws Exception{
        return registerOrUpdateRoom(Mode.UPDATE, roomId);
    }
    
    enum Mode {REGISTER,UPDATE};
    private RegistrationResult registerOrUpdateRoom(Mode mode, String roomId) throws Exception{
        Client postClient = ClientBuilder.newClient();

        // add our shared secret so all our queries come from the
        // game-on.org id
        postClient.register(new SignedClientRequestFilter(id, secret));
        
        // create the jax-rs 2.0 client
        WebTarget root = postClient.target(mapLocation);
        
        // build the registration/update payload (post data)
        JsonObjectBuilder registrationPayload = Json.createObjectBuilder();
        // add the basic room info.
        registrationPayload.add("name", room.getRoomId());
        registrationPayload.add("fullName", room.getRoomName());
        registrationPayload.add("description", room.getRoomDescription());
        // add the doorway descriptions we'd like the game to use if it
        // wires us to other rooms.
        JsonObjectBuilder doors = Json.createObjectBuilder();
        for(DoorDesc door : room.getDoors()){
            switch(door.direction){
                case NORTH:{
                    doors.add("n",door.description);
                    break;
                }
                case SOUTH:{
                    doors.add("s",door.description);
                    break;
                }
                case EAST:{
                    doors.add("e",door.description);
                    break;
                }
                case WEST:{
                    doors.add("w",door.description);
                    break;
                }
                case UP:{
                    doors.add("u",door.description);
                    break;
                }
                case DOWN:{
                    doors.add("d",door.description);
                    break;
                }
                default:{
                    throw new IllegalStateException("Bad enum value "+door.direction);
                }
            }
        }
        registrationPayload.add("doors", doors.build());
        
        // add the connection info for the room to connect back to us..
        JsonObjectBuilder connInfo = Json.createObjectBuilder();
        connInfo.add("type", "websocket"); // the only current supported
                                           // type.
        connInfo.add("target", getEndpointForRoom());
        if(!token.isEmpty()) {
            connInfo.add("token", token);       //add security token if it is present
        }
        registrationPayload.add("connectionDetails", connInfo.build());

        Response response=null;
        switch(mode){
            case REGISTER:{
                Invocation.Builder builder = root.request(MediaType.APPLICATION_JSON);
                response = builder.post(Entity.json(registrationPayload.build()));
                break;
            }
            case UPDATE:{
                Invocation.Builder builder = root.path("{roomId}").resolveTemplate("roomId", roomId).request(MediaType.APPLICATION_JSON);
                response = builder.put(Entity.json(registrationPayload.build()));
                break;
            }
            default:{
                throw new IllegalStateException("Bad enum value "+mode.name());
            }
        }
        
        RegistrationResult r = new RegistrationResult();
        try {
            
            if ( (mode.equals(Mode.REGISTER) && Status.CREATED.getStatusCode() == response.getStatus()) ||
                 (mode.equals(Mode.UPDATE) && Status.OK.getStatusCode() == response.getStatus()) ){
                String regString = response.readEntity(String.class);
                JsonReader reader = Json.createReader(new StringReader(regString));
                JsonObject registrationResponse = reader.readObject();

                r.type = RegistrationResult.Type.REGISTERED;
                r.registeredObject = registrationResponse;
                
                Log.log(Level.INFO,this,"Sucessful registration/update operation against ({0})({1})({2}) : {3}",roomId,id,room.getRoomId(),regString);
            } else {
                String resp = response.readEntity(String.class);

                Log.log(Level.SEVERE, "Error registering room provider : {0} : status code {1} : response {2}", room.getRoomName(), response.getStatus(), String.valueOf(resp));

                r.type = RegistrationResult.Type.NOT_REGISTERED;
                
                throw new Exception("Room operation did not report success, got error code "+response.getStatus()+" "+response.getStatusInfo().getReasonPhrase());
            }
        } finally {
            response.close();
        }
        return r;
    }

    private String getEndpointForRoom() {
        return endPoint + "/ws/" +room.getRoomId();
    }
   
    public String getToken() {
        return token;
    }

}
