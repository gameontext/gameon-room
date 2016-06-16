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
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class Reset extends CommandHandler {

    private static final CommandTemplate reset = new CommandTemplateBuilder().build(Type.VERB, "Reset").build();

    private static final Set<CommandTemplate> templates = Collections
            .unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[] { reset })));

    @Override
    public String getHelpText(){
        return "Reset the room back to it's default state";
    }

    @Override
    public Set<CommandTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public void processCommand(Room room, String execBy, ParsedCommand command) {
        // we'll add the ability to identify admin users via the db later
        if ("twitter:281946000".equals(execBy)) {
            room.resetRoom();
            room.playerEvent(execBy, "Reset executed. Verify room contents.",
                    "An odd feeling comes over you, like you just felt a glitch in the matrix");
        } else {
            User u = room.getUserById(execBy);
            if (u != null) {
                room.playerEvent(execBy,
                        "You hit the invisble reset button, hoping it does something. A beige futuristic archway appears.",
                        u.username + " says \"Computer. Arch!\" and an archway appears, " + u.username
                                + " enters the archway and requests a holodeck reset.");
                room.roomEvent("Access denied: " + u.username
                        + " is not recognised by the ships computer as being a command officer. This infraction has been noted.");
                room.roomEvent("The archway disappears");
            }
        }
    }

    @Override
    public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
        room.playerEvent(execBy, "I'm sorry, but I'm not sure how I'm supposed to reset " + cmdWithoutVerb, null);
    }

}
