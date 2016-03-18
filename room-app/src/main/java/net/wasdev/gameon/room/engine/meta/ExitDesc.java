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
package net.wasdev.gameon.room.engine.meta;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import net.wasdev.gameon.room.engine.Room;

public class ExitDesc {

    public enum Direction {
        NORTH("N", "North"), SOUTH("S", "South"), EAST("E", "East"), WEST("W", "West"), UP("U", "Up"), DOWN("D",
                "Down");
        private final String shortName;
        private final String longName;

        Direction(String shortName, String longName) {
            this.shortName = shortName;
            this.longName = longName;
        }

        public String toString() {
            return shortName;
        }

        public String toLongString() {
            return longName;
        }
    };
    
    public final Direction direction; 
    public final String name;
    public final String fullName; 
    private final String doorDescription; 
    public final String targetId;
    public final String connectionType; 
    public final String connectionTarget;
    public final ExitHandler handler;
    
    public interface ExitHandler {

        public String getDescription(ExitDesc exit, Room exitOwner);

        public String getSelfDepartMessage(String execBy, ExitDesc exit, Room exitOwner);

        public String getOthersDepartMessage(String execBy, ExitDesc exit, Room exitOwner);

        public boolean isTraversable(String execBy, ExitDesc exit, Room exitOwner);
    }
    
    public ExitDesc(String direction, String name, String fullName, String doorDescription, String targetId,
            String connectionType, String connectionTarget) {
        super();
        switch(direction.toLowerCase().trim()){
            case "n":{
                this.direction = Direction.NORTH; break;
            }
            case "s":{
                this.direction = Direction.SOUTH; break;
            }
            case "e":{
                this.direction = Direction.EAST; break;
            }
            case "w":{
                this.direction = Direction.WEST; break;
            }
            case "u":{
                this.direction = Direction.UP; break;
            }
            case "d":{
                this.direction = Direction.DOWN; break;
            }
            default:{
                throw new IllegalArgumentException("Unknown direction "+direction);
            }                
        }
        this.name = name;
        this.fullName = fullName;
        this.doorDescription = doorDescription;
        this.targetId = targetId;
        this.connectionType = connectionType;
        this.connectionTarget = connectionTarget;
        
        this.handler = new ExitHandler() {
            @Override
            public String getDescription(ExitDesc exit, Room exitOwner) {
                return doorDescription;
            }

            @Override
            public String getSelfDepartMessage(String execBy, ExitDesc exit, Room exitOwner) {
                return "You head " + exit.direction.toLongString();
            }

            @Override
            public String getOthersDepartMessage(String execBy, ExitDesc exit, Room exitOwner) {
                return exitOwner.getUserById(execBy).username + " leaves, headed " + exit.direction.toLongString();
            }

            @Override
            public boolean isTraversable(String execBy, ExitDesc exit, Room exitOwner) {
                return true;
            }
        };
    }

    public Direction getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDoorDescription() {
        return doorDescription;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getConnectionTarget() {
        return connectionTarget;
    }
    
    public String toString() {
        return toJsonString();
    }
    public JsonObject toJsonObject(){
        JsonObjectBuilder obj = Json.createObjectBuilder();
        obj.add("name", name);
        obj.add("fullName", fullName);
        obj.add("door", doorDescription);
        obj.add("id", targetId);
        if(connectionType!=null && connectionTarget!=null){
            JsonObjectBuilder cd = Json.createObjectBuilder();
            cd.add("type", connectionType);
            cd.add("target", connectionTarget);
            obj.add("connectionDetails", cd.build());
        }
        return obj.build();
    }
    public String toJsonString(){
        return toJsonObject().toString();
    }
}
