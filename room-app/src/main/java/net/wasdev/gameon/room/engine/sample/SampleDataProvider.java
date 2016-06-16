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
import net.wasdev.gameon.room.engine.meta.DoorDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.meta.RoomDesc;
import net.wasdev.gameon.room.engine.parser.CommandHandler;
import net.wasdev.gameon.room.engine.sample.commands.Drop;
import net.wasdev.gameon.room.engine.sample.commands.Examine;
import net.wasdev.gameon.room.engine.sample.commands.Go;
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
            .asList(new CommandHandler[] { new Drop(), new Examine(), new Go(),
                    new Inventory(), new ListPlayers(), new Look(), new Quit(), new Reset(), new Take(), new Use() });

    DoorDesc recRoomN = new DoorDesc(DoorDesc.Direction.NORTH,"A dark alleyway, with a Neon lit sign saying 'Rec Room', you can hear the feint sounds of a jukebox playing.");
    DoorDesc recRoomS = new DoorDesc(DoorDesc.Direction.SOUTH,"Hidden behind piles of trash, you think you can make out the back entrance to the Rec Room.");
    DoorDesc recRoomE = new DoorDesc(DoorDesc.Direction.EAST,"The window on the wall of the Rec Room looks large enough to climb through.");
    DoorDesc recRoomW = new DoorDesc(DoorDesc.Direction.WEST,"The doorway has a sign saying 'Rec Room' beneath it, about halfway down the door, someone has written 'No Goblins' in crayon.");

    RoomDesc bar = new RoomDesc("RecRoom", "Rec Room",
            "A dimly lit shabbily decorated room, that appears tired and dated. It looks like someone attempted to provide kitchen facilities here once, but you really wouldn't want to eat anything off those surfaces!",
            new ItemDesc[] { Items.mug, Items.coffeeMachine, Items.stilettoHeels, Items.jukebox, Items.cupboard },
            new DoorDesc[] { recRoomN, recRoomS, recRoomE, recRoomW });

    DoorDesc basementN = new DoorDesc(DoorDesc.Direction.NORTH,"A very dark opening leads downwards toward a quiet space.");
    DoorDesc basementS = new DoorDesc(DoorDesc.Direction.SOUTH,"A dark doorway, covered in cobwebs.");
    DoorDesc basementE = new DoorDesc(DoorDesc.Direction.EAST,"A pile of bricks with a gap beside it, you think can just about squeeze past.");
    DoorDesc basementW = new DoorDesc(DoorDesc.Direction.WEST,"A very dark archway, you can't quite make out what's beyond.");

    RoomDesc basement = new RoomDesc("Basement", "Basement",
            "A dark basement. It is dark here. You think you should probably leave.",
            new ItemDesc[] {}, new DoorDesc[] { basementN, basementS, basementE, basementW });

    DoorDesc mugRoomN = new DoorDesc(DoorDesc.Direction.NORTH,"A doorway that smells slightly of coffee");
    DoorDesc mugRoomS = new DoorDesc(DoorDesc.Direction.SOUTH,"A doorway that smells slightly of coffee");
    DoorDesc mugRoomE = new DoorDesc(DoorDesc.Direction.EAST,"A doorway that smells slightly of coffee");
    DoorDesc mugRoomW = new DoorDesc(DoorDesc.Direction.WEST,"A doorway that smells slightly of coffee");

    RoomDesc mugRoom = new RoomDesc("MugRoom", "The Room with The Mug",
            "The room is rather clinical, and entirely white, in the center of the floor sits a lonely mug. There is a sign on the wall here.",
            new ItemDesc[] {Items.mug, Items.mugRoomSign}, new DoorDesc[] { mugRoomS, mugRoomN, mugRoomE, mugRoomW });

    Collection<Room> rooms = new ArrayList<Room>(
            Arrays.asList(new Room[] { new Room(bar, globalCommands), new Room(basement, globalCommands), new Room(mugRoom, globalCommands)}));

    @Override
    public Collection<Room> getRooms() {
        return rooms;
    }

}
