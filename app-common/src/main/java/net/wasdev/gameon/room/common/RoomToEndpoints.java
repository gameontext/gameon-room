package net.wasdev.gameon.room.common;

import java.util.ArrayList;
import java.util.List;

public class RoomToEndpoints {

	private String roomId;
	private List<String> endpoints = new ArrayList<String>();

	public List<String> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<String> endpoints) {
		this.endpoints = endpoints;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
}
