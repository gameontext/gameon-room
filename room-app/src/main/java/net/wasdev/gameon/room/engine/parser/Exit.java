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

import net.wasdev.gameon.room.engine.meta.ExitDesc;

public class Exit extends Node {
    public final ExitDesc exit;

    public Exit(ExitDesc exit) {
        this.exit = exit;
    }

    public Type getType() {
        return Type.EXIT;
    }

    public String getKey() {
        return "/E:";
    }
}
