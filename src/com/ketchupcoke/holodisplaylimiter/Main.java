package com.ketchupcoke.holodisplaylimiter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

public class Main extends JavaPlugin{
	static JavaPlugin plugin;
	@Override
	public void onEnable(){
    	if(!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")){
    		Bukkit.getLogger().info("[HoloDisplayLimiter] Can't run without HolographicDisplays. Plugin disabled.");
        	this.setEnabled(false);
        	return;
    	}
    	else{
    		plugin = this;
    	    plugin.getConfig();
    	    plugin.getConfig().addDefault("settings.radius", 8);
    	    plugin.getConfig().addDefault("settings.interval", 5);
    		plugin.saveDefaultConfig();
    		plugin.getConfig().options().copyDefaults(true);
    		loadAllHolograms();
    		BukkitScheduler scheduler = getServer().getScheduler();
    		scheduler.scheduleSyncRepeatingTask(plugin, new Runnable(){
    			@Override
    			public void run(){
    				for(Hologram hologram : HologramsAPI.getHolograms(plugin)){
    					double x = hologram.getX();
    					double z = hologram.getZ();
    					for(Player player : Main.plugin.getServer().getOnlinePlayers()){
    						Location playerlocation = player.getLocation();
    						double playerx = playerlocation.getX();
    						double playerz = playerlocation.getZ();
    						VisibilityManager visibilityManager = hologram.getVisibilityManager();
    						if(Math.abs(playerx - x) <= plugin.getConfig().getInt("settings.radius") && Math.abs(playerz - z) <= plugin.getConfig().getInt("settings.radius")){
    							visibilityManager.showTo(player);
    							visibilityManager.setVisibleByDefault(false);
    						}
    						else{
    							visibilityManager.hideTo(player);
    							visibilityManager.setVisibleByDefault(false);
    						}			
    					}
    				}
    				for(Hologram hologram : HologramsAPI.getHolograms(Bukkit.getPluginManager().getPlugin("HolographicDisplays"))){
    					double x = hologram.getX();
    					double z = hologram.getZ();
    					for(Player player : Main.plugin.getServer().getOnlinePlayers()){
    						Location playerlocation = player.getLocation();
    						double playerx = playerlocation.getX();
    						double playerz = playerlocation.getZ();
    						VisibilityManager visibilityManager = hologram.getVisibilityManager();
    						if(Math.abs(playerx - x) <= plugin.getConfig().getInt("settings.radius") && Math.abs(playerz - z) <= plugin.getConfig().getInt("settings.radius")){
    							visibilityManager.showTo(player);
    							visibilityManager.setVisibleByDefault(false);
    						}
    						else{
    							visibilityManager.hideTo(player);
    							visibilityManager.setVisibleByDefault(false);
    						}			
    					}
    				}
    			}
    		}, 0L, plugin.getConfig().getLong("settings.interval"));
    	}
	}

