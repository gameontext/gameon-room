package net.wasdev.gameon.room.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ExitDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.meta.RoomDesc;

public class Room {		
	RoomDesc roomDesc;
	
	Map<String,User> userMap = new ConcurrentHashMap<String,User>();
	
	Map<String,RoomCommand> commandMap = new HashMap<String,RoomCommand>();
	
	public interface RoomResponseProcessor {
		//"Player message :: from("+senderId+") onlyForSelf("+String.valueOf(selfMessage)+") others("+String.valueOf(othersMessage)+")"
		public void playerEvent(String senderId, String selfMessage, String othersMessage);
		//"Message sent to everyone :: "+s
		public void roomEvent(String s);
		
		public void locationEvent(String senderId, String roomName, String roomDescription, Map<String,String> exits, List<String>objects, List<String>inventory);
	}
	
	public static class DebugResponseProcessor implements Room.RoomResponseProcessor {
		public void playerEvent(String senderId, String selfMessage, String othersMessage){
			System.out.println("Player message :: from("+senderId+") onlyForSelf("+String.valueOf(selfMessage)+") others("+String.valueOf(othersMessage)+")");
		}
		public void roomEvent(String s){
			System.out.println("Message sent to everyone :: "+s);
		}
		public void locationEvent(String senderId, String roomName, String roomDescription, Map<String,String> exits, List<String>objects, List<String>inventory){
			System.out.println("Location: "+roomName+" (For "+senderId+") "+roomDescription);
			if(!objects.isEmpty()){
				System.out.println("You can see the following items: "+objects);
			}
			if(!inventory.isEmpty()){
				System.out.println("You are carrying "+inventory);
			}
		}
	}
	
	static Room.RoomResponseProcessor rrp = new DebugResponseProcessor();
	
	public Room(RoomDesc r, List<RoomCommand> globalCommands){
		roomDesc = r;
		for(RoomCommand c: globalCommands){
			commandMap.put(c.getVerb(), c);
		}
	}
	public void locationEvent(String senderId, Room room, String roomDescription, Collection<ExitDesc> exits, List<String>objects, List<String>inventory){
		Map<String,String> exitMap = new HashMap<String,String>();
		for(ExitDesc e : exits){
			if(e.handler.isVisible()){
				exitMap.put(e.direction.toString(), e.handler.getDescription(senderId, room));
			}
		}		
		rrp.locationEvent(senderId, room.getRoomName(), roomDescription, exitMap, objects, inventory);
	}
	public void playerEvent(String senderId, String selfMessage, String othersMessage){
		rrp.playerEvent(senderId,selfMessage,othersMessage);
	}
	public void roomEvent(String s){
		rrp.roomEvent(s);
	}
	public void setRoomResponseProcessor(Room.RoomResponseProcessor rrp){
		Room.rrp = rrp;
	}
	
	public void addUserToRoom(String id, String username){
		User u = new User(id,username);
		if(!userMap.containsKey(id)){
			userMap.put(id, u);
			this.roomEvent(u.username+" enters the room.");
		}
	}
	public void removeUserFromRoom(String id){
		if(userMap.containsKey(id)){
			User u = userMap.get(id);
			//drop all items in the users inventory when they leave.
			Iterator<ItemDesc> itemIter = u.inventory.iterator();
			while(itemIter.hasNext()){
				ItemDesc item = itemIter.next();
				//add to the room
				this.roomDesc.items.add(item);
				//remove from the user.
				itemIter.remove();					
				this.playerEvent(id, "You drop the "+item.name, u.username+" drops the "+item.name);
			}
			userMap.remove(id);
			this.roomEvent(u.username+" leaves the room.");
		}else{
			System.out.println("Unable to remove "+id+" from room "+roomDesc.id+" because user is not known to the room");
		}
	}		
	
	private static String getFirstWordFromCommand(String cmd){
		Scanner s = new Scanner(cmd);
		if(s.hasNext()){
			return s.next().trim().toUpperCase();
		}else{
			return null;
		}			
	}	
	public void command(String userid, String cmd){
		String command = getFirstWordFromCommand(cmd);
		if(command!=null){
			RoomCommand c = commandMap.get(command);
			if(c!=null){
				c.process(userid, cmd, this);
			}else{
				playerEvent(userid,"\"I'm sorry dave, I don't know how to do that.\"",null);
			}
		}else{
			playerEvent(userid,"You feel a disturbance in the force.",null);
		}
	}
	
	public String getRoomId(){
		return roomDesc.id;
	}
	public String getRoomName(){
		return roomDesc.name;
	}
	public String getRoomDescription(){
		return roomDesc.description;
	}
	public boolean isStarterLocation(){
		return roomDesc.isStarterLocation;
	}
	public User getUserById(String id){
		return userMap.get(id);
	}
	public Collection<User> getAllUsersInRoom(){
		return userMap.values();
	}
	
	public Collection<ItemDesc> getItems(){
		return roomDesc.items;
	}
	
	public Collection<ExitDesc> getExits(){
		return roomDesc.exits;
	}
	
	public void resetRoom(){
		for(User u : userMap.values()){
			u.inventory.clear();
		}
		roomDesc.items.clear();
		roomDesc.items.addAll(roomDesc.defaultItems);
		for(ItemDesc item : roomDesc.items){
			if(item instanceof ContainerDesc){
				ContainerDesc box = (ContainerDesc)item;
				box.items.clear();
				box.items.addAll(box.defaultItems);
			}
		}
	}
	
	public Collection<RoomCommand> getCommands(){
		return commandMap.values();
	}
}