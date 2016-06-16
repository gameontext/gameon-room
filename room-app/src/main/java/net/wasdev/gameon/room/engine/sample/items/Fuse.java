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
import net.wasdev.gameon.room.engine.parser.ContainerItem;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.ItemUseHandler;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Fuse extends ItemDesc {

    public static final ItemUseHandler handler = new ItemUseHandler() {
        private final CommandTemplate useInventoryFuseWithRoomItem = new CommandTemplateBuilder()
                .build(Type.INVENTORY_ITEM).build(Type.LINKWORD, "With").build(Type.CONTAINER_ITEM).build();
        private final Set<CommandTemplate> templates = Collections.unmodifiableSet(
                new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[] { useInventoryFuseWithRoomItem })));

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
                Item fuse = (Item) command.args.get(0);
                ContainerItem jb = (ContainerItem) command.args.get(2);
                if (key.equals(useInventoryFuseWithRoomItem.key)) {
                    if (jb.item.equals(Items.jukebox)) {
                        room.playerEvent(execBy, "You take the fuse, and insert it into the jukebox. Fingers crossed!",
                                u.username + " installs the fuse into the jukebox.");
                        jb.container.items.add(Items.fuse);
                        u.inventory.remove(Items.fuse);
                    } else {
                        room.playerEvent(execBy, "You try several times to use the " + fuse.item.name + " with the "
                                + jb.item.name + " but can't seem to figure out how.", null);
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

    public Fuse() {
        super("Fuse", "A small 5 amp cartridge fuse, it appears to be functional.", true, false, handler);

        /*
         * new ItemCommand(){
         *
         * @Override public void processCommand(ItemDesc item, String execBy,
         * String cmd, Room room) { //allow use of fuse with jukebox //using
         * fuse with jukebox should move it from player inv into jukebox. User u
         * = room.getUserById(execBy); if(u!=null){ //remove the 'use itemname'
         * String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
         * //is the next word 'with' ? String next =
         * getFirstWordFromCommand(restOfCmd); if(next==null){
         * room.playerEvent(execBy,
         * "The thing about fuses, is they are not all that interesting, they don't play video games, and they don't have buttons to press."
         * ,null); }else{ if("WITH".equals(next)){ //remove with.. restOfCmd =
         * getCommandWithoutVerbAsString(restOfCmd); //assume rest of string is
         * an item. ItemDesc otherItem = findItemInRoomOrInventory(u, restOfCmd,
         * room); if(otherItem!=null){ if(otherItem.equals(Items.jukebox)){ //is
         * the fuse in the users inventory.. boolean itemIsInInventory =
         * findItemInInventory(item.name, u)!=null; if(itemIsInInventory){
         * //yes, player has item in inventory room.playerEvent(execBy,
         * "You take the fuse, and insert it into the jukebox. Fingers crossed!"
         * ,u.username+" installs the fuse into the jukebox."); ContainerDesc jb
         * = (ContainerDesc)otherItem; jb.items.add(Items.fuse);
         * u.inventory.remove(Items.fuse); }else{ //no, item is in room.
         * room.playerEvent(execBy,
         * "That fuse looks remarkably like it might fit in that jukebox, but the fuse is all the way over there, perhaps you should take the fuse first?"
         * ,null); } }else{ room.playerEvent(execBy,
         * "You try several times to use the "+item.name+" with the "
         * +otherItem.name+" but can't seem to figure out how.",null); } }else{
         * room.playerEvent(execBy, "You aren't quite sure what "+restOfCmd+
         * " is, or how to use it with "+item.name,null); } }else{
         * room.playerEvent(execBy, "I'm sorry dave, I cannot do that!", null);
         * } } }else{ //player not in room anymore. } }});
         */
    }
}
