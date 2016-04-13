/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package net.wasdev.gameon.room.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.wasdev.gameon.room.Log;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.DoorDesc;
import net.wasdev.gameon.room.engine.meta.ExitDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.meta.RoomDesc;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;

public class Room {
    
    public final String TOKEN_ID;
    private Map<String, ExitDesc> exitMap;    
    private RoomDesc roomDesc;
    private Map<String, User> userMap = new ConcurrentHashMap<String, User>();
    private Map<String, CommandHandler> commandMap = new HashMap<String, CommandHandler>();    
    private Room.RoomResponseProcessor rrp = new DebugResponseProcessor();

    public interface RoomResponseProcessor {
        // "Player message :: from("+senderId+")
        // onlyForSelf("+String.valueOf(selfMessage)+")
        // others("+String.valueOf(othersMessage)+")"
        public void playerEvent(String senderId, String selfMessage, String othersMessage);

        // "Message sent to everyone :: "+s
        public void roomEvent(String s);

        public void locationEvent(String senderId, String roomId, String roomName, String roomDescription, Map<String,String> exits,
                List<String> objects, List<String> inventory, Map<String,String> commands);

        public void exitEvent(String senderId, String exitMessage, String exitID, String exitJson);

    }

    public static class DebugResponseProcessor implements Room.RoomResponseProcessor {
        @Override
        public void playerEvent(String senderId, String selfMessage, String othersMessage) {
            System.out.println("Player message :: from(" + senderId + ") onlyForSelf(" + String.valueOf(selfMessage)
                    + ") others(" + String.valueOf(othersMessage) + ")");
        }

        @Override
        public void roomEvent(String s) {
            System.out.println("Message sent to everyone :: " + s);
        }

        @Override
        public void locationEvent(String senderId, String roomId, String roomName, String roomDescription,Map<String,String> exits,
                List<String> objects, List<String> inventory, Map<String,String> commands) {
            System.out.println("Location: " + roomName + " (For " + senderId + ") " + roomDescription);
            if (exits.isEmpty()){
                System.out.println("There are no exits.");
            }else{
                for( Entry<String, String> exit : exits.entrySet()){
                    System.out.println(" - "+exit.getKey()+" "+exit.getValue());
                }
            }
            if (!objects.isEmpty()) {
                System.out.println("You can see the following items: " + objects);
            }
            if (!inventory.isEmpty()) {
                System.out.println("You are carrying " + inventory);
            }
        }

        @Override
        public void exitEvent(String senderId, String m, String id, String exitJson) {
            System.out.println("Exit succeeded : " + m + " to " + id);
        }
    }

    public Room(RoomDesc r, List<CommandHandler> globalCommands) {
        roomDesc = r;
        for (CommandHandler c : globalCommands) {
            for (CommandTemplate t : c.getTemplates()) {
                CommandTemplate.ParseNode verb = t.template.get(0);
                commandMap.put(verb.data.toUpperCase(), c);
            }
        }
        TOKEN_ID = r.id + "_token";     //the name that will be used to query JNDI to see if a token has been defined for this room
    }

    public Map<String, String> getExitsMap(String senderId, Room room) {
        Map<String, String> exitMap = new HashMap<String, String>();
        for (ExitDesc e : this.exitMap.values()) {
            exitMap.put(e.getDirection().toString().toLowerCase(), e.getDoorDescription());
        }
        return exitMap;
    }
    
    public Collection<ExitDesc> getExits(){
        return exitMap.values();
    }

    public void locationEvent(String senderId, Room room, String roomDescription, Collection<ExitDesc> exits,
            List<String> objects, List<String> inventory, Map<String,String> commands) {        
        rrp.locationEvent(senderId, room.getRoomId(), room.getRoomName(), roomDescription, getExitsMap(senderId, room), objects, 
                inventory,commands);
    }

    public void playerEvent(String senderId, String selfMessage, String othersMessage) {
        rrp.playerEvent(senderId, selfMessage, othersMessage);
    }

    public void roomEvent(String s) {
        rrp.roomEvent(s);
    }

    public void exitEvent(String senderId, String exitMessage, String exitId) {
        rrp.exitEvent(senderId, exitMessage, exitId, null);
    }

    public void setRoomResponseProcessor(Room.RoomResponseProcessor rrp) {
        this.rrp = rrp;
    }

    public void addUserToRoom(String id, String username) {
        User u = new User(id, username);
        if (!userMap.containsKey(id)) {
            userMap.put(id, u);
            this.roomEvent(u.username + " enters the room.");
        }
    }

    public void removeUserFromRoom(String id) {
        if (userMap.containsKey(id)) {
            User u = userMap.get(id);
            // drop all items in the users inventory when they leave.
            Iterator<ItemDesc> itemIter = u.inventory.iterator();
            while (itemIter.hasNext()) {
                ItemDesc item = itemIter.next();
                // add to the room
                this.roomDesc.items.add(item);
                // remove from the user.
                itemIter.remove();
                // reset item state if needed
                if(item.clearStateOnDrop){
                    item.setState("");
                }
                this.playerEvent(id, "You drop the " + item.name, u.username + " drops the " + item.name);
            }
            userMap.remove(id);
            this.roomEvent(u.username + " leaves the room.");
        } else {
            Log.log(Level.WARNING, this, "Unable to remove {0} from room {1} because user is not known to room", id,roomDesc.id);
        }
    }

    public void command(String userid, String cmd) {
        try {
            Parser.parseInput(commandMap, cmd, this, userid);
        } catch (RuntimeException e) {
            this.playerEvent(userid, "I'm sorry Dave, I don't know how to do that", null);
        }
    }

    public String getRoomId() {
        return roomDesc.id;
    }

    public String getRoomName() {
        return roomDesc.name;
    }

    public String getRoomDescription() {
        return roomDesc.description;
    }

    public User getUserById(String id) {
        return userMap.get(id);
    }

    public Collection<User> getAllUsersInRoom() {
        return userMap.values();
    }

    public Collection<ItemDesc> getItems() {
        return roomDesc.items;
    }
    
    public Collection<DoorDesc> getDoors() {
        return roomDesc.doorways;
    }

    public void resetRoom() {
        for (User u : userMap.values()) {
            u.inventory.clear();
        }
        roomDesc.items.clear();
        roomDesc.items.addAll(roomDesc.defaultItems);
        for (ItemDesc item : roomDesc.items) {
            item.setState("");
            if (item instanceof ContainerDesc) {
                ContainerDesc box = (ContainerDesc) item;
                box.items.clear();
                box.items.addAll(box.defaultItems);
            }
        }
    }

    public Collection<CommandHandler> getCommands() {
        return commandMap.values();
    }

    public void setExits(Map<String, ExitDesc> exitMap) {
        Map<String,ExitDesc> exits = new HashMap<String,ExitDesc>();
        exits.putAll(exitMap);
        this.exitMap = Collections.unmodifiableMap(exits);        
    }
    
}