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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ExitDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.ContainerItem;
import net.wasdev.gameon.room.engine.parser.Exit;
import net.wasdev.gameon.room.engine.parser.InventoryItem;
import net.wasdev.gameon.room.engine.parser.ItemInContainerItem;
import net.wasdev.gameon.room.engine.parser.LinkWord;
import net.wasdev.gameon.room.engine.parser.Node;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;
import net.wasdev.gameon.room.engine.parser.RoomItem;
import net.wasdev.gameon.room.engine.parser.Verb;

public class Parser {

    protected static ItemDesc findItemInInventory(String cmd, String execBy, Room room) {
        User u = room.getUserById(execBy);
        if (u != null) {
            String itemName = getItemNameFromCommand(cmd, room, u);
            for (ItemDesc item : u.inventory) {
                if (item.name.equalsIgnoreCase(itemName)) {
                    return item;
                }
            }
        }
        return null;
    }

    protected static ItemDesc findItemInRoom(String cmd, String execBy, Room room) {
        User u = room.getUserById(execBy);
        if (u != null) {
            String itemName = getItemNameFromCommand(cmd, room, u);
            for (ItemDesc item : room.getItems()) {
                if (item.name.equalsIgnoreCase(itemName)) {
                    return item;
                }
            }
        }
        return null;
    }

    protected static ItemDesc[] findItemInContainerInInventoryOrRoom(String cmd, String execBy, Room room) {
        User u = room.getUserById(execBy);
        if (u != null) {
            String itemName = getItemNameFromCommand(cmd, room, u);
            for (ItemDesc item : room.getItems()) {
                if (item instanceof ContainerDesc) {
                    ContainerDesc box = (ContainerDesc) item;
                    for (ItemDesc boxItem : box.items) {
                        if (boxItem.name.equalsIgnoreCase(itemName)) {
                            return new ItemDesc[] { boxItem, item };
                        }
                    }
                }
            }
            // still here? container wasn't in room, maybe the user has it.
            for (ItemDesc item : u.inventory) {
                if (item instanceof ContainerDesc) {
                    ContainerDesc box = (ContainerDesc) item;
                    for (ItemDesc boxItem : box.items) {
                        if (boxItem.name.equalsIgnoreCase(itemName)) {
                            return new ItemDesc[] { boxItem, item };
                        }
                    }
                }
            }
        }
        return null;
    }

    protected static ContainerDesc findContainerInInventoryOrRoom(String cmd, String execBy, Room room) {
        User u = room.getUserById(execBy);
        if (u != null) {
            String itemName = getItemNameFromCommand(cmd, room, u);
            for (ItemDesc item : room.getItems()) {
                if (item.name.equalsIgnoreCase(itemName) && item instanceof ContainerDesc) {
                    return (ContainerDesc) item;
                }
            }
            // still here? container wasn't in room, maybe the user has it.
            for (ItemDesc item : u.inventory) {
                if (item.name.equalsIgnoreCase(itemName) && item instanceof ContainerDesc) {
                    return (ContainerDesc) item;
                }
            }
        }
        return null;
    }

    protected static ExitDesc findExitInRoom(String cmd, Room room) {
        String exitName = getFirstWordFromCommand(cmd);
        for (ExitDesc exit : room.getExits()) {
            if (exit.getDirection().toString().equalsIgnoreCase(exitName)
                    || exit.getDirection().toLongString().equalsIgnoreCase(exitName)) {
                return exit;
            }
        }
        return null;
    }

    protected static String removeWordFromCommand(String cmd, String word) {
        String cmdWord = getFirstWordFromCommand(cmd);
        if (word.equalsIgnoreCase(cmdWord)) {
            return removeFirstWordFromCommand(cmd);
        } else {
            throw new IllegalArgumentException("Word " + cmdWord + " from command did not match requested word " + word);
        }

    }

