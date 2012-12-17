package me.corriekay.pppopp3.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.modules.InvisibilityHandler;
import me.corriekay.pppopp3.rpa.RemotePonyAdmin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftOfflinePlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;

import de.diddiz.LogBlockQuestioner.LogBlockQuestioner;

public abstract class PSCmdExe implements EventExecutor, CommandExecutor, Listener {

	public final String name;
	protected final String pinkieSays = ChatColor.LIGHT_PURPLE+"Pinkie Pie: ";
	protected final HashMap<Class<? extends Event>, Method> methodMap = new HashMap<Class<? extends Event>, Method>();
	protected final ConsoleCommandSender console = Bukkit.getConsoleSender();
	protected final String notPlayer = pinkieSays+"Silly console, You need to be a player to do that!";
	protected final String notEnoughArgs = pinkieSays+"Uh oh, youre gonna need to provide more arguments for that command than that!";
	protected final String cantFindPlayer = pinkieSays+"I looked high and low, but I couldnt find that pony! :C";
	protected final LogBlockQuestioner questioner;
	
	@SuppressWarnings("unchecked")
	public PSCmdExe(String name, String...cmds){
		this.name = name;
		questioner = (LogBlockQuestioner)Bukkit.getPluginManager().getPlugin("LogBlockQuestioner");
		for(Method method : this.getClass().getMethods()){
			method.setAccessible(true);
			Annotation eh = method.getAnnotation(EventHandler.class);
			if(eh != null){
				Class<?>[] params = method.getParameterTypes();
				if(params.length == 1){
					registerEvent((Class<? extends Event>)params[0], ((EventHandler)eh).priority(),method);
				}
			}
		}
		if (cmds != null) {
			registerCommands(cmds);
		}
	}
	private void registerEvent(Class<? extends Event> event, EventPriority priority,Method method){
		try {
			Bukkit.getPluginManager().registerEvent(event, this, priority,this, Mane.getInstance());
			methodMap.put(event, method);
		} catch (NullPointerException e) {
			Bukkit.getLogger().severe("Illegal event registration!");
		} catch (IllegalPluginAccessException e){
			Bukkit.getLogger().severe("Illegal plugin access exception!");
			Bukkit.getLogger().severe(e.getMessage());
			Bukkit.getLogger().severe("Tried to register illegal event: "+event.getCanonicalName());
		}
	}
	private void registerCommands(String[] cmds){
		for(String cmd : cmds){
			try {
				Mane.getInstance().getCommand(cmd).setExecutor(this);
				Mane.getInstance().getCommand(cmd).setPermissionMessage(pinkieSays+"Oh no! You cant do this :c");
			} catch (Exception e) {
				Mane.getInstance().getLogger().severe("Attempted command register failed: \""+cmd+"\" is not registered.");
			}
		}
	}
	@Override
	public void execute(Listener arg0, Event arg1) throws EventException {
		Method method = methodMap.get(arg1.getClass());
		if(method == null){
			return;
		}
		try{
			method.invoke(this,arg1);
		} catch(Exception e){
			if(e instanceof InvocationTargetException){
				InvocationTargetException ite = (InvocationTargetException)e;
				e.setStackTrace(ite.getCause().getStackTrace());
			}
			String eventName = arg1.getClass().getCanonicalName();
			PonyLogger.logListenerException(e, "Error on event: "+e.getMessage(), name, eventName);
			sendMessage(console,"Unhandled exception in listener! Please check the error logs for more information: "+name+":"+e.getClass().getCanonicalName());
		}

	}

