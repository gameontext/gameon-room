package net.wasdev.gameon.room.engine.sample.items;

import java.util.concurrent.atomic.AtomicBoolean;

import net.wasdev.gameon.room.engine.ItemCommand;
import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class Items {

	public static final ItemDesc mug = new ItemDesc("Mug",null,true,false,new ItemCommand(){	
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of mug with coffee machine
			User u = room.getUserById(execBy);
			if(u!=null){
				String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
				String next = getFirstWordFromCommand(restOfCmd);
				if(next==null){
					if(item.getAndSetState("full","empty")){
						room.playerEvent(execBy, "You drink the entire cup of coffee.",u.username+" drinks the mug of coffee.");
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
									if(playerMug.getAndSetState("empty", "full") || playerMug.getAndSetState("", "full")){
										room.playerEvent(execBy, "You make a hot cup of coffee.",u.username+" makes a mug of coffee.");
									}else{
										room.playerEvent(execBy, "You attempt to fill the already full cup with more coffee. Coffee goes everywhere, you desperately clean up the coffee hoping nobody noticed.",u.username+" spills coffee all over the floor, then cleans it up.");
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
		}}, new ItemDesc.ItemDescriptionHandler(){
			private static final String mugEmpty = "A Somewhat sturdy container for liquids, with a small handle.";
			private static final String mugFull = "A Somewhat sturdy container for liquids, with a small handle, full of steaming hot coffee.";
			@Override
			public String getDescription(ItemDesc item, String execBy, String cmd, Room room) {
				if(item.getState().equals("full")){
					return mugFull;
				}else{
					return mugEmpty;
				}
			}		
		});
	public static final ItemDesc coffeeMachine = new ItemDesc("Coffee Machine","A machine for making coffee, it appears to be functional.",false,false,new ItemCommand(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of coffee machine with mug
			User u = room.getUserById(execBy);
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
									if(playerMug.getAndSetState("empty", "full") || playerMug.getAndSetState("", "full")){
										room.playerEvent(execBy, "You make a hot cup of coffee.",u.username+" makes a mug of coffee.");
									}else{
										room.playerEvent(execBy, "You attempt to fill the already full cup with more coffee. Coffee goes everywhere, you desperately clean up the coffee hoping nobody noticed.",u.username+" spills coffee all over the floor, then cleans it up.");
									}
								}else{
									room.playerEvent(execBy, "You try to telepathically make the mug interact with the coffee machine, and fail. Perhaps you should take the mug first?",null);
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
	public static final ItemDesc stilettoHeels = new ItemDesc("Stilettos", "A bright red pair of six inch stilleto heels.",true,true,new ItemCommand(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {			
			//allow use of heels with player (just use heels, must be in inventory)
			//allow use of heels with cupboard (if in inventory)
			//both will set state of heels to 'worn by playerid'
			//heels have 'clear state on drop' set, so if dropped, removes that state.			
			//allow use of coffee machine with mug
			User u = room.getUserById(execBy);
			if(u!=null){
				boolean itemIsInInventory = findItemInInventory(item.name, u)!=null;
				
				//remove the 'use itemname'
				String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
				//is the next word 'with' ?
				String next = getFirstWordFromCommand(restOfCmd);
				if(next==null){
					//player tried to use heels, are they in player inventory?
					if(itemIsInInventory){
						//yes, player has item in inventory						
						if(item.getAndSetState("", "wornby:"+u.id)){
							room.playerEvent(execBy, "You look at the heels carefully, and realise they are just your size. You slip your feet into the shoes, and slowly stand up. You feel taller!",u.username+" wears the stilettos.");	
						}else{
							//player already wearing heels.
							room.playerEvent(execBy, "You consider carefully how to use the stilettos now you are already wearing them, and decide to perform a little dance.",u.username+" does a dainty little dance in the heels.");
						}
					}else{
						//no, item is in room.
						room.playerEvent(execBy, "From here, it looks like they might be your size, but you can't be sure, perhaps if you picked them up?",null);
					}
				}else{
					if("WITH".equals(next)){
						//remove with.. 
						restOfCmd = getCommandWithoutVerbAsString(restOfCmd);
						//assume rest of string is an item.
						ItemDesc otherItem = findItemInRoomOrInventory(u, restOfCmd, room);
						if(otherItem!=null){
							if(otherItem.equals(cupboard)){
								//player tried to use heels, are they in player inventory?
								if(itemIsInInventory){
									//yes, player has item in inventory
									if(item.getAndSetState("", "wornby:"+u.id)){
										room.playerEvent(execBy, "You look at the heels carefully, and realise they are just your size. You slip your feet into the shoes, and slowly stand up. You feel tall enough to see into the cupboard now.",u.username+" wears the stilettos.");	
									}else{
										//player already wearing heels.
										room.playerEvent(execBy, "You are already wearing the heels, and are unsure how you can use them with the cupboard in any other way.",null);
									}
								}else{
									//no, item is in room.
									room.playerEvent(execBy, "From here, it looks like they might be your size, but you can't be sure, perhaps if you picked them up?",null);
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
	public static final ItemDesc jukebox = new ContainerDesc("Jukebox", "A gaudy looking unit, it has seen better days.", false,false, new ItemDesc[]{},new ItemCommand(){
		//we really only want one jukebox to play at once ;p
		AtomicBoolean isPlaying = new AtomicBoolean(false);		
		class JukeBoxPlayer implements Runnable {
			Room room;
			public JukeBoxPlayer(Room room){
				this.room =room;
			}
			@Override
			public void run() {
				if(isPlaying.compareAndSet(false,true)){
					try{	
						room.roomEvent("The jukebox sings \"Never gonna give you up.. \"");
						Thread.sleep(1000*10);
						room.roomEvent("The jukebox sings \"Never gonna let you down.. \"");
						Thread.sleep(1000*10);
						room.roomEvent("The jukebox sings \"Never gonna run around.. \"");
						Thread.sleep(1000*10);
						room.roomEvent("The jukebox sings \"And desert you.. \"");
						Thread.sleep(1000*5);
						room.roomEvent("The jukebox emits a bright arc of light, and a small puff of smoke.. and stops working.");
						Thread.sleep(1000*1);
					}catch(InterruptedException io){
						//ignore.
					}
					ContainerDesc box = (ContainerDesc)jukebox;
					box.items.remove(fuse);
					ContainerDesc cupboardBox = (ContainerDesc)cupboard;
					cupboardBox.items.add(fuse);
					room.roomEvent("You experience an odd feeling of deja vu.");
					isPlaying.compareAndSet(true, false);
				}	
			}
		}	
		
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of jukebox.
			//should only work if jukebox contains fuse
			User u = room.getUserById(execBy);
			if(u!=null){	
				ContainerDesc jb = (ContainerDesc)item;
				//remove the 'use itemname'
				String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
				//is the next word 'with' ?
				String next = getFirstWordFromCommand(restOfCmd);
				if(next==null){
					if(jb.items.contains(fuse)){
						room.playerEvent(execBy, "The jukebox plays music, you are so happy!",u.username+" makes the jukebox play music.");
						(new Thread(new JukeBoxPlayer(room))).start();
					}else{
						room.playerEvent(execBy, "The jukebox appears to be non functional, there's a large slot marked 15A that appears to be empty.",null);
					}
				}else{
					if("WITH".equals(next)){
						//remove with.. 
						restOfCmd = getCommandWithoutVerbAsString(restOfCmd);
						//assume rest of string is an item.
						ItemDesc otherItem = findItemInRoomOrInventory(u, restOfCmd, room);
						if(otherItem!=null){
							if(otherItem.equals(fuse)){
								//is the fuse in the users inventory.. 
								boolean itemIsInInventory = findItemInInventory(otherItem.name, u)!=null;
								if(itemIsInInventory){
									//yes, player has item in inventory
									room.playerEvent(execBy, "You take the fuse, and insert it into the jukebox. Fingers crossed!",u.username+" installs the fuse into the jukebox.");
									jb.items.add(fuse);
									u.inventory.remove(fuse);
								}else{
									//no, item is in room.
									room.playerEvent(execBy, "That fuse looks remarkably like it might fit in that jukebox, but the fuse is all the way over there, perhaps you should take the fuse first?",null);
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
	public static final ItemDesc fuse = new ItemDesc("Fuse","A small 5 amp cartridge fuse, it appears to be functional.",true,false,new ItemCommand(){
		@Override
		public void processCommand(ItemDesc item, String execBy, String cmd, Room room) {
			//allow use of fuse with jukebox
			//using fuse with jukebox should move it from player inv into jukebox.
			User u = room.getUserById(execBy);
			if(u!=null){	
				//remove the 'use itemname'
				String restOfCmd = getCommandWithoutVerbAndItemAsString(cmd,item);
				//is the next word 'with' ?
				String next = getFirstWordFromCommand(restOfCmd);
				if(next==null){
					room.playerEvent(execBy, "The thing about fuses, is they are not all that interesting, they don't play video games, and they don't have buttons to press.",null);
				}else{
					if("WITH".equals(next)){
						//remove with.. 
						restOfCmd = getCommandWithoutVerbAsString(restOfCmd);
						//assume rest of string is an item.
						ItemDesc otherItem = findItemInRoomOrInventory(u, restOfCmd, room);
						if(otherItem!=null){
							if(otherItem.equals(jukebox)){
								//is the fuse in the users inventory.. 
								boolean itemIsInInventory = findItemInInventory(item.name, u)!=null;
								if(itemIsInInventory){
									//yes, player has item in inventory
									room.playerEvent(execBy, "You take the fuse, and insert it into the jukebox. Fingers crossed!",u.username+" installs the fuse into the jukebox.");
									ContainerDesc jb = (ContainerDesc)otherItem;
									jb.items.add(fuse);
									u.inventory.remove(fuse);
								}else{
									//no, item is in room.
									room.playerEvent(execBy, "That fuse looks remarkably like it might fit in that jukebox, but the fuse is all the way over there, perhaps you should take the fuse first?",null);
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
	public static final ItemDesc cupboard = new ContainerDesc("Cupboard",null,false,false, new ItemDesc[]{fuse},new ContainerDesc.AccessVerificationHandler() {	
		@Override
		public boolean verifyAccess(ItemDesc item, String execBy, Room room) {
			//only allow access if execBy player has item heels in inventory, and state is 'worn by execBy'
			User u = room.getUserById(execBy);
			if(u!=null){
				if(u.inventory.contains(stilettoHeels) && stilettoHeels.getState().equals("wornby:"+u.id)){
					return true;
				}
			}
			return false;
		}}, null, new ItemDesc.ItemDescriptionHandler(){
			@Override
			public String getDescription(ItemDesc item, String execBy, String cmd, Room room) {
				ContainerDesc box = (ContainerDesc)item;
				if(box.access.verifyAccess(item,execBy,room)){
					return "A wall mounted cupboard above the Jukebox, with the shoes on, you are tall enough to reach it.";
				}
				return "A wall mounted cupboard above the Jukebox, it's just out of your reach.";
			}
			
		});
}
