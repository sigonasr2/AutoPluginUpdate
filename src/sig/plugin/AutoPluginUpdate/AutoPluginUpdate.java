package sig.plugin.AutoPluginUpdate;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoPluginUpdate extends JavaPlugin implements Listener{
	
	public final String LOG_PREFIX = "[AutoPluginUpdate]";
	
	public AutoPluginUpdate plugin = this;
	public static File datafolder;
	public static boolean restarting_server=false;
	
	public final int LOG_ERROR=0;
	public final int LOG_WARNING=1;
	public final int LOG_NORMAL=2;
	public final int LOG_DETAIL=3;
	public final int LOG_VERBOSE=4;
	public final int LOG_DEBUG=5;
	
	public static PluginManager pluginupdater;

	@Override
    public void onEnable() {
		log("Booting up...",LOG_NORMAL);
		datafolder = getDataFolder(); 
		log("Data folder is located at "+datafolder,LOG_DETAIL);
		
		pluginupdater = new PluginManager(plugin);
		pluginupdater.AddPlugin("TwosideKeeper", "https://dl.dropboxusercontent.com/s/z5ram6vi3jipiit/TwosideKeeper.jar");
		pluginupdater.AddPlugin("aPlugin", "https://dl.dropboxusercontent.com/u/62434995/aPlugin.jar");
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		if (!restarting_server) {
			Bukkit.getScheduler().runTaskTimerAsynchronously(this, pluginupdater, 6000l, 6000l);
		}
	}

	@Override
    public void onDisable() {
		 
	}  

    @EventHandler(priority=EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev) {
    	if (!restarting_server) {
    		Bukkit.getScheduler().runTaskAsynchronously(this, pluginupdater);
    	}
    }
    
	@EventHandler(priority=EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent ev) {
    	Bukkit.getScheduler().scheduleSyncDelayedTask(this, new ShutdownServerForUpdate(),5);
	}
    
	private void log(String msg, int loglv) {
		switch (loglv) {
			case LOG_ERROR:{
				Bukkit.getConsoleSender().sendMessage(LOG_PREFIX+" "+ChatColor.RED+"[ERROR]"+ChatColor.RESET+msg+ChatColor.RESET);
			}break;
			case LOG_WARNING:{
				Bukkit.getConsoleSender().sendMessage(LOG_PREFIX+" "+ChatColor.YELLOW+"[WARNING]"+ChatColor.RESET+msg+ChatColor.RESET);
			}break;
			default:{
				Bukkit.getConsoleSender().sendMessage(LOG_PREFIX+" "+ChatColor.RESET+msg+ChatColor.RESET);
			}break;
		}
	}

	private final class ShutdownServerForUpdate implements Runnable {
		@Override
		public void run() {
			if (Bukkit.getOnlinePlayers().size()==0 && restarting_server) {
				Bukkit.savePlayers();
				for (int i=0;i<Bukkit.getWorlds().size();i++) {
					Bukkit.getWorlds().get(i).save();
				}
				Bukkit.shutdown();
			}
		}
	}
	
	public static void updateServer() {
		if (Bukkit.getOnlinePlayers().size()!=0) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), new Runnable() {
				@Override
				public void run() {
					Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), pluginupdater);
					BroadcastMessage(ChatColor.YELLOW+"The server is restarting in 1 minute for a plugin update!");
				}
			},20*120);
			Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), new Runnable() {
				@Override
				public void run() {
					Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), pluginupdater);
					BroadcastMessage(ChatColor.RED+"The server is restarting in 10 seconds!");
				}
			},20*170);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), new Runnable() {
			@Override
			public void run() {
				Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), pluginupdater);
				Bukkit.savePlayers();
				BroadcastMessage(ChatColor.ITALIC+"Server is shutting down...");
				for (int i=0;i<Bukkit.getWorlds().size();i++) {
					Bukkit.getWorlds().get(i).save();
				}
				Bukkit.shutdown();
			}
		},20*180*((Bukkit.getOnlinePlayers().size()==0)?0:1)+1);
		
	}

	public static void BroadcastMessage(String msg) {
		Bukkit.getPluginManager().callEvent(new AnnounceUpdateEvent(msg));
		Bukkit.broadcastMessage(msg);
	}
}
