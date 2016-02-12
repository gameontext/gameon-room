/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.gameon.room;

public interface Constants {
    // A field enum or just free-form?
    String USERNAME = "username";
    String USERID = "userId";
    String BOOKMARK = "bookmark";
    String CONTENT = "content";
    String LOCATION = "location";
    String TYPE = "type";
    String NAME = "name";
    String DESCRIPTION = "description";
    String EXITS = "exits";
    String EXITID = "exitId";
    String STATE = "state";
    //env var names
    String ENV_ROOM_SVC = "service_room";
    String ENV_MAP_SVC = "service_map";
    //gameon-system id.
    String GAMEON_ID = "game-on.org";
}
