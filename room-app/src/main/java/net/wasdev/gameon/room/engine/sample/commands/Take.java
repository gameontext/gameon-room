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
package net.wasdev.gameon.room.engine.sample.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.ItemInContainerItem;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Take extends CommandHandler {

    private static final CommandTemplate takeItemInInventory = new CommandTemplateBuilder().build(Type.VERB, "Take")
            .build(Type.INVENTORY_ITEM).build();
    private static final CommandTemplate takeItemInRoom = new CommandTemplateBuilder().build(Type.VERB, "Take")
            .build(Type.ROOM_ITEM).build();
    private static final CommandTemplate takeItemFromContainer = new CommandTemplateBuilder().build(Type.VERB, "Take")
            .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "from").build(Type.CONTAINER_ITEM).build();

    private static final Set<CommandTemplate> templates = Collections.unmodifiableSet(new HashSet<CommandTemplate>(
            Arrays.asList(new CommandTemplate[] { takeItemInRoom, takeItemFromContainer, takeItemInInventory })));

    @Override
    public String getHelpText(){
        return "Pick up an item thats in the room, or take an item **from** a container";
    }
    
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
            if (key.equals(takeItemInRoom.key)) {
                // player tried to take item in room
                Item i = (Item) command.args.get(0);
                if (i.item.takeable) {
                    room.getItems().remove(i.item);
                    u.inventory.add(i.item);
                    room.playerEvent(execBy, "You pick up the " + i.item.name,
                            u.username + " picks up the " + i.item.name);
                } else {
                    room.playerEvent(execBy,
                            "You try really hard to pick up the " + i.item.name + " but it's just too tiring.",
                            u.username + " tries to pick up the " + i.item.name + " and fails.");
                }
            } else if (key.equals(takeItemFromContainer.key)) {
                // player tried to take item from a container.
                ItemInContainerItem i = (ItemInContainerItem) command.args.get(0);
                if (i.item.takeable) {
                    // if we have no access handler, or if we are approved..
                    // then we can take the item
                    if (i.container.access == null || i.container.access.verifyAccess(i.container, execBy, room)) {
                        i.container.items.remove(i.item);
                        u.inventory.add(i.item);
                        room.playerEvent(execBy, "You take the " + i.item.name + " from the " + i.container.name,
                                u.username + " takes the " + i.item.name + " from the " + i.container.name);
                    } else {
                        // the container denied us access, we won't reply with
                        // the item name here, only the container
                        room.playerEvent(execBy,
                                "You can't seem to reach inside the " + i.container.name + " to do that.", null);
                    }
                } else {
                    room.playerEvent(execBy,
                            "You try really hard to take the " + i.item.name + " from the " + i.container.name
                                    + " but it keeps slipping from your grasp.",
                            u.username + " tries to take the " + i.item.name + " from the " + i.container.name
                                    + " and fails.");
                }
            } else {
                // tried to take an item you are already holding..
                Item i = (Item) command.args.get(0);
                room.playerEvent(execBy, "You can't take the " + i.item.name + " because you already have it.", null);
            }
        }
    }

    @Override
    public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
        room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to take " + cmdWithoutVerb, null);
    }

}
