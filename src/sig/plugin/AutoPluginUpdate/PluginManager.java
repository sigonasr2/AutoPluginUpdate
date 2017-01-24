package sig.plugin.AutoPluginUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PluginManager implements Runnable{
	List<Plugin> plugins;
	boolean restarting=false;
	org.bukkit.plugin.Plugin plug=null;
	
	public PluginManager(org.bukkit.plugin.Plugin plug) {
		plugins = new ArrayList<Plugin>();
		this.plug=plug;
	}
	
	@Override
	public void run() {
		FetchPlugins();
	}
	
	void FetchPlugins(){
		for (int i=0;i<plugins.size();i++) {
			/*try {
				FileUtils.copyURLToFile(new URL(plugins.get(i).url), new File(AutoPluginUpdate.datafolder,"updates/"+plugins.get(i).name));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//After that's done, check the hash.
			FileInputStream file = null;
			try {
				file = new FileInputStream(new File(AutoPluginUpdate.datafolder,"updates/"+plugins.get(i).name));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			String md5 = null;
			try {
				md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		    URL url = null;
			try {
				url = new URL(plugins.get(i).url);
			} catch (MalformedURLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if (url!=null) {
				//AutoPluginUpdate.log("Valid URL. ", AutoPluginUpdate.LOG_NORMAL);
			    HttpURLConnection httpCon = null;
				try {
					httpCon = (HttpURLConnection) url.openConnection();
					httpCon.setRequestMethod("HEAD");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (httpCon!=null) {
					//AutoPluginUpdate.log("Connection is good. ", AutoPluginUpdate.LOG_NORMAL);
				    long lastModifiedDate = httpCon.getContentLengthLong();
					//AutoPluginUpdate.log("Last Modified Date Comparison: "+plugins.get(i).lastmodified+","+lastModifiedDate, AutoPluginUpdate.LOG_NORMAL);
					if (plugins.get(i).lastmodified!=lastModifiedDate) {
						//This plugin is different! Update the hash for it. Prepare for a restart of the server!
						final int ii=i;
						try {
							FileUtils.copyURLToFile(new URL(plugins.get(i).url), new File(AutoPluginUpdate.datafolder,"updates/"+plugins.get(i).name));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						//After that's done, check the hash.
						FileInputStream file = null;
						try {
							file = new FileInputStream(new File(AutoPluginUpdate.datafolder,"updates/"+plugins.get(i).name));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						String md5 = null;
						try {
							md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
						try {
							file.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (!md5.equalsIgnoreCase(plugins.get(i).hash)) {
							//AutoPluginUpdate.log("Last Modified Date Comparison: "+plugins.get(i).lastmodified+","+lastModifiedDate, AutoPluginUpdate.LOG_NORMAL);
							Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("AutoPluginUpdate"), new Runnable() {
								@Override
								public void run() {
									if (!AutoPluginUpdate.restarting_server) {
										if (Bukkit.getOnlinePlayers().size()!=0) {
											AutoPluginUpdate.BroadcastMessage("The server has detected a new version of "+ChatColor.YELLOW+plugins.get(ii).name+". The server will restart in 3 minutes!\n\n"+ChatColor.GRAY+ChatColor.ITALIC+"If all players leave, the update will occur immediately.");
										} else {
											AutoPluginUpdate.BroadcastMessage("The server has detected a new version of "+ChatColor.YELLOW+plugins.get(ii).name+".");
										}
										restarting=true;
										AutoPluginUpdate.restarting_server=true;
										//Save the new plugin hash.
									
									} else {
										AutoPluginUpdate.BroadcastMessage("The server has detected a new version of "+ChatColor.YELLOW+plugins.get(ii).name+".");
									}
									if (restarting) {
										AutoPluginUpdate.updateServer();
									}
							}},1);
						}
						plugins.get(i).hash = md5;
						plugins.get(i).lastmodified = lastModifiedDate;
						SaveHash(plugins.get(i));
						
						//Move the file to the new location.
						/*try {
							FileUtils.copyFile(new File(AutoPluginUpdate.datafolder,"updates/"+plugins.get(i).name),
									new File(AutoPluginUpdate.datafolder,"../"+plugins.get(i).name+".jar"));
						} catch (IOException e) {
							e.printStackTrace();
						}*/
					}
				}
			}
		}
	}
	
	public void AddPlugin(String name, String url) {
		plugins.add(new Plugin(name,url));
	}
	
	public void LoadHash(Plugin pluginname) {
		//Read from the server config.
		File config = new File(AutoPluginUpdate.datafolder,"hashes.data");
		FileConfiguration workable = YamlConfiguration.loadConfiguration(config);
		pluginname.hash = workable.getString(pluginname.name+"/HASH");
		pluginname.lastmodified = workable.getLong(pluginname.name+"/HASHMOD");
	}
	public void SaveHash(Plugin pluginname) {
		File config = new File(AutoPluginUpdate.datafolder,"hashes.data");
		FileConfiguration workable = YamlConfiguration.loadConfiguration(config);
		workable.set(pluginname.name+"/HASH",pluginname.hash);
		workable.set(pluginname.name+"/HASHMOD",pluginname.lastmodified);
		try {
			workable.save(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void SaveAllHashes(List<Plugin> pluginlist) {
		File config = new File(AutoPluginUpdate.datafolder,"hashes.data");
		FileConfiguration workable = YamlConfiguration.loadConfiguration(config);
		for (int i=0;i<pluginlist.size();i++) {
			workable.set(pluginlist.get(i).name+"/HASH",pluginlist.get(i).hash);
			workable.set(pluginlist.get(i).name+"/HASHMOD",pluginlist.get(i).lastmodified);
		}
		try {
			workable.save(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Plugin {
	String name;
	String hash;
	String url;
	long lastmodified=0;
	
	public Plugin(String name,String url) {
		this.name=name;
		this.url=url;
		this.hash=FetchHash(); //Try to fetch the hash.
		this.lastmodified=FetchLastModified();
	}
	
	public Plugin(String name,String hash,long lastmodified,String url) {
		this.name=name;
		this.url=url;
		this.hash=hash;
		this.lastmodified=lastmodified;
	}
	
	public String FetchHash() {
		File config = new File(AutoPluginUpdate.datafolder,"hashes.data");
		FileConfiguration workable = YamlConfiguration.loadConfiguration(config);
		return workable.getString(this.name+"/HASH");
	}
	
	public long FetchLastModified() {
		File config = new File(AutoPluginUpdate.datafolder,"hashes.data");
		FileConfiguration workable = YamlConfiguration.loadConfiguration(config);
		return workable.getLong(this.name+"/HASHMOD");
	}
}
