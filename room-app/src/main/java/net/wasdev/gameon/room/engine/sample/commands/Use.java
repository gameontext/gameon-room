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
import java.util.LinkedHashSet;
import java.util.Set;

import net.wasdev.gameon.room.engine.Parser;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Use extends CommandHandler {

    // TODO: one day we may want to allow using items with Players or Exits.
    //
    // for now, we just cover every 'use this item' and 'use this item with that
    // item' templates.

    private final static CommandTemplate useItemInRoom = new CommandTemplateBuilder().build(Type.VERB, "Use")
            .build(Type.ROOM_ITEM).build();
    private final static CommandTemplate useItemInInventory = new CommandTemplateBuilder().build(Type.VERB, "Use")
            .build(Type.INVENTORY_ITEM).build();
    private final static CommandTemplate useItemInContainer = new CommandTemplateBuilder().build(Type.VERB, "Use")
            .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

    private final static CommandTemplate useItemInRoomWithRoomItem = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.ROOM_ITEM).build(Type.LINKWORD, "With").build(Type.ROOM_ITEM).build();
    private final static CommandTemplate useItemInRoomWithInventoryItem = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.ROOM_ITEM).build(Type.LINKWORD, "With").build(Type.INVENTORY_ITEM)
            .build();
    private final static CommandTemplate useItemInRoomWithItemInContainer = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.ROOM_ITEM).build(Type.LINKWORD, "With")
            .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

    private final static CommandTemplate useItemInInventoryWithRoomItem = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With").build(Type.ROOM_ITEM)
            .build();
    private final static CommandTemplate useItemInInventoryWithInventoryItem = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With").build(Type.INVENTORY_ITEM)
            .build();
    private final static CommandTemplate useItemInInventoryWithItemInContainer = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With")
            .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

    private final static CommandTemplate useItemInContainerWithRoomItem = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "With")
            .build(Type.ROOM_ITEM).build();
    private final static CommandTemplate useItemInContainerWithInventoryItem = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "With")
            .build(Type.INVENTORY_ITEM).build();
    private final static CommandTemplate useItemInContainerWithItemInContainer = new CommandTemplateBuilder()
            .build(Type.VERB, "Use").build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "With")
            .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

    private static final Set<CommandTemplate> templates = Collections.unmodifiableSet(
            new LinkedHashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[] { useItemInRoom, useItemInInventory,
                    useItemInContainer, useItemInInventoryWithRoomItem, useItemInInventoryWithInventoryItem,
                    useItemInInventoryWithItemInContainer, useItemInRoomWithRoomItem, useItemInRoomWithInventoryItem,
                    useItemInRoomWithItemInContainer, useItemInContainerWithRoomItem,
                    useItemInContainerWithInventoryItem, useItemInContainerWithItemInContainer })));

    @Override
    public String getHelpText(){
        return "Use an item, or use an item **with** another item.";
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
        User u = room.getUserById(execBy);
        if (u != null) {
            // every template has an item as the first arg.
            Item i = (Item) command.args.get(0);
            if (i.item.useHandler != null) {
                // remove the use verb, the item use handlers do not expect it.
                String cmd = command.originalCommand;
                cmd = Parser.removeFirstWordFromCommand(cmd);
                boolean result = Parser.processCommandHandler(i.item.useHandler, cmd, room, execBy);
                // none of the templates for this handler processed this
                // instance.
                // let the handler generate the failure message.
                if (!result) {
                    i.item.useHandler.processUnknown(room, execBy, command.originalCommand, cmd);
                }
            } else {
                room.playerEvent(execBy, "I'm sorry, but it doesn't look like you can use " + i.item.name, null);
            }
        }
    }

    @Override
    public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
        if (cmdWithoutVerb.trim().length() > 0) {
            room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to use " + cmdWithoutVerb, null);
        } else {
            room.playerEvent(execBy,
                    "Normally, in these text adventurey things, you'd specify the item you wish to use, but you win a prize for being different.",
                    null);
        }
    }

}
