package net.wasdev.gameon.room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Engine {

	private static class ItemDesc {
		String name;
		String description;
		boolean takeable;	
		boolean clearStateOnDrop;
		String state="";
		
		interface CommandHandler{
			public void processCommand(ItemDesc item, String execBy, String cmd, Room room);
		}
		CommandHandler handler;
		
		public ItemDesc(String name, String description){
			this.name=name; this.description=description;this.takeable=false;this.clearStateOnDrop=true;
		}
		public ItemDesc(String name, String description, boolean takeable, boolean clearStateOnDrop){
			this.name=name; this.description=description;this.takeable=takeable;this.clearStateOnDrop=clearStateOnDrop;
		}
		public ItemDesc(String name, String description, boolean takeable){
			this.name=name; this.description=description;this.takeable=takeable;this.clearStateOnDrop=true;
		}
		public ItemDesc(String name, String description, boolean takeable, boolean clearStateOnDrap, CommandHandler handler){
			this.name=name; this.description=description;this.takeable=takeable;this.handler=handler;this.clearStateOnDrop=clearStateOnDrap;
		}
	}
	
	private static class ContainerDesc extends ItemDesc{
		Collection<ItemDesc> items;	
		Collection<ItemDesc> defaultItems;	
		
		//if you can't access the container, you don't get to know whats in it.
		//it may be out of reach, closed, or locked. 
		interface AccessVerificationHandler{
			public boolean verifyAccess(ItemDesc item, String execBy, Room room);
		}
		AccessVerificationHandler access;
		
		public ContainerDesc(String name, String description, boolean takeable,boolean clearStateOnDrop,ItemDesc[] items,AccessVerificationHandler access){
			super(name,description,takeable,clearStateOnDrop);
			this.access=access;
			this.items=new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items)); 
			this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
		}
		public ContainerDesc(String name, String description, boolean takeable,boolean clearStateOnDrop,ItemDesc[] items,AccessVerificationHandler access, CommandHandler handler){
			super(name,description,takeable,clearStateOnDrop,handler);
			this.access=access;
			this.items=new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items)); 
			this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
		}		
		public ContainerDesc(String name, String description, boolean takeable,boolean clearStateOnDrop,ItemDesc[] items,CommandHandler handler){
			super(name,description,takeable,clearStateOnDrop,handler);
			this.items=new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items)); 
			this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
		}
		public ContainerDesc(String name, String description, boolean takeable,boolean clearStateOnDrop,ItemDesc[] items){
			super(name,description,takeable,clearStateOnDrop);
			this.items=new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items)); 
			this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
		}					
	}
			
	private static class RoomDesc {
		String id;
		String name;
		String description;
		Collection<ItemDesc> items;	
		Collection<ItemDesc> defaultItems;	
		Collection<String> exits;
		Collection<String> defaultExits;
		public RoomDesc(String id, String name, String description, ItemDesc[] items, String[] exits){
			this.id=id; this.name=name; this.description=description; 
			this.items=new CopyOnWriteArraySet<ItemDesc>(Arrays.asList(items)); 
			this.exits=new CopyOnWriteArraySet<String>(Arrays.asList(exits));
			this.defaultItems = Collections.unmodifiableSet(new HashSet<ItemDesc>(this.items));
			this.defaultExits = Collections.unmodifiableSet(new HashSet<String>(this.exits));
		}
	}
		
	private static class User {
		String id;
		String username;
		Collection<ItemDesc> inventory;
		public User(String id, String username){
			this.id=id; this.username=username; this.inventory=new HashSet<ItemDesc>();
		}
	}

	private interface Command {		
		public String getVerb();
		public abstract void process(String execBy, String cmd, Room room);
	}	
	
	private static ItemDesc findItemInInventory(String itemName, User execBy){
		for(ItemDesc item :  execBy.inventory){
			if(item.name.equalsIgnoreCase(itemName)){	
				return item;
			}
		}
		return null;
	}
	private static ItemDesc findItemInRoom(String itemName, Room room){
		for(ItemDesc item : room.roomDesc.items){
			if(item.name.equalsIgnoreCase(itemName)){	
				return item;
			}
		}
		return null;
	}
	private static ItemDesc findItemInRoomOrInventory(User execBy, String itemName, Room room){
		ItemDesc result = findItemInRoom(itemName, room);
		if(result==null)
			result = findItemInInventory(itemName, execBy);
		return result;
	}
	private static String getCommandWithoutVerbAsString(String cmd){
		Scanner s = new Scanner(cmd);
		//skip the verb.. 
		s.next();
		StringBuilder builder = new StringBuilder();
		boolean first=true;
		while(s.hasNext()){
			if(!first)
				builder.append(" ");
			builder.append(s.next());
			first=false;	
		}
		return builder.toString();
	}
	private static String getCommandWithoutVerbAndItemAsString(String cmd, ItemDesc item){
		Scanner s = new Scanner(cmd);
		//skip the verb.. 
		s.next();
		StringBuilder builder = new StringBuilder();
		boolean first=true;
		while(s.hasNext()){
			if(!first)
				builder.append(" ");
			builder.append(s.next().toUpperCase());
			if(builder.toString().equals(item.name.toUpperCase())){
				builder = new StringBuilder();
			}
			first=false;	
		}
		return builder.toString();
	}
	private static String getItemNameFromCommand(String cmd, Room room, User execBy){		
		List<String> allItems = new ArrayList<String>();
		for(ItemDesc item : room.roomDesc.items){
			allItems.add(item.name.trim().toUpperCase());
		}
		for(ItemDesc item :  execBy.inventory){
			allItems.add(item.name.trim().toUpperCase());
		}
		//sort so we process longer item names first =)
		Collections.sort(allItems, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int o1len = o1.length();
				int o2len = o2.length();
				if(o1len>o2len){
					return -1;
				}else if(o1len<o2len){
					return 1;
				}else return o1.compareTo(o2);
			}
		});
		String uCmd = cmd.toUpperCase();
		for(String item: allItems){
			if(uCmd.startsWith(item))
				return item;
		}		
		return null;
	}
	private static String getFirstWordFromCommand(String cmd){
		Scanner s = new Scanner(cmd);
		if(s.hasNext()){
			return s.next().trim().toUpperCase();
		}else{
			return null;
		}			
	}

	
	public static class Room {		
		RoomDesc roomDesc;
		
		Map<String,User> userMap = new ConcurrentHashMap<String,User>();
		
		Map<String,Command> commandMap = new HashMap<String,Command>();
		
		interface RoomResponseProcessor {
			//"Player message :: from("+senderId+") onlyForSelf("+String.valueOf(selfMessage)+") others("+String.valueOf(othersMessage)+")"
			public void playerEvent(String senderId, String selfMessage, String othersMessage);
			//"Message sent to everyone :: "+s
			public void roomEvent(String s);
			
			public void locationEvent(String senderId, String roomName, String roomDescription, Object exits, List<String>objects, List<String>inventory);
		}
		
		public static class sysoutResponseProcessor implements RoomResponseProcessor {
			public void playerEvent(String senderId, String selfMessage, String othersMessage){
				System.out.println("Player message :: from("+senderId+") onlyForSelf("+String.valueOf(selfMessage)+") others("+String.valueOf(othersMessage)+")");
			}
			public void roomEvent(String s){
				System.out.println("Message sent to everyone :: "+s);
			}
			public void locationEvent(String senderId, String roomName, String roomDescription, Object exits, List<String>objects, List<String>inventory){
				System.out.println("Location: "+roomName+" (For "+senderId+") "+roomDescription);
				if(!objects.isEmpty()){
					System.out.println("You can see the following items: "+objects);
				}
				if(!inventory.isEmpty()){
					System.out.println("You are carrying "+inventory);
				}
			}
		}
		
		static RoomResponseProcessor rrp = new sysoutResponseProcessor();
		
		public Room(RoomDesc r, List<Command> globalCommands){
			roomDesc = r;
			for(Command c: globalCommands){
				commandMap.put(c.getVerb(), c);
			}
		}
		public void locationEvent(String senderId, String roomName, String roomDescription, Object exits, List<String>objects, List<String>inventory){
			rrp.locationEvent(senderId, roomName, roomDescription, exits, objects, inventory);
		}
		public void playerEvent(String senderId, String selfMessage, String othersMessage){
			rrp.playerEvent(senderId,selfMessage,othersMessage);
		}
		public void roomEvent(String s){
			rrp.roomEvent(s);
		}
		
		public void setRoomResponseProcessor(RoomResponseProcessor rrp){
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
			
		public void command(String userid, String cmd){
			String command = getFirstWordFromCommand(cmd);
			if(command!=null){
				Command c = commandMap.get(command);
				if(c!=null){
					c.process(userid, cmd, this);
				}else{
					playerEvent(userid,"I'm sorry dave, I don't know how to do that.",null);
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
	}
	
	/***********************************************************************************************
	 * 
	 * Command implementations.
	 * 
	 * 
	 ***********************************************************************************************/
	
	private static class Look implements Command {
		public Look(){
		}
		public String getVerb(){
			return "LOOK";
		}
		public void process(String execBy, String cmd, Room room){
			User u = room.userMap.get(execBy);
			if(u!=null){
				List<String> invItems = new ArrayList<String>();
				List<String> roomItems = new ArrayList<String>();
				for(ItemDesc i : room.roomDesc.items){
					roomItems.add(i.name);
				}
				for(ItemDesc i : u.inventory){
					invItems.add(i.name);
				}
				room.locationEvent(execBy, room.roomDesc.name, room.roomDesc.description, null, roomItems, invItems);
			}
		}
	}
	
	private static class Inventory implements Command {
		public Inventory(){
		}
		public String getVerb(){
			return "INVENTORY";
		}
		public void process(String execBy, String cmd, Room room){
			User u = room.userMap.get(execBy);
			if(u!=null){
				if(u.inventory.isEmpty()){
					room.playerEvent(execBy,"You do not appear to be carrying anything.",null);
				}else{
					StringBuilder sb = new StringBuilder();
					sb.append("You are carrying; ");
					boolean first=true;
					for(ItemDesc item : u.inventory){
						if(!first)sb.append(", ");
						sb.append(item.name);
						first=false;
					}
					room.playerEvent(execBy,sb.toString(),null);
				}				
			}
		}
	}
	
	private static class Take implements Command {
		public Take(){
		}
		public String getVerb(){
			return "TAKE";
		}
		public void process(String execBy, String cmd, Room room){
			User u = room.userMap.get(execBy);
			if(u!=null){
				String restOfCommand = getCommandWithoutVerbAsString(cmd);
				String itemName = getItemNameFromCommand(restOfCommand, room, u);
				//see if we can find the item in the room.
				ItemDesc item = findItemInRoom(itemName, room);
				if(item!=null){
					restOfCommand = getCommandWithoutVerbAndItemAsString(cmd, item).trim();
					if("".equals(restOfCommand.trim())){					
						if(item.takeable){
							//we have found a match!						
							//add to the player
							u.inventory.add(item);
							//remove from the room. 
							//(using copy on write, so it's safe to call remove on list, else we'd call remove on the iter)
							room.roomDesc.items.remove(item);
							
							room.playerEvent(execBy, "You pick up the "+item.name, u.username+" picks up the "+item.name);
						}else{
							room.playerEvent(execBy, "You try really hard to pick up the "+item.name+" but it's just too tiring.", u.username+" tries to pick up the "+item.name+" and fails.");
						}
					}else{
						room.playerEvent(execBy, "You reach out to take the "+item.name+" but then are confused by what you meant by '"+restOfCommand+"' so leave it there instead.",null);
					}
				}else{
					room.playerEvent(execBy, "You search for the "+restOfCommand+" to pick up, but cannot seem to locate it anywhere!",null);
				}
			}else{
				System.out.println("Cannot process take command for user "+execBy+" as they are not known to the room");
			}
		}
	}
	
	private static class Drop implements Command {
		public Drop(){
		}
		public String getVerb(){
			return "DROP";
		}
		public void process(String execBy, String cmd, Room room){
			User u = room.userMap.get(execBy);
			if(u!=null){
				String itemName = getCommandWithoutVerbAsString(cmd);
				//see if we can find the item in the user.
				ItemDesc item = findItemInInventory(itemName, u);
				if(item!=null){
					//add to the room
					room.roomDesc.items.add(item);
					//remove from the user.
					//(using copy on write, so it's safe to call remove on list, else we'd call remove on the iter)
					u.inventory.remove(item);
					
					//if the item requested it.. clear its state.
					if(item.clearStateOnDrop){
						item.state="";
					}						
					room.playerEvent(execBy, "You drop the "+item.name, u.username+" drops the "+item.name);
				}else{
					room.playerEvent(execBy, "You try to drop the "+itemName+" but it appears you don't actually have one of those.", null);
				}
				
			}else{
				System.out.println("Cannot process drop command for user "+execBy+" as they are not known to the room");
			}
		}
	}
	
	private static class Quit implements Command {
		public Quit(){
		}
		public String getVerb(){
			return "QUIT";
		}
		public void process(String execBy, String cmd, Room room){
			room.playerEvent(execBy,"QUIT?? HA! You can NEVER leave !!! Muahahah!",null);
		}
	}
	
	private static class Examine implements Command {
		public Examine(){
		}
		public String getVerb(){
			return "EXAMINE";
		}
		public void process(String execBy, String cmd, Room room){
			User u = room.userMap.get(execBy);
			if(u!=null){
				String itemName = getCommandWithoutVerbAsString(cmd);
				//see if we can find the item in the room or inventory
				ItemDesc item = findItemInRoomOrInventory(u, itemName, room);
				if(item!=null){
					room.playerEvent(execBy, item.description, null);
				}else{
					room.playerEvent(execBy, "You search for the "+itemName+" to pick up, but cannot seem to locate it anywhere!",null);
				}
			}else{
				System.out.println("Cannot process examine command for user "+execBy+" as they are not known to the room");
			}

		}
	}
	
	private static class Use implements Command {
		public Use(){
		}
		public String getVerb(){
			return "USE";
		}
		public void process(String execBy, String cmd, Room room){
			User u = room.userMap.get(execBy);
			if(u!=null){
				String restOfCommand = getCommandWithoutVerbAsString(cmd);
				String itemName = getItemNameFromCommand(restOfCommand, room, u);
				//see if we can find the item in the room or inventory
				ItemDesc item = findItemInRoomOrInventory(u, itemName, room);
				if(item==null){
					room.playerEvent(execBy, "You search for the "+itemName+" to pick up, but cannot seem to locate it anywhere!",null);
				}else{
					if(item.handler!=null){
						item.handler.processCommand(item, execBy, cmd, room);
					}else{
						room.playerEvent(execBy, "You stare confused at the "+itemName+" unsure quite what to do with it!",null);
					}
				}
			}else{
				System.out.println("Cannot process use command for user "+execBy+" as they are not known to the room");
			}
		}
	}
	
	private static class Help implements Command {
		public Help(){
		}
		public String getVerb(){
			return "HELP";
		}
		public void process(String execBy, String cmd, Room room){
			List<String> currentCmds = new ArrayList<String>();
			for(Command c: globalCommands){
				currentCmds.add(c.getVerb());
			}
			room.playerEvent(execBy, "The following commands are supported: "+currentCmds,null);
		}
	}
	
	private static List<Command> globalCommands = Arrays.asList(new Command[]{new Look(), new Inventory(), new Drop(), new Take(), new Quit(), new Use(), new Examine(), new Help()});
	
	
	/***********************************************************************************************
	 * 
	 * Item and Room definitions.
	 * 
	 * 
	 ***********************************************************************************************/

	String mugEmpty = "A Somewhat sturdy container for liquids, with a small handle.";
	String mugFull = "A Somewhat sturdy container for liquids, with a small handle, full of steaming hot coffee.";
	ItemDesc mug = new ItemDesc("Mug",mugEmpty,true,true,new ItemDesc.CommandHandler(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of mug with coffee machine
			User u = room.userMap.get(execBy);
			if(u!=null){
				String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
				String next = getFirstWordFromCommand(restOfCmd);
				if(next==null){
					if(item.state.equals("full")){
						room.playerEvent(execBy, "You drink the entire cup of coffee.",u.username+" drinks the mug of coffee.");
						item.description = mugEmpty;
						item.state="empty";
					}else{
						//note that default state is "" not "empty", so the else block works great here.
						room.playerEvent(execBy, "You place the mug on your head. Nothing Happens. You put it back.",null);
					}
				}else{
					if("WITH".equals(next)){
						restOfCmd = getCommandWithoutVerbAsString(restOfCmd);
						ItemDesc otherItem = findItemInRoom(restOfCmd, room);
						if(otherItem!=null){
							if(otherItem.equals(coffeeMachine)){
								ItemDesc playerMug = findItemInInventory(item.name, u);
									if(playerMug!=null){
									if(playerMug.state.equals("full")){
										room.playerEvent(execBy, "You attempt to fill the already full cup with more coffee. Coffee goes everywhere, you desperately clean up the coffee hoping nobody noticed.",u.username+" spills coffee all over the floor, then cleans it up.");
									}else{
										room.playerEvent(execBy, "You make a hot cup of coffee.",u.username+" makes a mug of coffee.");
										playerMug.description = mugFull;
										playerMug.state="full";
									}
								}else{
									room.playerEvent(execBy, "You try to telepathically make ther mug interact with the coffee machine, and fail. Perhaps you should take the mug first?",null);
								}
							}else{
								room.playerEvent(execBy, "You try several times to use the "+item.name+" with the "+otherItem.name+" but can't seem to figure out how.",null);
							}
						}else{
							room.playerEvent(execBy, "You aren't quite sure what "+restOfCmd+" is, or how to use it with "+item.name,null);
						}
					}else{
						room.playerEvent(execBy, "I'm sorry dave, I cannot do that!", null);
					}
				}
			}else{
				//player not in room anymore.
			}
		}});
	ItemDesc coffeeMachine = new ItemDesc("Coffee Machine","A machine for making coffee, it appears to be functional.",false,false,new ItemDesc.CommandHandler(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of coffee machine with mug
			User u = room.userMap.get(execBy);
			if(u!=null){
				//remove the 'use itemname'
				String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
				//is the next word 'with' ?
				String next = getFirstWordFromCommand(restOfCmd);
				if(next==null){
					room.playerEvent(execBy, "You randomly press buttons on the coffee machine, hot liquid spills all over the floor, you mop it up, you decide that's probably not how this machine is supposed to be used.",u.username+" uses the coffee machine, spilling coffee everywhere, then quietly mops it up while mumbling about reading instruction manuals");
				}else{
					if("WITH".equals(next)){
						//remove with.. 
						restOfCmd = getCommandWithoutVerbAsString(restOfCmd);
						//assume rest of string is an item.
						ItemDesc otherItem = findItemInRoomOrInventory(u, restOfCmd, room);
						if(otherItem!=null){
							if(otherItem.equals(mug)){
								ItemDesc playerMug = findItemInInventory(restOfCmd, u);
									if(playerMug!=null){
									if(playerMug.state.equals("full")){
										room.playerEvent(execBy, "You attempt to fill the already full cup with more coffee. Coffee goes everywhere, you desperately clean up the coffee hoping nobody noticed.",u.username+" spills coffee all over the floor, then cleans it up.");
									}else{
										room.playerEvent(execBy, "You make a hot cup of coffee.",u.username+" makes a mug of coffee.");
										playerMug.description = mugFull;
										playerMug.state="full";
									}
								}else{
									room.playerEvent(execBy, "You try to telepathically make ther mug interact with the coffee machine, and fail. Perhaps you should take the mug first?",null);
								}
							}else{
								room.playerEvent(execBy, "You try several times to use the "+item.name+" with the "+otherItem.name+" but can't seem to figure out how.",null);
							}
						}else{
							room.playerEvent(execBy, "You aren't quite sure what "+restOfCmd+" is, or how to use it with "+item.name,null);
						}
					}else{
						room.playerEvent(execBy, "I'm sorry dave, I cannot do that!", null);
					}
				}
			}else{
				//player not in room anymore.
			}
		}});
	ItemDesc stilettoHeels = new ItemDesc("Stilettos", "A bright red pair of six inch stilleto heels.",true,true,new ItemDesc.CommandHandler(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {			
			//allow use of heels with player
			//allow use of heels with cupboard 
			//both will set state of heels to 'worn by playerid'
			room.playerEvent(execBy, "You're not sure how to use the heels yet.", null);
		}});
	ItemDesc jukebox = new ContainerDesc("Jukebox", "A gaudy looking unit, it has seen better days.", false,false, new ItemDesc[]{},new ItemDesc.CommandHandler(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of jukebox.
			//should only work if jukebox contains fuse
			room.playerEvent(execBy, "The jukebox appears non functional.", null);
		}});
	ItemDesc fuse = new ItemDesc("Fuse","A small 5 amp cartridge fuse, it appears to be functional.",true,false,new ItemDesc.CommandHandler(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of fuse with jukebox
			//using fuse with jukebox should move it from player inv into jukebox.
			room.playerEvent(execBy, "You try to use the fuse, but you get the blues, and lose your hues.. or something.", null);
		}});
	ItemDesc cupboard = new ContainerDesc("Cupboard","A wall mounted cupboard above the Jukebox.", false,false, new ItemDesc[]{fuse},new ContainerDesc.AccessVerificationHandler() {	
		@Override
		public boolean verifyAccess(ItemDesc item, String execBy, Room room) {
			//only allow access if execBy player has item heels in inventory, and state is 'worn by execBy' 			
			return false;
		}});
	
	RoomDesc bar = new RoomDesc("RecRoom",
			                    "Rec Room","A dimly lit shabbily decorated room, that appears tired and dated. It looks like someone attempted to provide kitchen facilities here once, but you really wouldn't want to eat anything off those surfaces!",
			                    new ItemDesc[] {mug,coffeeMachine,stilettoHeels,jukebox,cupboard},
			                    new String[]{});
	
	
	Collection<Room> rooms = new ArrayList<Room>(Arrays.asList(new Room[]{new Room(bar,globalCommands)}));
	
	
	/***********************************************************************************************
	 * 
	 * Engine methods, and test rig.
	 * 
	 * 
	 ***********************************************************************************************/
	
	public Collection<Room> getRooms(){
		return Collections.unmodifiableCollection(rooms);
	}
	
	private Engine(){		
	}
	
	private static final Engine engine = new Engine();
	
	public static Engine getEngine(){
		return engine;
	}
	
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
