package net.wasdev.gameon.room.engine.meta;

import net.wasdev.gameon.room.engine.Room;

public class ExitDesc {
	
	public enum Direction {
		NORTH("N"),
		SOUTH("S"),
		EAST("E"),
		WEST("W"),
		UP("U"),
		DOWN("D");
		private final String shortName;
		Direction(String shortName){
			this.shortName=shortName;
		}
		public String toString(){
			return shortName;
		}
	};
	
	public interface ExitHandler{
		public String getDescription(String execBy, Room exitOwner);
		public boolean isVisible();
		public boolean isTraversable();		
	}
	
	public final Direction direction;
	public final String targetRoomId;
	public final ExitHandler handler;
	
	public ExitDesc(String targetRoomId, Direction dir, String description){
		this(targetRoomId, dir,description,true);
	}
	
	public ExitDesc(String targetRoomId, Direction dir, final String description, final boolean visible){
		//build a default exit handler that has 
		this(targetRoomId, dir, new ExitHandler(){
			@Override
			public String getDescription(String execBy, Room exitOwner) {
				return description;
			}
			@Override
			public boolean isVisible() {
				return visible;
			}
			@Override
			public boolean isTraversable() {
				return true;
			}			
		});
	}
	
	public ExitDesc(String targetRoomId, Direction dir, ExitHandler handler){	
		this.direction = dir;
		this.handler = handler;
		this.targetRoomId = targetRoomId;
	}
	
}