	@Override
	public void onDisable(){
		plugin.saveConfig();
		clearHolograms();
		plugin = null;
		Bukkit.getLogger().info("[HoloDisplayLimiter] Plugin disabled.");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(command.getName().equalsIgnoreCase("hdl")){
			if(args.length == 0){
				sender.sendMessage(ChatColor.GRAY + "HoloDisplayLimiter Version " + ChatColor.GOLD + "1.7\n" + ChatColor.GRAY + "Author: " + ChatColor.GOLD + "ketchupcoke");
				return true;
			}
			else if(args[0].toLowerCase().matches("create") && args.length > 1){
				if(!checkHologramExists(args[1])){
					if(args.length > 5){
						try{
							String name = args[1];
							Player player = (Player) sender;
							World world = player.getLocation().getWorld();
							String worldname = world.getName();
							double x = Double.parseDouble(args[2]);
							double y = Double.parseDouble(args[3]);
							double z = Double.parseDouble(args[4]);
							Location location = new Location(world, x, y, z);
							Hologram hologram = HologramsAPI.createHologram(plugin, location);
							VisibilityManager visibilityManager = hologram.getVisibilityManager();
							visibilityManager.setVisibleByDefault(false);
							String text = "";
						    for(int i = 5; i < args.length; i++){
							if(i == 5){
								text += args[i];
							}
							else{
								text += " " + args[i];
							}
						    }
							saveHologram(name, worldname, location, text);
							int lastpos = 0;
							for(int i = 0; i < text.length(); i++){
								char c = text.charAt(i);
								if(c == '*'){
									@SuppressWarnings("unused")
									TextLine line = hologram.appendTextLine(text.substring(lastpos, i).replaceAll("&", "§"));
									lastpos = i+1;
								}
								else if(i+1 == text.length()){
									@SuppressWarnings("unused")
									TextLine line = hologram.appendTextLine(text.substring(lastpos, text.length()).replaceAll("&", "§"));
								}
							}
							sender.sendMessage(ChatColor.GRAY + "Hologram \"" + ChatColor.GOLD + name + ChatColor.GRAY + "\" created.");
							return true;
						}
						catch(NumberFormatException e){
							sender.sendMessage(ChatColor.GRAY + "Invalid arguments.");
							return false;
						}
					}
					else{
						sender.sendMessage(ChatColor.GRAY + "Usage: /hdl create <name> <x> <y> <z> <text>");
						return true;
					}
				}
				if(args.length > 5 && checkHologramExists(args[1])){
					sender.sendMessage(ChatColor.GRAY + "You must use a unique hologram name.");
					return true;
				}
				else{
					sender.sendMessage(ChatColor.GRAY + "Usage: /hdl create <name> <x> <y> <z> <text>");
					return true;
				}
			}
			else if(args.length == 1 && args[0].toString().matches("create")){
				sender.sendMessage(ChatColor.GRAY + "Usage: /hdl create <name> <x> <y> <z> <text>");
				return true;
			}
			else if(args[0].toLowerCase().matches("bringhere")){
				if(args.length > 1){
					if(checkHologramExists(args[1])){
						Player player = (Player) sender;
						plugin.getConfig().set("holograms." + args[1] + ".world", player.getLocation().getWorld().getName());
						plugin.getConfig().set("holograms." + args[1] + ".x", player.getLocation().getX());
						plugin.getConfig().set("holograms." + args[1] + ".y", player.getLocation().getY());
						plugin.getConfig().set("holograms." + args[1] + ".z", player.getLocation().getZ());
						plugin.saveConfig();
						clearHolograms();
			    		loadAllHolograms();
			    		sender.sendMessage(ChatColor.GRAY + "Hologram \"" + ChatColor.GOLD + args[1] + ChatColor.GRAY + "\" was brought to your location.");
						return true;
					}
					else{
						sender.sendMessage(ChatColor.GRAY + "That hologram does not exist.");
						return true;
					}
				}
				else{
					sender.sendMessage(ChatColor.GRAY + "Usage: /hdl bringhere <name>");
					return true;
				}
			}
			else if(args[0].toLowerCase().matches("help")){
				sender.sendMessage(ChatColor.GOLD + "HoloDisplayLimiter Help:\n" + ChatColor.GOLD + " /hdl:" + ChatColor.GRAY + " Brings up version info.\n" + ChatColor.GOLD + " /hdl help:" + ChatColor.GRAY + " Shows the help page.\n" + ChatColor.GOLD + " /hdl create <name> <x> <y> <z> <text>:" + ChatColor.GRAY + " Creates a hologram at the specified location. Use \"*\" to separate lines.\n" + ChatColor.GOLD + " /hdl bringhere <name>:" + ChatColor.GRAY + " Teleports the specified hologram to your location.\n" + ChatColor.GOLD + " /hdl list:" + ChatColor.GRAY + " Lists all created holograms.\n" + ChatColor.GOLD + " /hdl reload:" + ChatColor.GRAY + " Reloads the config.");
				return true;
			}
			else if(args[0].toLowerCase().matches("reload")){
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.GRAY + "Config reloaded.");
				return true;
			}
			else if(args[0].toLowerCase().matches("list")){
				String list = listHolograms();
				if(list.matches("")){
					sender.sendMessage(ChatColor.GRAY + "No holograms have been created.");
				}
				else{
					sender.sendMessage(ChatColor.GRAY + "Holograms: " + list + ChatColor.GRAY + ".");
				}
				return true;
			}
			else{
				sender.sendMessage("Unknown command. Type \"/help\" for help.");
				return true;
			}
		}
		return false;
	}

	public boolean checkHologramExists(String name){
		if(!(plugin.getConfig().contains("holograms"))){
			return false;
		}
		for(String otherName : plugin.getConfig().getConfigurationSection("holograms").getKeys(false)){
			if(otherName.matches(name)){
				return true;
			}
		}
		return false;
	}

	public void saveHologram(String name, String worldname, Location location, String text){
		plugin.getConfig().set("holograms." + name + ".world", worldname);
		plugin.getConfig().set("holograms." + name + ".x", location.getX());
		plugin.getConfig().set("holograms." + name + ".y", location.getY());
		plugin.getConfig().set("holograms." + name + ".z", location.getZ());
		plugin.getConfig().set("holograms." + name + ".text", text);
		plugin.saveConfig();
	}

	public void loadHologram(String name){
		String worldname = plugin.getConfig().getString("holograms." + name + ".world");
		Double x = plugin.getConfig().getDouble("holograms." + name + ".x");
		Double y = plugin.getConfig().getDouble("holograms." + name + ".y");
		Double z = plugin.getConfig().getDouble("holograms." + name + ".z");
		String text = plugin.getConfig().getString("holograms." + name + ".text");
		World world = Bukkit.getServer().getWorld(worldname);
		Location location = new Location(world, x, y, z);
		Hologram hologram = HologramsAPI.createHologram(plugin, location);
		VisibilityManager visibilityManager = hologram.getVisibilityManager();
		visibilityManager.setVisibleByDefault(false);
		int lastpos = 0;
		for(int i = 0; i < text.length(); i++){
			char c = text.charAt(i);
			if(c == '*'){
				@SuppressWarnings("unused")
				TextLine line = hologram.appendTextLine(text.substring(lastpos, i).replaceAll("&", "§"));
				lastpos = i+1;
			}
			if(i+1 == text.length()){
				@SuppressWarnings("unused")
				TextLine line = hologram.appendTextLine(text.substring(lastpos, text.length()).replaceAll("&", "§"));
			}
		}
	}

	public void loadAllHolograms(){
		if(plugin.getConfig().contains("holograms")){
			for(String name : plugin.getConfig().getConfigurationSection("holograms").getKeys(false)){
				loadHologram(name);
			}
		}
	}

	public void clearHolograms(){
		try{
			for(Hologram hologram : HologramsAPI.getHolograms(plugin)){
				hologram.delete();
			}
		}
		catch(NullPointerException e){
			Bukkit.getLogger().info("[HoloDisplayLimiter] No holograms to clear!");
		}
	}

	public String listHolograms(){
		String list = "";
		if(plugin.getConfig().contains("holograms")){
			for(String name : plugin.getConfig().getConfigurationSection("holograms").getKeys(false)){
				list += ChatColor.GOLD + name + ChatColor.GRAY + ", ";
			}
			return list.substring(0,list.length()-4);
		}
		else{
			return "";
		}
	}
}