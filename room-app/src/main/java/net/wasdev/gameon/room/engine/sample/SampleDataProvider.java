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
package net.wasdev.gameon.room.engine.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.wasdev.gameon.room.engine.DataProvider;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.meta.ExitDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.meta.RoomDesc;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.sample.commands.Drop;
import net.wasdev.gameon.room.engine.sample.commands.Examine;
import net.wasdev.gameon.room.engine.sample.commands.Exits;
import net.wasdev.gameon.room.engine.sample.commands.Go;
import net.wasdev.gameon.room.engine.sample.commands.Help;
import net.wasdev.gameon.room.engine.sample.commands.Inventory;
import net.wasdev.gameon.room.engine.sample.commands.ListPlayers;
import net.wasdev.gameon.room.engine.sample.commands.Look;
import net.wasdev.gameon.room.engine.sample.commands.Quit;
import net.wasdev.gameon.room.engine.sample.commands.Reset;
import net.wasdev.gameon.room.engine.sample.commands.Take;
import net.wasdev.gameon.room.engine.sample.commands.Use;
import net.wasdev.gameon.room.engine.sample.items.Items;

public class SampleDataProvider implements DataProvider {

    static List<CommandHandler> globalCommands = Arrays
            .asList(new CommandHandler[] { new Drop(), new Examine(), new Exits(), new Go(), new Help(),
                    new Inventory(), new ListPlayers(), new Look(), new Quit(), new Reset(), new Take(), new Use() });

    ExitDesc barToBasement = new ExitDesc("Basement", ExitDesc.Direction.DOWN,
            "A flight of stairs leading into a dark basement");
    ExitDesc barToNode = new ExitDesc("TheNodeRoom", ExitDesc.Direction.EAST,
            "A strange looking door that has dot js written on it");

    RoomDesc bar = new RoomDesc("RecRoom", "Rec Room",
            "A dimly lit shabbily decorated room, that appears tired and dated. It looks like someone attempted to provide kitchen facilities here once, but you really wouldn't want to eat anything off those surfaces!",
            true, // is a starter location.
            new ItemDesc[] { Items.mug, Items.coffeeMachine, Items.stilettoHeels, Items.jukebox, Items.cupboard },
            new ExitDesc[] { barToBasement, barToNode });

    ExitDesc basementToBar = new ExitDesc("RecRoom", ExitDesc.Direction.UP,
            "A flight of stairs leading back to the Rec Room");
    RoomDesc basement = new RoomDesc("Basement", "Basement",
            "A dark basement. It is dark here. You think you should probably leave.", false, // is
                                                                                             // a
                                                                                             // starter
                                                                                             // location.
            new ItemDesc[] {}, new ExitDesc[] { basementToBar });

    Collection<Room> rooms = new ArrayList<Room>(
            Arrays.asList(new Room[] { new Room(bar, globalCommands), new Room(basement, globalCommands), }));

    @Override
    public Collection<Room> getRooms() {
        return rooms;
    }

}
