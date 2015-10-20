package net.wasdev.gameon.concierge;

import static org.junit.Assert.*;
import net.wasdev.gameon.room.common.Room;

import java.util.UUID;

import org.junit.Test;

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
 *               ^
 *               |
 *               v
 *   #------------------------#
 *   |          North         |
 *   |          Exit          |
 *   |                        |
 *   | West   Starting   East |
 *   | Exit     Room     Exit |
 *   |                        |
 *   |          South         |
 *   |          Exit          |
 *   #------------------------#
 *   
 *   In some cases I use a simpler model:
 *   
 *   (Starting Room) <-> (East Room) <-> (Far East Room)
 */
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
	public void goFromStartRoomToEastRoom() {
		Concierge c = addEasyStartingRoom();
		Room startingRoom = c.getStartingRoom();
		assertEquals("The first room should be called the starting room", "Starting Room", startingRoom.getRoomName());
		Room eastRoom = new Room("East Room");
		UUID assignedEastRoomID = c.registerRoom(eastRoom);
		assertNotNull("Registering the east room should return a valid UUID", assignedEastRoomID);
		
		Room northRoom = c.exitRoom(startingRoom.getAssignedID(), "East");
		assertEquals("The starting room should connect to the northern room from the starting room's north connection", "East Room", northRoom.getRoomName());
	}
	
	@Test
	public void goFromNorthRoomToEastRoom() {
		Concierge c = addEasyStartingRoom();
		
		Room startingRoom = c.getStartingRoom();
		// Add 9 random rooms
		for(int i =2; i <= 10; i++) {
			Room room = new Room ("Room " + i);
			c.registerRoom(room);
		}
		Room northRoom = new Room("North Room");
		UUID northRoomUUID = c.registerRoom(northRoom);
		Room foundNorthRoom = c.exitRoom(startingRoom.getAssignedID(), "North");
		assertEquals("The concierge should be able to find the north room", "North Room", foundNorthRoom.getRoomName());
		assertEquals("The concierge should be able to find the north room", northRoomUUID, foundNorthRoom.getAssignedID());
	}
	
	@Test
	public void goFromNorthRoomToEastRoomAndBackAgain() {
		Concierge c = addEasyStartingRoom();
		
		// Add 9 random rooms
		for(int i =2; i <= 10; i++) {
			Room room = new Room ("Room " + i);
			c.registerRoom(room);
		}
		Room northRoomRegistered = new Room("North Room");
		UUID northRoomRegisteredUUID = c.registerRoom(northRoomRegistered);
		assertNotNull("The registeration fo the North room shoudl return a UUID", northRoomRegisteredUUID);
		Room eastRoomRegistered = new Room("East Room");
		UUID eastRoomRegisteredUUID = c.registerRoom(eastRoomRegistered);
		assertNotNull("The registeration of the East room should return a UUID", eastRoomRegisteredUUID);
		
		Room foundNorthRoom = c.exitRoom(c.getStartingRoom().getAssignedID(), "North");
		assertEquals("The concierge should be able to find the north room", "North Room", foundNorthRoom.getRoomName());
		Room eastRoom = c.exitRoom(northRoomRegistered.getAssignedID(), "East");
		assertEquals("The north room should connect to the east room on its east connection", "East Room", eastRoom.getRoomName());
		Room northRoomAgain = c.exitRoom(eastRoom.getAssignedID(), "West");
		assertEquals("The east room should connect to the north room on its west connection", "North Room", northRoomAgain.getRoomName());
	}
	
	
	@Test
	public void attemptToMoveThroughBlockedDoor() {
		Concierge c = addEasyStartingRoom();
		Room noRoom = c.exitRoom(c.getStartingRoom().getAssignedID(), "South");
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
