package sig.plugin.AutoPluginUpdate;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoPluginUpdate extends JavaPlugin implements Listener{
	
	public final static String LOG_PREFIX = "[AutoPluginUpdate]";
	
	public AutoPluginUpdate plugin = this;
	public static File datafolder;
	public static boolean restarting_server=false;
	public static boolean main_server=false;
	
	public final static int LOG_ERROR=0;
	public final static int LOG_WARNING=1;
	public final static int LOG_NORMAL=2;
	public final static int LOG_DETAIL=3;
	public final static int LOG_VERBOSE=4;
	public final static int LOG_DEBUG=5;
	
	public static PluginManager pluginupdater;

	@Override
    public void onEnable() {
		//log("Booting up...",LOG_NORMAL);
		datafolder = getDataFolder(); 
		//log("Data folder is located at "+datafolder,LOG_DETAIL);
		
		CheckIfMainServer();
		
		pluginupdater = new PluginManager(plugin);
		pluginupdater.AddPlugin("TwosideKeeper", "https://dl.dropboxusercontent.com/s/z5ram6vi3jipiit/TwosideKeeper.jar");
		pluginupdater.AddPlugin("aPlugin", "https://dl.dropboxusercontent.com/u/62434995/aPlugin.jar");
		pluginupdater.AddPlugin("AutoPluginUpdate", "https://dl.dropboxusercontent.com/s/q59lndgromemv0p/AutoPluginUpdate.jar");
		
		Bukkit.getPluginManager().registerEvents(this, this);
		
		if (!restarting_server && main_server) {
			Bukkit.getScheduler().runTaskTimerAsynchronously(this, pluginupdater, 600l, 600l);
		}
	}

	private void CheckIfMainServer() {
		File checkfile = new File("plugins/TwosideKeeper","config.yml");
		if (checkfile.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(checkfile);
			int type = config.getInt("SERVER_TYPE");
			if (type==0) {
				main_server=true;
				log("Identified this server as the MAIN SERVER.",LOG_DETAIL);
				log("Features Enabled.",LOG_DETAIL);
			}
		}
	}

	@Override
    public void onDisable() {
		 
	}  

    @EventHandler(priority=EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev) {
    	if (!restarting_server && main_server) {
    		Bukkit.getScheduler().runTaskAsynchronously(this, pluginupdater);
    	}
    }
    
	@EventHandler(priority=EventPriority.LOW,ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent ev) {
		if (main_server) {
			if (Bukkit.getOnlinePlayers().size()==1 && restarting_server) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, ()->{BroadcastMessage(ChatColor.ITALIC+"Server is shutting down...");},3);
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new ShutdownServerForUpdate(),5);
		}
	}
    
	public static void log(String msg, int loglv) {
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
				//BroadcastMessage(ChatColor.ITALIC+"Server is shutting down...");
				for (int i=0;i<Bukkit.getWorlds().size();i++) {
					Bukkit.getWorlds().get(i).save();
				}
				Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), ()->{Bukkit.shutdown();}, 60);
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
				Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), ()->{Bukkit.shutdown();}, 60);
			}
		},20*180*((Bukkit.getOnlinePlayers().size()==0)?0:1)+1);
		
	}

	public static void BroadcastMessage(String msg) {
		Bukkit.getPluginManager().callEvent(new AnnounceUpdateEvent(msg));
		Bukkit.broadcastMessage(msg);
	}
}
