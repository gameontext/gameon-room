package net.wasdev.gameon.room;

import javax.servlet.ServletException;

import net.wasdev.gameon.room.common.Exit;
import net.wasdev.gameon.room.common.Room;

/*
 * Copyright 2015 IBM Corp.
 */

/*
 * This is a really boring room that doesn't do anything but let people
 * exit it.
 */
public class BoringRoom implements RoomProvider {
	private static final String name = "Boring";
	private static final String ENV_ROOM_SVC = "service.room";
	private String endPoint = null;

	@Override
	public Room getRoom() throws Exception {
		Room room = new Room(name);
		getConfig();
		room.setAttribute("endPoint", endPoint);
		room.addExit(new Exit("N", "NotWiredUpExit"));
		return room;
	}
	
	private void getConfig() throws ServletException {
		endPoint = System.getProperty(ENV_ROOM_SVC, System.getenv(ENV_ROOM_SVC));
		if(endPoint == null) {
			throw new ServletException("The location for the concierge service cold not be "
					+ "found in a system property or environment variable named : " + ENV_ROOM_SVC);
		}
	}
}
