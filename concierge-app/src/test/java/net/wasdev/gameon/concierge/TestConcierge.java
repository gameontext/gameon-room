package net.wasdev.gameon.concierge;

import static org.junit.Assert.*;
import net.wasdev.gameon.room.common.Room;

import org.junit.Test;

public class TestConcierge {
	
	@Test
	public void registerARoom() {
		// We want the room itself to come up and publish to the concierge. So the flow will require the room to say "Here I am concierge"
		Concierge c = new Concierge();
		Room startingRoom = new Room("Starting Room");
		c.registerRoom(startingRoom);
	}
	
	@Test
	public void getStartingRoom() {
		Concierge c = addEasyStartingRoom();
		Room theSameRoom = c.getStartingRoom();
		assertEquals("The concierge should have a starting room called 'Starting Room'", "Starting Room", theSameRoom.getRoomName());
	}
	
	@Test
	public void goFromStartRoomToNorthRoom() {
		Concierge c = addEasyStartingRoom();
		Room startingRoom = c.getStartingRoom();
		assertEquals("The first room should be called the starting room", "Starting Room", startingRoom.getRoomName());
		
		Room northRoom = c.exitRoom(startingRoom, "North");
		assertEquals("The starting room should connect to the northern room from the starting room's north connection", "North Room", northRoom.getRoomName());
	}
	
	@Test
	public void goFromNorthRoomToEastRoom() {
		Concierge c = addEasyStartingRoom();
		
		Room startingRoom = c.getStartingRoom();
		assertEquals("The first room should be called the starting room", "Starting Room", startingRoom.getRoomName());

		Room northRoom = c.exitRoom(startingRoom, "North");
		assertEquals("The concierge should be able to find the north room", "North Room", northRoom.getRoomName());
		Room eastRoom = c.exitRoom(northRoom, "East");
		assertEquals("The north room should connect to the east room on its east connection", "East Room", eastRoom.getRoomName());
	}
	
	
	/**
	 * These rooms should in theory look like this:
	 * 
	 *   #------------------------#     #------------------------#
	 *   |          North         |     |          North         |
	 *   |          Exit          |     |          Exit          |
	 *   |                        |     |                        |
	 *   | West  North Room  East | <-> | West  East Room   East |
	 *   | Exit              Exit |     | Exit              Exit |
	 *   |                        |     |                        |
	 *   |          South         |     |          South         |
	 *   |          Exit          |     |          Exit          |
	 *   #------------------------#     #------------------------#
	 */
	@Test
	public void goFromNorthRoomToEastRoomAndBackAgain() {
		Concierge c = addEasyStartingRoom();
		Room northRoom = c.exitRoom(c.getStartingRoom(), "North");
		assertEquals("The concierge should be able to find the north room", "North Room", northRoom.getRoomName());
		Room eastRoom = c.exitRoom(northRoom, "East");
		assertEquals("The north room should connect to the east room on its east connection", "East Room", eastRoom.getRoomName());
		Room northRoomAgain = c.exitRoom(eastRoom, "West");
		assertEquals("The east room should connect to the north room on its west connection", "North Room", northRoomAgain.getRoomName());
	}
	
	
	@Test
	public void attemptToMoveThroughBlockedDoor() {
		Concierge c = addEasyStartingRoom();
		Room noRoom = c.exitRoom(c.getStartingRoom(), "South");
		assertNull("The concierge should return a null object", noRoom);
	}

	private Concierge addEasyStartingRoom() {
		Concierge c = new Concierge();
		Room anEasyRoom = new Room("Starting Room");
		anEasyRoom.setAttribute("difficulty", "easy");
		c.registerRoom(anEasyRoom);
		return c;
	}
	
}
