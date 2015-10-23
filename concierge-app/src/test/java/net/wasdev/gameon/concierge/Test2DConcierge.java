package net.wasdev.gameon.concierge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import net.wasdev.gameon.room.common.EndpointCollection;
import net.wasdev.gameon.room.common.RegistrationResponse;
import net.wasdev.gameon.room.common.Room;

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
public class Test2DConcierge {
	
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
		eastRoom.setAttribute("endPoint", "ws://secondRoom");
		RegistrationResponse response = c.registerRoom(eastRoom);
		assertNotNull("Registering the east room should return a valid UUID", response.getRoomUUID());
		
		EndpointCollection endpoints = c.exitRoom("East Room", "East");
		assertEquals("The starting room should connect to the northern room from the starting room's north connection", "East Room", endpoints.getEndpoints().get(0).getUrl());
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
		northRoom.setAttribute("endPoint", "ws://northRoom");
		c.registerRoom(northRoom);
		EndpointCollection connectedRooms = c.exitRoom(startingRoom.getRoomName(), "North");
		assertEquals("The concierge should be able to find the north room", "ws://northRoom", connectedRooms.getEndpoints().get(0).getUrl());
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
		northRoomRegistered.setAttribute("endPoint", "ws://northroom");
		RegistrationResponse northRoomRegistration = c.registerRoom(northRoomRegistered);
		assertNotNull("The registration of the North room should return a UUID", northRoomRegistration.getRoomUUID());
		Room eastRoomRegistered = new Room("East Room");
		northRoomRegistered.setAttribute("endPoint", "ws://eastroom");
		RegistrationResponse eastRoomRegistration = c.registerRoom(eastRoomRegistered);
		assertNotNull("The registeration of the East room should return a UUID", eastRoomRegistration.getRoomUUID());
		
		EndpointCollection northRoomEndpoint = c.exitRoom(c.getStartingRoom().getRoomName(), "North");
		assertEquals("The concierge should be able to find the north room", "ws://northroom", northRoomEndpoint.getEndpoints().get(0).getUrl());
		EndpointCollection eastRoomEndpoints = c.exitRoom(northRoomRegistered.getRoomName(), "East");
		assertEquals("The north room should connect to the east room on its east connection", "ws://eastroom", eastRoomEndpoints.getEndpoints().get(0).getUrl());
		EndpointCollection northRoomEndpointAgain = c.exitRoom(eastRoomRegistered.getRoomName(), "West");
		assertEquals("The east room should connect to the north room on its west connection", "ws://northroom", northRoomEndpointAgain.getEndpoints().get(0).getUrl());
	}
	
	
	@Test
	public void attemptToMoveThroughBlockedDoor() {
		Concierge c = addEasyStartingRoom();
		EndpointCollection noRoom = c.exitRoom(c.getStartingRoom().getRoomName(), "South");
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
