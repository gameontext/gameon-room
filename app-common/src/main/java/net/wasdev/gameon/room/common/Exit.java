package net.wasdev.gameon.room.common;

public class Exit {
	private String name;
	private String room;

	public Exit() {
		//no-args constructor for JSON serialisation
	}
	
	public Exit(String exitName, String endRoom) {
		name = exitName;
		room = endRoom;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	@Override
	public String toString() {
		return "Exit : " + name + " -> " + room;
	}
	
	

}
