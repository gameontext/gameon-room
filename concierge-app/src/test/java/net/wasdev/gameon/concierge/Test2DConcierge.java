package net.wasdev.gameon.concierge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import net.wasdev.gameon.room.common.RoomToEndpoints;
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
		Concierge c = new Concierge(new Simple2DPlacement());
		Room startingRoom = new Room("Starting Room");
		c.registerRoom(startingRoom);
	}
	
	@Test
	public void getStartingRoom() {
		Concierge c = addEasyStartingRoom();
		RoomToEndpoints theSameRoom = c.getStartingRoom();
		assertEquals("The concierge should have a starting room called 'Starting Room'", "Starting Room", theSameRoom.getRoomId());
	}
	
	@Test
	public void goFromStartRoomToEastRoom() {
		Concierge c = addEasyStartingRoom();
		RoomToEndpoints startingRoom = c.getStartingRoom();
		assertEquals("The first room should be called the starting room", "Starting Room", startingRoom.getRoomId());
		Room eastRoom = new Room("East Room");
		eastRoom.setAttribute("startLocation", "false");
		eastRoom.setAttribute("endPoint", "ws://secondRoom");
		RegistrationResponse response = c.registerRoom(eastRoom);
		assertNotNull("Registering the east room should return a valid UUID", response);
		
		RoomToEndpoints endpoints = c.exitRoom("Starting Room", "East");
		assertEquals("The starting room should connect to the northern room from the starting room's north connection", "East Room", endpoints.getRoomId());
	}
	
	@Test
	public void goFromNorthRoomToEastRoom() {
		Concierge c = addEasyStartingRoom();
		
		RoomToEndpoints startingRoom = c.getStartingRoom();
		// Add 9 random rooms
		for(int i =2; i <= 10; i++) {
			Room room = new Room ("Room " + i);
			c.registerRoom(room);
		}
		Room northRoom = new Room("North Room");
		northRoom.setAttribute("endPoint", "ws://northRoom");
		c.registerRoom(northRoom);
		RoomToEndpoints connectedRooms = c.exitRoom(startingRoom.getRoomId(), "North");
		assertEquals("The concierge should be able to find the north room", "ws://northRoom", connectedRooms.getEndpoints().get(0));
	}
	
	@Test
	public void goFromNorthRoomToEastRoomAndBackAgain() {
		Concierge c = addEasyStartingRoom();
		
		// Add 9 random rooms
		for(int i =2; i <= 10; i++) {
			Room room = new Room ("Room " + i);
			room.setAttribute("startLocation", "false");
			c.registerRoom(room);
		}
		Room northRoomRegistered = new Room("North Room");
		northRoomRegistered.setAttribute("startLocation", "false");
		northRoomRegistered.setAttribute("endPoint", "ws://northroom");
		RegistrationResponse northRoomRegistration = c.registerRoom(northRoomRegistered);
		assertNotNull("The registration of the North room should return a UUID", northRoomRegistration);
		
		Room eastRoomRegistered = new Room("East Room");
		eastRoomRegistered.setAttribute("startLocation", "false");
		eastRoomRegistered.setAttribute("endPoint", "ws://eastroom");
		RegistrationResponse eastRoomRegistration = c.registerRoom(eastRoomRegistered);
		assertNotNull("The registeration of the East room should return a UUID", eastRoomRegistration);
		
		RoomToEndpoints northRoomEndpoint = c.exitRoom(c.getStartingRoom().getRoomId(), "North");
		assertEquals("The concierge should be able to find the north room", "ws://northroom", northRoomEndpoint.getEndpoints().get(0));
		RoomToEndpoints eastRoomEndpoints = c.exitRoom(northRoomRegistered.getRoomName(), "East");
		assertEquals("The north room should connect to the east room on its east connection", "ws://eastroom", eastRoomEndpoints.getEndpoints().get(0));
		RoomToEndpoints northRoomEndpointAgain = c.exitRoom(eastRoomRegistered.getRoomName(), "West");
		assertEquals("The east room should connect to the north room on its west connection", "ws://northroom", northRoomEndpointAgain.getEndpoints().get(0));
	}
	
	
	@Test
	public void attemptToMoveThroughBlockedDoor() {
		Concierge c = addEasyStartingRoom();
		RoomToEndpoints noRoom = c.exitRoom(c.getStartingRoom().getRoomId(), "South");
		assertNull("The concierge should return a null object", noRoom);
	}

	private Concierge addEasyStartingRoom() {
		Concierge c = new Concierge(new Simple2DPlacement());
		Room anEasyRoom = new Room("Starting Room");
		c.registerRoom(anEasyRoom);
		return c;
	}
	
}
