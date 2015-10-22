package net.wasdev.gameon.room;

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

	@Override
	public Room getRoom() {
		return new Room(name);
	}
}