    protected static User findUserInRoom(String cmd, Room room) {
        String username = getFirstWordFromCommand(cmd);
        for (User u : room.getAllUsersInRoom()) {
            if (u.username.toUpperCase().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public static String removeFirstWordFromCommand(String cmd) {
        Scanner s = new Scanner(cmd);
        s.next();
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        while (s.hasNext()) {
            if (!first)
                builder.append(" ");
            builder.append(s.next());
            first = false;
        }
        s.close();
        return builder.toString();
    }

    protected static String removeUserFromCommand(String cmd, User u) {
        return removeFirstWordFromCommand(cmd);
    }

    protected static String removeVerbFromCommand(String cmd, String v) {
        return removeFirstWordFromCommand(cmd);
    }

    protected static String removeExitFromCommand(String cmd, ExitDesc e) {
        return removeFirstWordFromCommand(cmd);
    }

    protected static String removeItemFromCommand(String cmd, ItemDesc item) {
        Scanner s = new Scanner(cmd);
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        while (s.hasNext()) {
            if (!first)
                builder.append(" ");
            builder.append(s.next().toUpperCase());
            if (builder.toString().equals(item.name.toUpperCase())) {
                builder = new StringBuilder();
            }
            first = false;
        }
        s.close();
        return builder.toString();
    }

    protected static String getItemNameFromCommand(String cmd, Room room, User execBy) {
        List<String> allItems = new ArrayList<String>();
        for (ItemDesc item : room.getItems()) {
            allItems.add(item.name.trim().toUpperCase());
            if (item instanceof ContainerDesc) {
                ContainerDesc box = (ContainerDesc) item;
                for (ItemDesc boxItem : box.items) {
                    allItems.add(boxItem.name.trim().toUpperCase());
                }
            }
        }
        for (ItemDesc item : execBy.inventory) {
            allItems.add(item.name.trim().toUpperCase());
            if (item instanceof ContainerDesc) {
                ContainerDesc box = (ContainerDesc) item;
                for (ItemDesc boxItem : box.items) {
                    allItems.add(boxItem.name.trim().toUpperCase());
                }
            }
        }
        // sort so we process longer item names first =)
        Collections.sort(allItems, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int o1len = o1.length();
                int o2len = o2.length();
                if (o1len > o2len) {
                    return -1;
                } else if (o1len < o2len) {
                    return 1;
                } else
                    return o1.compareTo(o2);
            }
        });
        String uCmd = cmd.toUpperCase();
        for (String item : allItems) {
            if (uCmd.startsWith(item))
                return item;
        }
        return null;
    }

    protected static String getFirstWordFromCommand(String cmd) {
        Scanner s = new Scanner(cmd);
        String result = null;
        if (s.hasNext()) {
            result = s.next().trim().toUpperCase();
        }
        s.close();
        return result;
    }

    public static void parseInput(Map<String, CommandHandler> commands, String s, Room room, String execBy) {
        // do parse, create ParsedCommand to return, or throw exception.

        // backup original user command for reference.
        String origCmd = s;

        // all verbs must be single words.
        // parse in first word..
        String first = getFirstWordFromCommand(s);

        // lookup matching handlers & templates from the map
        CommandHandler h = commands.get(first);
        if (h != null) {

            boolean result = processCommandHandler(h, origCmd, room, execBy);

            if (!result) {
                // verb was recognised, but no template matched..
                h.processUnknown(room, execBy, origCmd, removeWordFromCommand(origCmd, first));
            }
        } else {
            // command verb was unknown to map..
            throw new RuntimeException("Unknown Command");
        }
    }

    public static boolean processCommandHandler(CommandHandler h, String origCmd, Room room, String execBy) {
        // try to parse the command using each template.
        for (CommandTemplate t : h.getTemplates()) {
            try {
                String cmd = origCmd;
                List<Node> parsed = new ArrayList<Node>();
                for (CommandTemplate.ParseNode node : t.template) {
                    switch (node.type) {
                        case USER:
                            cmd = processUserCommand(room, t, cmd, parsed);
                            break;
                        case EXIT:
                            cmd = processExitCommand(room, t, cmd, parsed);
                            break;
                        case ROOM_ITEM:
                            cmd = processRoomItemCommand(room, execBy, t, cmd, parsed);
                            break;
                        case INVENTORY_ITEM:
                            cmd = processInventoryItemCommand(room, execBy, t, cmd, parsed);
                            break;
                        case CONTAINER_ITEM:
                            cmd = processContainerItemCommand(room, execBy, t, cmd, parsed);
                            break;
                        case ITEM_INSIDE_CONTAINER_ITEM:
                            cmd = processItemInsideContainerCommand(room, execBy, t, cmd, parsed);
                            break;
                        case VERB:
                            cmd = processVerbCommand(cmd, parsed, node);
                            break;
                        case LINKWORD:
                            cmd = processLinkWordCommand(cmd, parsed, node);
                            break;
                        default:
                            break;
                    }
                }

                // if we made it to here, then we just matched every part of
                // a template..
                // but .. is there more on the command we haven't looked at
                // yet?
                boolean complete = cmd.trim().length() == 0;
                if (complete) {
                    ParsedCommand p = new ParsedCommand(origCmd, parsed);
                    h.processCommand(room, execBy, p);
                    return true;
                }

            } catch (Exception e) {
                // we couldn't match this template..
                // ignore it.. maybe the next template will match
            }
        }
        return false;
    }

    private static String processLinkWordCommand(String cmd, List<Node> parsed, CommandTemplate.ParseNode node) {
        // if node.data doesn't match the word, we'll
        // exception our way out of here.
        cmd = removeWordFromCommand(cmd, node.data);
        Node l = new LinkWord(node.data);
        parsed.add(l);
        return cmd;
    }

    private static String processVerbCommand(String cmd, List<Node> parsed, CommandTemplate.ParseNode node) {
        // verb was already matched outside, so we can just
        // eat it here.
        cmd = removeVerbFromCommand(cmd, node.data);
        Node v = new Verb(node.data);
        parsed.add(v);
        return cmd;
    }

    private static String processItemInsideContainerCommand(Room room, String execBy, CommandTemplate t, String cmd,
            List<Node> parsed) {
        ItemDesc iiitem[] = findItemInContainerInInventoryOrRoom(cmd, execBy, room);
        if (iiitem != null) {
            cmd = removeItemFromCommand(cmd, iiitem[0]);
            Node n = new ItemInContainerItem((ContainerDesc) iiitem[1], iiitem[0]);
            parsed.add(n);
        } else {
            throw new IllegalStateException("Unable to match room arg for template " + t.key);
        }
        return cmd;
    }

    private static String processContainerItemCommand(Room room, String execBy, CommandTemplate t, String cmd,
            List<Node> parsed) {
        ContainerDesc citem = findContainerInInventoryOrRoom(cmd, execBy, room);
        if (citem != null) {
            cmd = removeItemFromCommand(cmd, citem);
            Node n = new ContainerItem(citem);
            parsed.add(n);
        } else {
            throw new IllegalStateException("Unable to match room arg for template " + t.key);
        }
        return cmd;
    }

    private static String processInventoryItemCommand(Room room, String execBy, CommandTemplate t, String cmd,
            List<Node> parsed) {
        ItemDesc iitem = findItemInInventory(cmd, execBy, room);
        if (iitem != null) {
            cmd = removeItemFromCommand(cmd, iitem);
            Node n = new InventoryItem(iitem);
            parsed.add(n);
        } else {
            throw new IllegalStateException("Unable to match room arg for template " + t.key);
        }
        return cmd;
    }

    private static String processRoomItemCommand(Room room, String execBy, CommandTemplate t, String cmd,
            List<Node> parsed) {
        ItemDesc ritem = findItemInRoom(cmd, execBy, room);
        if (ritem != null) {
            cmd = removeItemFromCommand(cmd, ritem);
            Node n = new RoomItem(ritem);
            parsed.add(n);
        } else {
            throw new IllegalStateException("Unable to match room arg for template " + t.key);
        }
        return cmd;
    }

    private static String processExitCommand(Room room, CommandTemplate t, String cmd, List<Node> parsed) {
        ExitDesc exit = findExitInRoom(cmd, room);
        if (exit != null) {
            cmd = removeExitFromCommand(cmd, exit);
            Node n = new Exit(exit);
            parsed.add(n);
        } else {
            throw new IllegalStateException("Unable to match room arg for template " + t.key);
        }
        return cmd;
    }

    private static String processUserCommand(Room room, CommandTemplate t, String cmd, List<Node> parsed) {
        User user = findUserInRoom(cmd, room);
        if (user != null) {
            cmd = removeUserFromCommand(cmd, user);
            Node n = new net.wasdev.gameon.room.engine.parser.User(user);
            parsed.add(n);
        } else {
            throw new IllegalStateException("Unable to match room arg for template " + t.key);
        }
        return cmd;
    }

}
