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

public class LinkWord extends Node {
    public final String word;

    public LinkWord(String word) {
        this.word = word.trim().toUpperCase();
        if (word.contains(":") || word.contains("/")) {
            throw new RuntimeException("Forbidden characters : or / found in linkword name");
        }
    }

    public Type getType() {
        return Type.LINKWORD;
    }

    public String getKey() {
        return "/L:" + word;
    }
}