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

    public interface ExitHandler {
        /**
         * @param execBy
         *            CAN BE NULL (for getDescription only), means supply
         *            default for concierge at registration.
         */
        public String getDescription(String execBy, ExitDesc exit, Room exitOwner);

        public String getSelfDepartMessage(String execBy, ExitDesc exit, Room exitOwner);

        public String getOthersDepartMessage(String execBy, ExitDesc exit, Room exitOwner);

        public boolean isVisible();

        public boolean isTraversable(String execBy, ExitDesc exit, Room exitOwner);
    }

    public final Direction direction;
    public final String targetRoomId;
    public final ExitHandler handler;

    public ExitDesc(String targetRoomId, Direction dir, String description) {
        this(targetRoomId, dir, description, true);
    }

    public ExitDesc(String targetRoomId, Direction dir, final String description, final boolean visible) {
        // build a default exit handler that has
        this(targetRoomId, dir, new ExitHandler() {
            @Override
            public String getDescription(String execBy, ExitDesc exit, Room exitOwner) {
                return description;
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
            public boolean isVisible() {
                return visible;
            }

            @Override
            public boolean isTraversable(String execBy, ExitDesc exit, Room exitOwner) {
                return true;
            }
        });
    }

    public ExitDesc(String targetRoomId, Direction dir, ExitHandler handler) {
        this.direction = dir;
        this.handler = handler;
        this.targetRoomId = targetRoomId;
    }

}
