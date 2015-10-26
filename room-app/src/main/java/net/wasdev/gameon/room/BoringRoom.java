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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;

import net.wasdev.gameon.room.common.Room;

/*
 * Copyright 2015 IBM Corp.
 */

/*
 * This is a really boring room that doesn't do anything but let people
 * exit it.
 */
@ApplicationScoped
public class BoringRoom implements RoomProvider {
	protected static final String ENV_ROOM_SVC = "service_room";
	private String endPoint = null;
	protected final Room room;
	private ConcurrentMap<String, String> players = new ConcurrentHashMap<String, String>();	//players currently in this room
	
	Engine e = Engine.getEngine();
	Engine.Room r;
	
	public BoringRoom() {
		
		//get the 'first' room from the engine.
		r = e.getRooms().iterator().next();
				
		room = new Room(r.getRoomId());
		getConfig();
		room.setAttribute("endPoint", endPoint + "/ws");
		
		//TODO: how to wire up any other rooms from engine?
	}

	public Room getRoom() throws Exception {
		return room;
	}
	
	private void getConfig() {
		endPoint = System.getProperty(ENV_ROOM_SVC, System.getenv(ENV_ROOM_SVC));
		if(endPoint == null) {
			throw new RuntimeException("The location for the concierge service cold not be "
					+ "found in a system property or environment variable named : " + ENV_ROOM_SVC);
		}
	}
	
	public String getDescription() {
		return r.getRoomDescription();
	}
	
	/**
	 * Add a new player to this room
	 * 
	 * @return true if this is a new player, false if this is a reconnect
	 */
	public boolean addPlayer(String id, String name) {
		r.addUserToRoom(id,name);
		return players.putIfAbsent(id, name) == null;
	}
	
	public void removePlayer(String id) {
		r.removeUserFromRoom(id);
		players.remove(id);
	}
}
