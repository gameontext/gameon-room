package net.wasdev.gameon.room.engine;

import java.util.Collection;
import java.util.HashSet;

import net.wasdev.gameon.room.engine.meta.ItemDesc;

public class User {
	
	public final String id;
	public final String username;
	public final Collection<ItemDesc> inventory;
	
	public User(String id, String username){
		this.id=id; 
		this.username=username; 
		this.inventory=new HashSet<ItemDesc>();
	}
}