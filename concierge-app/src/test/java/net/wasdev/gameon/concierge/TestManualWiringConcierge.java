package net.wasdev.gameon.concierge;

import static org.junit.Assert.*;

import net.wasdev.gameon.room.common.Exit;
import net.wasdev.gameon.room.common.Room;

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
		assertEquals("The start room should be the first room we add", "Starting Room", c.getStartingRoom().getRoomName());
	}

	@Test
	public void placeTwoRoomsNearEachother() {
		// We want the room itself to come up and publish to the concierge. So the flow will require the room to say "Here I am concierge"
		Concierge c = new Concierge(new ManualWiringPlacement());
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
		exits.add(new Exit("North", "Second Room"));
		anEasyRoom.setExits(exits);
		UUID startingRoomUUID = c.registerRoom(anEasyRoom);
		
		Room secondRoomAsRegistered = new Room("Second Room");
		UUID secondRoomUUID = c.registerRoom(secondRoomAsRegistered);
 		Room secondRoomAsFound = c.exitRoom(startingRoomUUID, "North");
		assertEquals("The secondRoom should be returned", secondRoomUUID, secondRoomAsFound.getAssignedID());
	}
	
	

	@Test
	public void MoveToNewRoomAndBack() {
		// We want the room itself to come up and publish to the concierge. So the flow will require the room to say "Here I am concierge"
		Concierge c = new Concierge(new ManualWiringPlacement());
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
		exits.add(new Exit("North", "Second Room"));
		anEasyRoom.setExits(exits);
		UUID startingRoomUUID = c.registerRoom(anEasyRoom);
		
		Room secondRoomAsRegistered = new Room("Second Room");
		List<Exit> secondRoomExits = new ArrayList<Exit>();
		secondRoomExits.add(new Exit("South", "Starting Room"));
		secondRoomAsRegistered.setExits(secondRoomExits);
		UUID secondRoomUUID = c.registerRoom(secondRoomAsRegistered);
 		Room secondRoomAsFound = c.exitRoom(startingRoomUUID, "North");
		assertEquals("The secondRoom should be returned", secondRoomUUID, secondRoomAsFound.getAssignedID());
		
 		Room startingRoomAsFound = c.exitRoom(secondRoomUUID, "South");
		assertEquals("The secondRoom should be returned", startingRoomUUID, startingRoomAsFound.getAssignedID());
	}
	
	
	private Concierge addEasyStartingRoom() {
		Concierge c = new Concierge();
		Room anEasyRoom = new Room("Starting Room");
		List<Exit> exits = new ArrayList<Exit>();
		exits.add(new Exit("North", "Second Room"));
		anEasyRoom.setExits(exits);
		c.registerRoom(anEasyRoom);
		return c;
	}
	
}
