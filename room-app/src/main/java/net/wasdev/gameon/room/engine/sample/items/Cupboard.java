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
package net.wasdev.gameon.room.engine.sample.items;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Cupboard extends ContainerDesc {

    public final static ContainerDesc.AccessVerificationHandler access = new ContainerDesc.AccessVerificationHandler() {
        @Override
        public boolean verifyAccess(ItemDesc item, String execBy, Room room) {
            // only allow access if execBy player has item heels in inventory,
            // and state is 'worn by execBy'
            User u = room.getUserById(execBy);
            if (u != null) {
                if (u.inventory.contains(Items.stilettoHeels)
                        && Items.stilettoHeels.getState().equals("wornby:" + u.id)) {
                    return true;
                }
            }
            return false;
        }
    };

    public final static ItemDesc.ItemDescriptionHandler handler = new ItemDesc.ItemDescriptionHandler() {
        @Override
        public String getDescription(ItemDesc item, String execBy, String cmd, Room room) {
            ContainerDesc box = (ContainerDesc) item;
            if (box.access.verifyAccess(item, execBy, room)) {
                return "A wall mounted cupboard above the Jukebox, with the shoes on, you are tall enough to reach it.";
            }
            return "A wall mounted cupboard above the Jukebox, it's just out of your reach.";
        }

    };

    public Cupboard() {
        super("Cupboard", null, false, false, new ItemDesc[] { Items.fuse }, access, null, handler);
    }
}
