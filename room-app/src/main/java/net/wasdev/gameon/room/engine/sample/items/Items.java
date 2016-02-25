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
package net.wasdev.gameon.room.engine.sample.items;

import net.wasdev.gameon.room.engine.meta.ItemDesc;

/**
 * A simple class that gives the Items a way to refer to each other.. 
 */
public class Items {
    public static final ItemDesc mug = new Mug();
    public static final ItemDesc coffeeMachine = new CoffeeMachine();
    public static final ItemDesc stilettoHeels = new Stilettos();
    public static final ItemDesc jukebox = new JukeBox();
    public static final ItemDesc fuse = new Fuse();
    public static final ItemDesc cupboard = new Cupboard();
    public static final ItemDesc mugRoomSign = new MugRoomSign();
}
