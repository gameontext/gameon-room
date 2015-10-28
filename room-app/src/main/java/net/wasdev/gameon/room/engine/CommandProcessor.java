package net.wasdev.gameon.room.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import net.wasdev.gameon.room.engine.meta.ContainerDesc;
import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class CommandProcessor {

	protected static ItemDesc findItemInInventory(String itemName, User execBy) {
		for(ItemDesc item :  execBy.inventory){
			if(item.name.equalsIgnoreCase(itemName)){	
				return item;
			}
		}
		return null;
	}

	protected static ItemDesc findItemInRoom(String itemName, Room room) {
		for(ItemDesc item : room.roomDesc.items){
			if(item.name.equalsIgnoreCase(itemName)){	
				return item;
			}
		}
		return null;
	}

	protected static ItemDesc findItemInContainers(String itemName, Room room) {
		for(ItemDesc item : room.roomDesc.items){
			if(item instanceof ContainerDesc){
				ContainerDesc box = (ContainerDesc)item;
				for(ItemDesc boxItem : box.items){
					if(boxItem.name.equalsIgnoreCase(itemName)){	
						return boxItem;
					}
				}
			}
		}
		return null;
	}

	protected static ItemDesc findItemInRoomOrInventory(User execBy, String itemName, Room room) {
		ItemDesc result = findItemInRoom(itemName, room);
		if(result==null)
			result = findItemInInventory(itemName, execBy);
		return result;
	}

	protected static String getCommandWithoutVerbAsString(String cmd) {
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

	protected static String getCommandWithoutVerbAndItemAsString(String cmd, ItemDesc item) {
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

	protected static String getItemNameFromCommand(String cmd, Room room, User execBy) {		
		List<String> allItems = new ArrayList<String>();
		for(ItemDesc item : room.roomDesc.items){
			allItems.add(item.name.trim().toUpperCase());
			if(item instanceof ContainerDesc){
				ContainerDesc box = (ContainerDesc)item;
				for(ItemDesc boxItem: box.items){
					allItems.add(boxItem.name.trim().toUpperCase());
				}
			}
		}
		for(ItemDesc item :  execBy.inventory){
			allItems.add(item.name.trim().toUpperCase());
			if(item instanceof ContainerDesc){
				ContainerDesc box = (ContainerDesc)item;
				for(ItemDesc boxItem: box.items){
					allItems.add(boxItem.name.trim().toUpperCase());
				}
			}
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

	protected static String getFirstWordFromCommand(String cmd) {
		Scanner s = new Scanner(cmd);
		if(s.hasNext()){
			return s.next().trim().toUpperCase();
		}else{
			return null;
		}			
	}

	public CommandProcessor() {
		super();
	}

}