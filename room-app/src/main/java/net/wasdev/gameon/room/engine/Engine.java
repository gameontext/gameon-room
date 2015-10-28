package net.wasdev.gameon.room.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

import net.wasdev.gameon.room.engine.sample.SampleDataProvider;

public class Engine {

	//eventually we'll let this be customizable.. 
	DataProvider dp = new SampleDataProvider();
	
	public Collection<Room> getRooms(){	
		//wrap it into an unmodifiable to prevent accidents ;p
		return Collections.unmodifiableCollection(dp.getRooms());
	}
	
	private Engine(){		
	}
	
	private static final Engine engine = new Engine();
	
	public static Engine getEngine(){
		return engine;
	}
	
	/**
	 * Console based test rig. 
	 */
	public static void main(String[] args){
		
		Engine e = new Engine();
	
		Collection<Room> rooms = e.getRooms();
		
		Room current = rooms.iterator().next();
				
		//go interactive ;p
		System.out.println("---[[[ OZMONSTA Engine v1.0, EXIT to quit. Room '"+current.getRoomId()+"' ]]]---");		
		current.addUserToRoom("oz", "Ozzy");
		//make player look at first room.
		current.command("oz", "LOOK");
		Scanner input = new Scanner(System.in);
		String cmd = input.nextLine();		
		while(!"EXIT".equals(cmd.toUpperCase())){
			current.command("oz", cmd);
			cmd = input.nextLine();
		}
		
		current.removeUserFromRoom("oz");
	}
	
}
