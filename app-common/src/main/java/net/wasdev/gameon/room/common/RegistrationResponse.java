package net.wasdev.gameon.room.common;

import java.util.UUID;

public class RegistrationResponse {

	private UUID roomUUID;

	public RegistrationResponse(UUID roomUUID) {
		this.setRoomUUID(roomUUID);
	}

	public UUID getRoomUUID() {
		return roomUUID;
	}

	public void setRoomUUID(UUID roomUUID) {
		this.roomUUID = roomUUID;
	}

	

}
