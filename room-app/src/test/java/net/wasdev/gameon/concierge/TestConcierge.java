package net.wasdev.gameon.concierge;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestConcierge {

	@Test
	public void getStartingRoom() {
		Concierge c = new Concierge();
		Room startingRoom = c.getStartingRoom();
		assertEquals("The first room should be called the starting room", "Starting Room", startingRoom.getRoomName());
		
	}
	
	@Test
	public void goFromStartRoomToNorthRoom() {
		Concierge c = new Concierge();
		Room startingRoom = c.getStartingRoom();
		assertEquals("The first room should be called the starting room", "Starting Room", startingRoom.getRoomName());
		
		Room northRoom = c.exitRoom(startingRoom, "North");
		assertEquals("The starting room should connect to the northern room from the starting room's north connection", "North Room", northRoom.getRoomName());
		
	}
	
	@Test
	public void goFromNorthRoomToEastRoom() {
		Concierge c = new Concierge();
		
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
		Concierge c = new Concierge();
		Room northRoom = c.exitRoom(c.getStartingRoom(), "North");
		assertEquals("The concierge should be able to find the north room", "North Room", northRoom.getRoomName());
		Room eastRoom = c.exitRoom(northRoom, "East");
		assertEquals("The north room should connect to the east room on its east connection", "East Room", eastRoom.getRoomName());
		Room northRoomAgain = c.exitRoom(eastRoom, "West");
		assertEquals("The east room should connect to the north room on its west connection", "North Room", northRoomAgain.getRoomName());
	}
	
	
	@Test
	public void attemptToMoveThroughBlockedDoor() {
		Concierge c = new Concierge();
		Room noRoom = c.exitRoom(c.getStartingRoom(), "South");
		assertNull("The concierge should return a null object", noRoom);
	}
	
	
}
