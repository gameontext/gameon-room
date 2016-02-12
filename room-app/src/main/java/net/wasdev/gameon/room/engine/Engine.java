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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import net.wasdev.gameon.room.engine.meta.ExitDesc;
import net.wasdev.gameon.room.engine.sample.SampleDataProvider;

public class Engine {

    // eventually we'll let this be customizable..
    DataProvider dp = new SampleDataProvider();

    public Collection<Room> getRooms() {
        // wrap it into an unmodifiable to prevent accidents ;p
        return Collections.unmodifiableCollection(dp.getRooms());
    }

    private Engine() {
    }

    private static final Engine engine = new Engine();

    public static Engine getEngine() {
        return engine;
    }

    /**
     * Console based test rig.
     */
    public static void main(String[] args) {

        Engine e = new Engine();

        Collection<Room> rooms = e.getRooms();

        Room current = rooms.iterator().next();
        Map<String,ExitDesc> exits = new HashMap<String,ExitDesc>();
        exits.put("N",new ExitDesc("n","roomName","roomFullName","doorDescriptionText","targetRoomId","type","target"));
        current.setExits(exits);

        // go interactive ;p
        System.out.println("---[[[ OZMONSTA Engine v1.0, EXIT to quit. Room '" + current.getRoomId() + "' ]]]---");
        current.addUserToRoom("oz", "Ozzy");
        // make player look at first room.
        current.command("oz", "LOOK");
        Scanner input = new Scanner(System.in);
        String cmd = input.nextLine();
        while (!"EXIT".equals(cmd.toUpperCase())) {
            current.command("oz", cmd);
            cmd = input.nextLine();
        }

        current.removeUserFromRoom("oz");
    }

}