	@Override
	public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try{
			return handleCommand(sender,cmd,label,args);
		} catch (Exception e){
			String message = "Exception thrown on command: "+cmd.getName()+"\nLabel: "+label;
			message +="\nArgs: ";
			for(String arg : args){
				message+= arg+" ";
			}
			PonyLogger.logCmdException(e, message, name);
			sendMessage(console,"Unhandled exception on Command! Please check the error log for more information!");
			sendMessage(sender,"OH SWEET CELESTIA SOMETHING WENT HORRIBLY HORRIBLY WRONG! YOU GOTTA TELL THE SERVER ADMINS AS SOON AS YOU CAN D:");
			return false;
		}
	}
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args) throws Exception{
		return false;
	}
	public void logAdmin(CommandSender sender, String message){
		String name = "";
		if(sender instanceof Player){
			name = ((Player)sender).getName();
		} else if(sender instanceof ConsoleCommandSender){
			name = "Console";
		}
		PonyLogger.logAdmin(name, message);
	}
        
        /**
         * Sends a message to a CommandSender prepended with "Pinkie Pie: ".
         * @param sender CommandSender object to issue a command to.
         * @param message Message to send to sender.
         */
	public void sendMessage(CommandSender sender, String message){
		sender.sendMessage(pinkieSays+message);
	}
	/**
     * Sends a message to a CommandSender prepended with "Pinkie Pie: ", through the main thread.
     * @param sender CommandSender object to issue a command to.
     * @param message Message to send to sender.
     */
	public void sendSyncMessage(final CommandSender sender, final String message){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			public void run(){
				sender.sendMessage(pinkieSays+message);
			}
		});
	}

	/*methods for getting players*/
	private void tooManyMatches(ArrayList<String> playerNames, CommandSender requester){
		String message = pinkieSays+"There were too many matches!!";
		for(String string : playerNames){
			message+= string+ChatColor.LIGHT_PURPLE+" ";
		}
		requester.sendMessage(message);
	}
	/**
	 * Player get methods
	 */
	protected Player getOnlinePlayer(String pName, CommandSender requester){
		pName = pName.toLowerCase();
		ArrayList<String> players = new ArrayList<String>();
		for(Player player : Bukkit.getOnlinePlayers()){
			String playername = player.getName().toLowerCase();
			String playerDname = ChatColor.stripColor(player.getDisplayName()).toLowerCase();
			if(playername.equals(pName)||playerDname.equals(pName)){
				return player;
			} else {
				if(playername.contains(pName)||playerDname.contains(pName)){
					if(!InvisibilityHandler.ih.isHidden(player.getName())||requester.hasPermission("pppopp2.seehidden")){
						players.add(player.getName()); 
					}
				}
			}
		}
		if(players.size()>1){
			tooManyMatches(players,requester);
			return null;
		} else if(players.size()<1){
			sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
			return null;
		} else {
			return Bukkit.getPlayerExact(players.get(0));
		}
	}
        /**
         * Returns a String object based on the parameters given that returns 
         * null should more than one player file contain pName.  This will only
         * work using account names, rather than nicknames.
         * @param pName Full or partial String of a username to find.
         * @param requester Reference to the issuer of the command.
         * @return If only one match is found, this returns a String containing
         *         the extensionless filename of the player's file that contains
         *         pName.  Otherwise, this returns <code>null</code>.
         */
	protected String getOfflinePlayer(String pName, CommandSender requester){
		pName = pName.toLowerCase();
		ArrayList<String> player = new ArrayList<String>();
		File dir = new File(Mane.getInstance().getDataFolder()+File.separator+"Players");
		if(!dir.isDirectory()){
			sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
			return null;
		}
		File[] files = dir.listFiles();
		for(File file : files){
			String fname = file.getName();
			if(fname.toLowerCase().equals(pName)){
				return fname;
			}
			if(fname.toLowerCase().contains(pName)){
				player.add(fname);
			}
		}
		if(player.size()==0){
			sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
			return null;
		}
		if(player.size()>1){
			tooManyMatches(player,requester);
			return null;
		}
		return player.get(0);
	}
	protected OfflinePlayer getOnlineOfflinePlayer(String pName, CommandSender requester){
		pName = pName.toLowerCase();
		ArrayList<String> player = new ArrayList<String>();
		for(Player p : Bukkit.getOnlinePlayers()){
			String name = ChatColor.stripColor(p.getName()).toLowerCase();
			String nickname = ChatColor.stripColor(p.getDisplayName().toLowerCase());
			if(name.contains(pName)||nickname.contains(pName)){
				if(InvisibilityHandler.ih.isHidden(p.getName())&&!requester.hasPermission("pppopp3.seehidden")){
					continue;
				}
				if(name.equals(pName)||nickname.equals(pName)){
					return p;
				} else {
					player.add(p.getName());
				}
			}
		}
		if(player.size()>1){
			tooManyMatches(player,requester);
			return null;
		} else if(player.size() == 1){
			return Bukkit.getOfflinePlayer(player.get(0));
		} else {
			File dir = new File(Mane.getInstance().getDataFolder()+File.separator+"Players");
			if(!dir.isDirectory()){
				sendMessage(requester,"I looked high and low, but I couldnt find that pony! :C");
				return null;
			}
			File[] files = dir.listFiles();
			for(File file : files){
				String name = file.getName().toLowerCase();
				if(name.contains(pName)){
					if(name.equals(pName)){
						return Bukkit.getOfflinePlayer(file.getName());
					} else {
						player.add(file.getName());
					}
				}
			}
			if(player.size()>1){
				tooManyMatches(player,requester);
				return null;
			} else if (player.size()<1){
				sendMessage(requester,cantFindPlayer);
				return null;
			} else {
				OfflinePlayer op = Bukkit.getOfflinePlayer(player.get(0));
				if(op.isOnline()){
					op = new CraftOfflinePlayer((CraftServer) Bukkit.getServer(), player.get(0)){
						public Player getPlayer(){
							return null;
						}
					};
				}
				return op;
			}
		}
	}
        /**
         * Sends a message to all Players connected, including the console.
         * @param message Message to send.
         */
	protected void broadcastMessage(String message){
		for(Player p : Bukkit.getOnlinePlayers()){
			p.sendMessage(message);
		}
		console.sendMessage(message);
		RemotePonyAdmin.rpa.message(message);
		
	}
	public static void saveNamedConfig(String name, FileConfiguration config){
		try {
			config.save(new File(Mane.getInstance().getDataFolder(),name));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static FileConfiguration getNamedConfig(String name){
		File file = new File(Mane.getInstance().getDataFolder(),name);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				return null;
			}
		}
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public String getStringFromIndex(int index, String[] args){
		String s = "";
		for(int i = index;i<args.length;i++){
			s += args[i]+" ";
		}
		return s.trim();
	}

	public void deactivate(){
		
	}
}
