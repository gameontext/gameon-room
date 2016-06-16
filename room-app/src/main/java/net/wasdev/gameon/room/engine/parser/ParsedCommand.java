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
import java.util.Collections;
import java.util.List;

public class ParsedCommand {
    public final String originalCommand;
    public final Verb verb;
    public final List<Node> args;
    public final String key;

    public ParsedCommand(String command, List<Node> parsed) {
        // build the key.
        StringBuilder sb = new StringBuilder();
        for (Node n : parsed) {
            sb.append(n.getKey());
        }
        this.key = sb.toString();

        ArrayList<Node> n = new ArrayList<Node>(parsed);
        // split out the verb and remove it from the args when there is one.
        if (Node.Type.VERB == parsed.get(0).getType()) {
            verb = (Verb) parsed.get(0);
            n.remove(0);
        } else {
            verb = null;
        }
        args = Collections.unmodifiableList(n);
        this.originalCommand = command;
    }
}
