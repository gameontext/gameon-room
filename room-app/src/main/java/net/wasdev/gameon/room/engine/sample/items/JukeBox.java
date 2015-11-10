package net.wasdev.gameon.room.engine.sample.items;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.User;
import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;
import net.wasdev.gameon.room.engine.parser.CommandTemplate;
import net.wasdev.gameon.room.engine.parser.ContainerItem;
import net.wasdev.gameon.room.engine.parser.Item;
import net.wasdev.gameon.room.engine.parser.ItemUseHandler;
import net.wasdev.gameon.room.engine.parser.Node.Type;
import net.wasdev.gameon.room.engine.parser.ParsedCommand;

public class JukeBox extends ContainerDesc {
	
	public static final ItemUseHandler handler = new ItemUseHandler(){
		
		private final CommandTemplate useJukeBoxInRoom =new CommandTemplateBuilder().build(Type.CONTAINER_ITEM).build();		
		private final CommandTemplate useJukeBoxWithInventoryItem =new CommandTemplateBuilder().build(Type.CONTAINER_ITEM).build(Type.LINKWORD,"With").build(Type.INVENTORY_ITEM).build();
		private final CommandTemplate useJukeBoxWithRoomItem =new CommandTemplateBuilder().build(Type.CONTAINER_ITEM).build(Type.LINKWORD,"With").build(Type.ROOM_ITEM).build();
		
		private final Set<CommandTemplate> templates = Collections.unmodifiableSet(new HashSet<CommandTemplate>(Arrays.asList(new CommandTemplate[]{
				useJukeBoxInRoom,
				useJukeBoxWithInventoryItem,
				useJukeBoxWithRoomItem
		})));
		
		@Override
		public Set<CommandTemplate> getTemplates() {
			return templates;
		}

		@Override
		public boolean isHidden() {
			return false;
		}
		
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
					ContainerDesc box = (ContainerDesc)Items.jukebox;
					box.items.remove(Items.fuse);
					ContainerDesc cupboardBox = (ContainerDesc)Items.cupboard;
					cupboardBox.items.add(Items.fuse);
					room.roomEvent("You experience an odd feeling of deja vu.");
					isPlaying.compareAndSet(true, false);
				}	
			}
		}	
		
		@Override
		public void processCommand(Room room, String execBy, ParsedCommand command) {
			String key = command.key;
			User u = room.getUserById(execBy);
			if(u!=null){ 
				//every template has this item as the first item.. 
				ContainerItem jb = (ContainerItem)command.args.get(0);
				if(key.equals(useJukeBoxInRoom.key)){
					//use jukebox
					
					if(jb.container.items.contains(Items.fuse)){
						room.playerEvent(execBy, "The jukebox plays music, you are so happy!",u.username+" makes the jukebox play music.");
						(new Thread(new JukeBoxPlayer(room))).start();
					}else{
						room.playerEvent(execBy, "The jukebox appears to be non functional, there's a large slot marked 15A that appears to be empty.",null);
					}
				}else if(key.equals(useJukeBoxWithInventoryItem.key)){
					//use jukebox with inventory item
					
					Item other = (Item)command.args.get(2);
					if(other.item == Items.fuse){
						//yes, player has item in inventory
						room.playerEvent(execBy, "You take the fuse, and insert it into the jukebox. Fingers crossed!",u.username+" installs the fuse into the jukebox.");
						jb.container.items.add(Items.fuse);
						u.inventory.remove(Items.fuse);
					}else{
						room.playerEvent(execBy, "You try several times to use the fuse with the "+other.item.name+" but can't seem to figure out how.",null);
					}
				}else{
					//use jukebox with room item.
					
					Item other = (Item)command.args.get(2);
					//give a clue if it's the fuse. 
					if(other.item == Items.fuse){
						room.playerEvent(execBy, "That fuse looks remarkably like it might fit in that jukebox, but the fuse is all the way over there, perhaps you should take the fuse first?",null);
					}else{
						room.playerEvent(execBy, "You try several times to use the fuse with the "+other.item.name+" but can't seem to figure out how.",null);
					}
				}
			}
		}
		
		@Override
		public void processUnknown(Room room, String execBy, String origCmd, String cmdWithoutVerb) {
			room.playerEvent(execBy, "The jukebox stares at you disapprovingly, unsure what you meant" + cmdWithoutVerb, null);
		}		
	};
	
	public JukeBox(){
		super("Jukebox", "A gaudy looking unit, it has seen better days.", false,false, new ItemDesc[]{},handler);
	}
}
