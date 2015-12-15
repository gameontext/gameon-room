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
package net.wasdev.gameon.room.engine.parser;

import java.util.ArrayList;
import java.util.Set;

import net.wasdev.gameon.room.engine.Room;

public abstract class CommandHandler {
    public static class CommandTemplateBuilder {
        private ArrayList<CommandTemplate.ParseNode> args = new ArrayList<CommandTemplate.ParseNode>();

        private void addArg(Node.Type type, String arg) {
            if (args.isEmpty() && type != Node.Type.VERB) {
                throw new RuntimeException("Commands must start with a verb");
            }
            switch (type) {
                case VERB:
                    if (!args.isEmpty())
                        throw new RuntimeException("Verb can only be used as initial node type");
                case LINKWORD:
                    if (arg.contains(":") || arg.contains("/"))
                        throw new RuntimeException("Verb and linkword cannot contain character : or /");
                default:
            }

            CommandTemplate.ParseNode p = new CommandTemplate.ParseNode();
            p.type = type;
            p.data = arg;
            args.add(p);
        }

        public CommandHandler.CommandTemplateBuilder build(Node.Type type, String arg) {
            switch (type) {
                case USER:
                case EXIT:
                case ROOM_ITEM:
                case INVENTORY_ITEM:
                case CONTAINER_ITEM:
                case ITEM_INSIDE_CONTAINER_ITEM:
                    if (arg != null)
                        throw new RuntimeException("No string  argument can be supplied for type " + type.name());
                default:
            }
            addArg(type, arg);
            return this;
        }

        public CommandHandler.CommandTemplateBuilder build(Node.Type type) {
            switch (type) {
                case VERB:
                case LINKWORD:
                    throw new RuntimeException("Verb and linkword must be built with the string as an argument");
                default:
            }
            return build(type, null);
        }

        public CommandTemplate build() {

            StringBuilder sb = new StringBuilder();
            for (CommandTemplate.ParseNode n : args) {
                switch (n.type) {
                    case USER:
                        sb.append("/U:");
                        break;
                    case EXIT:
                        sb.append("/E:");
                        break;
                    case ROOM_ITEM:
                        sb.append("/R:");
                        break;
                    case INVENTORY_ITEM:
                        sb.append("/I:");
                        break;
                    case CONTAINER_ITEM:
                        sb.append("/C:");
                        break;
                    case ITEM_INSIDE_CONTAINER_ITEM:
                        sb.append("/B:");
                        break;
                    case VERB:
                        sb.append("/V:" + n.data.trim().toUpperCase());
                        break;
                    case LINKWORD:
                        sb.append("/L:" + n.data.trim().toUpperCase());
                        break;
                    default:
                        throw new RuntimeException("Unknown node type in Command Template " + n.type.name());
                }
            }
            CommandTemplate t = new CommandTemplate(sb.toString(), args);
            return t;
        }
    }

    public abstract Set<CommandTemplate> getTemplates();

    public abstract boolean isHidden();

    public abstract void processCommand(Room room, String execBy, ParsedCommand command);

    public abstract void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb);
}