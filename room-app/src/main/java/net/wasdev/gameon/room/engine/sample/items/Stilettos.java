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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.ItemUseHandler;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Stilettos extends ItemDesc {

    public static ItemUseHandler useHandler = new ItemUseHandler() {

        private final CommandTemplate useStilettosInRoom = new CommandTemplateBuilder().build(Type.ROOM_ITEM).build();
        private final CommandTemplate useStilettosInInventory = new CommandTemplateBuilder().build(Type.INVENTORY_ITEM)
                .build();
        private final CommandTemplate useStilettosWithRoomItem = new CommandTemplateBuilder().build(Type.INVENTORY_ITEM)
                .build(Type.LINKWORD, "With").build(Type.ROOM_ITEM).build();

        private final Set<CommandTemplate> templates = Collections
                .unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[] { useStilettosInRoom,
                        useStilettosInInventory, useStilettosWithRoomItem })));

        @Override
        public Set<CommandTemplate> getTemplates() {
            return templates;
        }

        @Override
        public boolean isHidden() {
            return false;
        }

        @Override
        public void processCommand(Room room, String execBy, ParsedCommand command) {
            String key = command.key;
            User u = room.getUserById(execBy);
            if (u != null) {
                // every template has this item as the first item..
                Item heels = (Item) command.args.get(0);
                if (key.equals(useStilettosInRoom.key)) {
                    room.playerEvent(execBy,
                            "From here, it looks like they might be your size, but you can't be sure, perhaps if you picked them up?",
                            null);
                } else if (key.equals(useStilettosInInventory.key)) {
                    if (heels.item.getAndSetState("", "wornby:" + u.id)) {
                        room.playerEvent(execBy,
                                "You look at the heels carefully, and realise they are just your size. You slip your feet into the shoes, and slowly stand up. You feel taller!",
                                u.username + " wears the stilettos.");
                    } else {
                        // player already wearing heels.
                        room.playerEvent(execBy,
                                "You consider carefully how to use the stilettos now you are already wearing them, and decide to perform a little dance.",
                                u.username + " does a dainty little dance in the heels.");
                    }
                } else if (key.equals(useStilettosWithRoomItem.key)) {
                    Item other = (Item) command.args.get(2);
                    if (other.item.equals(Items.cupboard)) {
                        if (heels.item.getAndSetState("", "wornby:" + u.id)) {
                            room.playerEvent(execBy,
                                    "You look at the heels carefully, and realise they are just your size. You slip your feet into the shoes, and slowly stand up. You feel tall enough to see into the cupboard now.",
                                    u.username + " wears the stilettos.");
                        } else {
                            // player already wearing heels.
                            room.playerEvent(execBy,
                                    "You are already wearing the heels, and are unsure how you can use them with the cupboard in any other way.",
                                    null);
                        }
                    } else {
                        room.playerEvent(execBy, "You try several times to use the stiletto heels with the "
                                + other.item.name + " but can't seem to figure out how.", null);
                    }
                }
            }
        }

        @Override
        public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
            room.playerEvent(execBy, "That doesn't sound like the way you are supposed to use the " + cmdWithoutVerb,
                    null);
        }

    };

    public Stilettos() {
        super("Stilettos", "A bright red pair of six inch stilleto heels.", true, true, useHandler);
    }
}
