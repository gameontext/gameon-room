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
package net.wasdev.gameon.room.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class Room {

    private UUID assignedID = UUID.randomUUID(); // default to a random UUID
    private String roomName;
    private final List<Exit> exits = new ArrayList<Exit>();
    private final Map<String, String> attribs = new HashMap<String, String>();

    public Room() {
        // no-args constructor to allow JSON serialisation
    }

    public Room(String roomName) {
        super();
        setRoomName(roomName);
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getAttribute(String name) {
        return attribs.get(name);
    }

    public void setAttribute(String name, String value) {
        attribs.put(name, value);
    }

    public Map<String, String> getAttributes() {
        return attribs;
    }

    public List<Exit> getExits() {
        return exits;
    }

    public UUID getAssignedID() {
        return assignedID;
    }

    public void setAssignedID(UUID assignedID) {
        this.assignedID = assignedID;
    }

    /*
     * Takes a copy of the exits. Subsequent changes in the supplied list will
     * not be reflected.
     */
    public void setExits(List<Exit> exits) {
        this.exits.clear();
        this.exits.addAll(exits);
    }

    public void addExit(Exit exit) {
        exits.add(exit);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Room : ");
        builder.append(roomName);
        builder.append('\n');
        builder.append("Attributes : \n");
        for (Entry<String, String> attrib : attribs.entrySet()) {
            builder.append('\t');
            builder.append(attrib.getKey());
            builder.append(" : ");
            builder.append(attrib.getValue());
            builder.append('\n');
        }
        builder.append("Exits : \n");
        for (Exit exit : exits) {
            builder.append('\t');
            builder.append(exit.toString());
            builder.append('\n');
        }
        return builder.toString();
    }

}
