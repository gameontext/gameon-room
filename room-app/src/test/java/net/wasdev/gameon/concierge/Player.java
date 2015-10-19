package net.wasdev.gameon.concierge;

public class Player {
	
	private String playerID;
	
	
	public Player(String playerID) {
		this.setPlayerID(playerID);
	}


	public String getPlayerID() {
		return playerID;
	}


	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}
	

}
