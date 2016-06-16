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
package net.wasdev.gameon.room.engine.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

public class RoomDesc {

    public final String id;
    public final String name;
    public final String description;
    public final Collection<ItemDesc> items;
    public final Collection<ItemDesc> defaultItems;
    public final Collection<DoorDesc> doorways;

    public RoomDesc(String id, String name, String description, ItemDesc[] items,  DoorDesc[] doorways) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.items = new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items));
        this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
        this.doorways = Collections.unmodifiableList(new ArrayList<DoorDesc>(Arrays.asList(doorways)));
    }
}
