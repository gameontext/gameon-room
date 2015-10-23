package net.wasdev.gameon.concierge;

import static org.junit.Assert.*;

import net.wasdev.gameon.room.common.Exit;
import net.wasdev.gameon.room.common.RegistrationResponse;
import net.wasdev.gameon.room.common.Room;
import net.wasdev.gameon.room.common.RoomToEndpoints;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

/**
 * The dynamic strategy will make it harder to draw out a map of the rooms because there is some
 * arbitrariness.
 */
public class TestManualWiringConcierge {
	
	@Test
	public void registerARoom() {
		// We want the room itself to come up and publish to the concierge. So the flow will require the room to say "Here I am concierge"
		Concierge c = addEasyStartingRoom();
		assertEquals("The start room should be the first room we add", "Starting Room", c.getStartingRoom().getRoomId());
	}

	@Test
	public void placeTwoRoomsNearEachother() {
		// We want the room itself to come up and publish to the concierge. So the flow will require the room to say "Here I am concierge"
		Concierge c = new Concierge(new ManualWiringPlacement());
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
		exits.add(new Exit("North", "Second Room", "Door from the starting room to the second room"));
		anEasyRoom.setExits(exits);
		RegistrationResponse startingRoomUUID = c.registerRoom(anEasyRoom);
		assertNotNull("The registration should return a non-null object", startingRoomUUID);
		
		Room secondRoomAsRegistered = new Room("Second Room");
		RegistrationResponse secondRoomUUID = c.registerRoom(secondRoomAsRegistered);
		assertNotNull("The second room registration should return a non-null object", secondRoomUUID);
 		RoomToEndpoints secondRoomAsFound = c.exitRoom(anEasyRoom.getRoomName(), "North");
		assertEquals("The secondRoom should be returned", secondRoomAsRegistered.getRoomName(), secondRoomAsFound.getRoomId());
	}
	
	

	@Test
	public void MoveToNewRoomAndBack() {
		// We want the room itself to come up and publish to the concierge. So the flow will require the room to say "Here I am concierge"
		Concierge c = new Concierge(new ManualWiringPlacement());
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
		exits.add(new Exit("North", "Second Room", "Door from the starting room to the second room"));
		anEasyRoom.setExits(exits);
		RegistrationResponse startingRoomUUID = c.registerRoom(anEasyRoom);
		
		Room secondRoomAsRegistered = new Room("Second Room");
		List<Exit> secondRoomExits = new ArrayList<Exit>();
		secondRoomExits.add(new Exit("South", "Starting Room", "A door between the second room back to the starting room"));
		secondRoomAsRegistered.setExits(secondRoomExits);
		RegistrationResponse secondRoomUUID = c.registerRoom(secondRoomAsRegistered);
 		RoomToEndpoints secondRoomAsFound = c.exitRoom(anEasyRoom.getRoomName(), "North");
		assertEquals("The secondRoom should be returned", "Second Room", secondRoomAsFound.getRoomId());
		
 		RoomToEndpoints startingRoomAsFound = c.exitRoom(secondRoomAsRegistered.getRoomName(), "South");
		assertEquals("The secondRoom should be returned", "Starting Room", startingRoomAsFound.getRoomId());
	}
	
	@Test
	public void reregisterSameRoom() {
		Concierge c = new Concierge(new ManualWiringPlacement());
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
		exits.add(new Exit("North", "Second Room", "A door from the starting room to the Second room"));
		anEasyRoom.setExits(exits);
		RegistrationResponse startingRoomUUID = c.registerRoom(anEasyRoom);
		
		Room sameRoomAgain = new Room("Starting Room");
     	RegistrationResponse sameRoomAgainUUID = c.registerRoom(sameRoomAgain);

     	RoomToEndpoints startingRoomEndpoints = c.getStartingRoom();
     	assertEquals("The starting room should contain two endpoints", 2, startingRoomEndpoints.getEndpoints().size());
		
		
	}
	
	
	
	private Concierge addEasyStartingRoom() {
		Concierge c = new Concierge();
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
//		exits.add(new Exit("North", "Second Room"));
		anEasyRoom.setExits(exits);
		c.registerRoom(anEasyRoom);
		return c;
	}
	
}
