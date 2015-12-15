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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CopyOnWriteArraySet;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.parser.ItemUseHandler;

public class ContainerDesc extends ItemDesc {
    public final Collection<ItemDesc> items;
    public final Collection<ItemDesc> defaultItems;

    // if you can't access the container, you don't get to know whats in it.
    // it may be out of reach, closed, or locked.
    public interface AccessVerificationHandler {
        public boolean verifyAccess(ItemDesc item, String execBy, Room room);
    }

    public final ContainerDesc.AccessVerificationHandler access;

    public ContainerDesc(String name, String description, boolean takeable, boolean clearStateOnDrop, ItemDesc[] items,
            ContainerDesc.AccessVerificationHandler access, ItemUseHandler handler,
            ItemDesc.ItemDescriptionHandler descHandler) {
        super(name, description, takeable, clearStateOnDrop, handler, descHandler);
        this.access = access;
        this.items = new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items));
        this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
    }

    public ContainerDesc(String name, String description, boolean takeable, boolean clearStateOnDrop, ItemDesc[] items,
            ItemUseHandler handler) {
        this(name, description, takeable, clearStateOnDrop, items, null, handler, null);
    }

    public ContainerDesc(String name, String description, boolean takeable, boolean clearStateOnDrop, ItemDesc[] items,
            ContainerDesc.AccessVerificationHandler access) {
        this(name, description, takeable, clearStateOnDrop, items, access, null, null);
    }

    public ContainerDesc(String name, String description, boolean takeable, boolean clearStateOnDrop,
            ItemDesc[] items) {
        this(name, description, takeable, clearStateOnDrop, items, null, null, null);
    }
}