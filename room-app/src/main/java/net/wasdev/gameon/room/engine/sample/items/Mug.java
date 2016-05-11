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

import javax.enterprise.inject.spi.CDI;

import net.wasdev.gameon.room.Kafka;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.ItemUseHandler;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Mug extends ItemDesc {

    public static final ItemDescriptionHandler descriptionHandler = new ItemDesc.ItemDescriptionHandler() {
        private static final String mugEmpty = "A Somewhat sturdy container for liquids, with a small handle.";
        private static final String mugFull = "A Somewhat sturdy container for liquids, with a small handle, full of steaming hot coffee.";

        @Override
        public String getDescription(ItemDesc item, String execBy, String cmd, Room room) {
            if (item.getState().equals("full")) {
                return mugFull;
            } else {
                return mugEmpty;
            }
        }
    };

    public final static ItemUseHandler useHandler = new ItemUseHandler() {

        private final CommandTemplate useMugInRoom = new CommandTemplateBuilder().build(Type.ROOM_ITEM).build();
        private final CommandTemplate useMugInInventory = new CommandTemplateBuilder().build(Type.INVENTORY_ITEM)
                .build();
        private final CommandTemplate useMugInContainer = new CommandTemplateBuilder()
                .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

        private final CommandTemplate useMugInRoomWithRoomItem = new CommandTemplateBuilder().build(Type.ROOM_ITEM)
                .build(Type.LINKWORD, "With").build(Type.ROOM_ITEM).build();
        private final CommandTemplate useMugInRoomWithInventoryItem = new CommandTemplateBuilder().build(Type.ROOM_ITEM)
                .build(Type.LINKWORD, "With").build(Type.INVENTORY_ITEM).build();
        private final CommandTemplate useMugInRoomWithItemInContainer = new CommandTemplateBuilder()
                .build(Type.ROOM_ITEM).build(Type.LINKWORD, "With").build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

        private final CommandTemplate useMugInInventoryWithRoomItem = new CommandTemplateBuilder()
                .build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With").build(Type.ROOM_ITEM).build();
        private final CommandTemplate useMugInInventoryWithInventoryItem = new CommandTemplateBuilder()
                .build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With").build(Type.INVENTORY_ITEM).build();
        private final CommandTemplate useMugInInventoryWithItemInContainer = new CommandTemplateBuilder()
                .build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With").build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

        private final CommandTemplate useMugInContainerWithRoomItem = new CommandTemplateBuilder()
                .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "With").build(Type.ROOM_ITEM).build();
        private final CommandTemplate useMugInContainerWithInventoryItem = new CommandTemplateBuilder()
                .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "With").build(Type.INVENTORY_ITEM).build();
        private final CommandTemplate useMugInContainerWithItemInContainer = new CommandTemplateBuilder()
                .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build(Type.LINKWORD, "With")
                .build(Type.ITEM_INSIDE_CONTAINER_ITEM).build();

        private final Set<CommandTemplate> templates = Collections.unmodifiableSet(
                new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[] { useMugInRoom, useMugInInventory,
                        useMugInContainer, useMugInInventoryWithRoomItem, useMugInInventoryWithInventoryItem,
                        useMugInInventoryWithItemInContainer, useMugInRoomWithRoomItem, useMugInRoomWithInventoryItem,
                        useMugInRoomWithItemInContainer, useMugInContainerWithRoomItem,
                        useMugInContainerWithInventoryItem, useMugInContainerWithItemInContainer })));

        private Kafka kafka = CDI.current().select(Kafka.class).get();

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
                Item mug = (Item) command.args.get(0);
                if (key.equals(useMugInInventory.key)) {
                    if (mug.item.getAndSetState("full", "empty")) {
                        room.playerEvent(execBy, "You drink the entire cup of coffee.",
                                u.username + " drinks the mug of coffee.");
                    } else {
                        // note that default state is "" not "empty", so the
                        // else block works great here.
                        room.playerEvent(execBy, "You place the mug on your head. Nothing Happens. You put it back.",
                                null);
                    }
                } else if (key.equals(useMugInInventoryWithInventoryItem.key)
                        || key.equals(useMugInInventoryWithItemInContainer.key)) {
                    // user is holding mug, and trying to use it with an item in
                    // inventory/container..
                    Item i = (Item) command.args.get(2);
                    if ("full".equals(mug.item.getState())) {
                        room.playerEvent(execBy,
                                "You pour the coffee onto the " + i.item.name
                                        + " and wait to see if anything happens. Nope. Not a thing. You pull out a hankerchief and gently dry the "
                                        + i.item.name,
                                u.username + " pours coffee on the " + i.item.name);
                    } else {
                        // note that default state is "" not "empty", so the
                        // else block works great here.
                        room.playerEvent(execBy, "You fiddle with the mug and the " + i.item.name
                                + ". Nothing Happens. You stop fiddling.", null);
                    }
                } else if (key.equals(useMugInInventoryWithRoomItem.key)) {
                    // check if item is coffee machine =)
                    Item i = (Item) command.args.get(2);
                    if (i.item == Items.coffeeMachine) {
                        if (mug.item.getAndSetState("empty", "full") || mug.item.getAndSetState("", "full")) {
                            room.playerEvent(execBy, "You make a hot cup of coffee.",
                                    u.username + " makes a mug of coffee.");
                            if(kafka!=null){
                              Log.log(Level.FINE, this, "Sending message to kafka");
                              kafka.publishMessage("gameon","coffee","User "+u.username+" made coffee in "+room.getRoomName()+" using command '"+command.originalCommand+"'");
                              Log.log(Level.FINE, this, "Sent message to kafka");
                            }else{
                              Log.log(Level.FINE, this, "Kafka bean lookup failed.. ");
                            }
                        } else {
                            room.playerEvent(execBy,
                                    "You attempt to fill the already full cup with more coffee. Coffee goes everywhere, you desperately clean up the coffee hoping nobody noticed.",
                                    u.username + " spills coffee all over the floor, then cleans it up.");
                        }
                    } else {
                        room.playerEvent(execBy, "You try several times to use the " + mug.item.name + " with the "
                                + i.item.name + " but can't seem to figure out how.", null);
                    }
                } else if (key.equals(useMugInRoom.key) || key.equals(useMugInContainer.key)) {
                    room.playerEvent(execBy,
                            "You try to telepathically manipulate the mug, and fail. Perhaps you should take the mug first?",
                            null);
                } else {
                    Item i = (Item) command.args.get(2);
                    // mug was in room, or in cupboard not in users inventory.
                    room.playerEvent(execBy, "You try to telepathically make ther mug interact with the " + i.item.name
                            + ", and fail. Perhaps you should take the mug first?", null);
                }
            }
        }

        @Override
        public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
            room.playerEvent(execBy, "I'm sorry, but I'm just not sure how I'm supposed to use the " + cmdWithoutVerb, null);
        }

    };

    public Mug() {
        super("Mug", null, true, false, useHandler, descriptionHandler);
    }
}
