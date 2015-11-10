package net.wasdev.gameon.room.engine.meta;

import net.wasdev.gameon.room.engine.Room;
import net.wasdev.gameon.room.engine.parser.ItemUseHandler;

public class ItemDesc {
	public final String name;
	private final String description;
	public final boolean takeable;
	public final boolean clearStateOnDrop;
	private String state = "";
	private final Object stateMonitor = new Object();

	public final ItemUseHandler useHandler;

	public interface ItemDescriptionHandler {
		public String getDescription(ItemDesc item, String execBy, String cmd, Room room);
	}

	public final ItemDescriptionHandler descHandler;

	public ItemDesc(String name, String description) {
		this(name, description, false, true, null, null);
	}

	public ItemDesc(String name, String description, boolean takeable, boolean clearStateOnDrop) {
		this(name, description, takeable, clearStateOnDrop, null, null);
	}

	public ItemDesc(String name, String description, boolean takeable) {
		this(name, description, takeable, false, null, null);
	}

	public ItemDesc(String name, String description, boolean takeable, boolean clearStateOnDrop, ItemUseHandler handler) {
		this(name, description, takeable, clearStateOnDrop, handler, null);
	}

	public ItemDesc(String name, String description, boolean takeable, boolean clearStateOnDrop,
			ItemDescriptionHandler descHandler) {
		this(name, description, takeable, clearStateOnDrop, null, descHandler);
	}
	
	public ItemDesc(String name, String description, boolean takeable, boolean clearStateOnDrop, ItemUseHandler handler, ItemDescriptionHandler descHandler) {
		this.name = name;
		this.description = description;
		this.takeable = takeable;
		this.clearStateOnDrop = clearStateOnDrop;
		this.descHandler = descHandler;
		this.useHandler=handler;
	}

	public void setState(String newstate) {
		synchronized (stateMonitor) {
			this.state = newstate;
		}
	}

	public boolean getAndSetState(String oldstate, String newstate) {
		synchronized (stateMonitor) {
			if (this.state.equals(oldstate)) {
				this.state = newstate;
				return true;
			} else {
				return false;
			}
		}
	}

	public String getState() {
		return this.state;
	}

	public String getDescription(String execBy, String cmd, Room room) {
		if (descHandler == null) {
			return this.description;
		} else {
			return descHandler.getDescription(this, execBy, cmd, room);
		}
	}
}