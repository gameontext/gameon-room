package net.wasdev.gameon.room;

import net.wasdev.gameon.room.common.Room;

/*
 * Copyright 2015 IBM Corp.
 */

public interface RoomProvider {
	Room getRoom() throws Exception;
}
