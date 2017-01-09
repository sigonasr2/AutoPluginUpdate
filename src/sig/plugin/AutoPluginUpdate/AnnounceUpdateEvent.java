package sig.plugin.AutoPluginUpdate;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AnnounceUpdateEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private String announce_msg;
	
	public AnnounceUpdateEvent(String announce_msg) {
		this.announce_msg=announce_msg;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	public String getAnnouncementMessage() {
		return announce_msg;
	}
	
}
