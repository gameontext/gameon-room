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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import net.wasdev.gameon.room.common.Exit;
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
	protected static final String name = "Boring";
	protected static final String ENV_ROOM_SVC = "service.room";
	protected static final String description = "You are in the worlds most boring room. There is nothing to do here. There is an exit to the North";
	private String endPoint = null;
	protected final Room room;
	private ConcurrentMap<String, String> players = new ConcurrentHashMap<String, String>();	//players currently in this room
	
	public BoringRoom() {
		room = new Room(name);
		getConfig();
		room.setAttribute("endPoint", endPoint + "/ws");
		Exit exit = new Exit("N", "NotWiredUpExit", "A very plain looking door");
		exit.setState(Exit.State.open);
		room.addExit(exit);
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
	
	protected JsonObjectBuilder toJSON() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		response.add(Constants.TYPE, "location");
		response.add(Constants.NAME, name);
		response.add(Constants.DESCRIPTION, description);
		JsonObjectBuilder exits = Json.createObjectBuilder();
		for(Exit exit : room.getExits()) {
			JsonObjectBuilder jsexit = Json.createObjectBuilder();
			jsexit.add(Constants.STATE, exit.getState().name());
			jsexit.add(Constants.DESCRIPTION, exit.getState().name());
			exits.add(exit.getName(), jsexit);
		}
		response.add(Constants.EXITS, exits);
		return response;
	}

	public List<Exit> getExits() {
		return room.getExits();
	}
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * Add a new player to this room
	 * 
	 * @return true if this is a new player, false if this is a reconnect
	 */
	public boolean addPlayer(String id, String name) {
		return players.putIfAbsent(id, name) == null;
	}
	
	public void removePlayer(String id) {
		players.remove(id);
	}
}
