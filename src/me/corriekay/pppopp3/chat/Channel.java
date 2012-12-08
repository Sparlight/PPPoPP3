package me.corriekay.pppopp3.chat;

import static me.corriekay.pppopp3.chat.ChatHandler.regex;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;

import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Channel {

	private final String name;
	private final String icon;
	private final String perm;
	private final String quick;
	private String password;
	private final ChatColor cc;
	private final ChatColor w = ChatColor.WHITE;
	
	private final HashSet<String> listeners = new HashSet<String>();
	private final HashSet<String> chatters = new HashSet<String>();

	public Channel(String channelName, String icon, String permission, ChatColor channelColor, String quick, String password){
		name = channelName;
		this.icon = icon;
		perm = permission;
		cc = channelColor;
		this.quick = quick;
		this.password = password;
	}
	public String broadcastToChannel(String who, String whoplayer, String message, boolean log){
		message = parseForLinks(message);
		if(message.startsWith(">")){
			message = ChatColor.GREEN+message;
		}
		String message2send = cc+icon+" "+w+who+w+": "+cc+message;
		for(String name : listeners){
			try{
				if(ChatHandler.silenced.get(name).contains(whoplayer)){
					 continue;
				   }
			} catch (Exception e){
				continue;
			}
			Player player = Bukkit.getPlayerExact(name);
			if(player!=null){
				player.sendMessage(message2send);
			}
		}
		Bukkit.getConsoleSender().sendMessage(message2send);
		if(log){
			PonyLogger.logMessage("ChatLogs"+File.separator+Utils.getFileDate(System.currentTimeMillis()), this.name, "["+Utils.getTimeStamp(System.currentTimeMillis())+"] ["+ChatColor.stripColor(who)+"]: "+message);
		}
		/*TODO
		ChatPacket cp = new ChatPacket();
		cp.channel = name;
		cp.message = ChatColor.stripColor(message2send);
		RemotePonyAdmin.rpa.sendChatPacket(cp);
		*/
		return message2send;
	}
	public void broadcastRawMessage(String message, boolean log){
		message = parseForLinks(message);
		for(String name : listeners){
			Player player = Bukkit.getPlayerExact(name);
			if(player!=null){
				player.sendMessage(message);
			}
		}
		if(log){
			PonyLogger.logMessage("ChatLogs"+File.separator+Utils.getFileDate(System.currentTimeMillis()), name+".txt", "["+Utils.getTimeStamp(System.currentTimeMillis())+"]: "+message);
		}
		/*TODO
		ChatPacket cp = new ChatPacket();
		cp.channel = name;
		cp.message = ChatColor.stripColor(message);
		RemotePonyAdmin.rpa.sendChatPacket(cp);
		*/
		Bukkit.getConsoleSender().sendMessage(message);
	}
	protected String permission(){
		return perm;
	}
	protected void joinChatters(Player player, boolean notify){
		chatters.add(player.getName());
		if(notify){
			player.sendMessage(cc+"Now chatting in channel "+name+"!");
		}
	}
	protected void joinListeners(Player player, boolean notify){

		listeners.add(player.getName());
		if(notify){
			player.sendMessage(cc+"Now listening to channel "+name+"!");
		}
	}
	protected void leaveListeners(Player player, boolean notify){
		listeners.remove(player.getName());
		if(notify){
			player.sendMessage(cc+"No longer listening to channel "+name+"!");
		}
	}
	protected void leaveChatters(Player player, boolean notify){
		chatters.remove(player.getName());
		if(notify){
			player.sendMessage(cc+"No longer chatting in channel "+name+"!");
		}
	}

	protected boolean isListening(Player player){
		return listeners.contains(player.getName());
	}
	protected boolean isChatting(Player player){
		return chatters.contains(player.getName());
	}
	protected String getName(){
		return name;
	}
	protected HashSet<String> getChatters(){
		return chatters;
	}
	protected HashSet<String> getListeners(){
		return listeners;
	}
	protected String getQuick(){
		return quick;
	}
	protected ChatColor color(){
		return cc;
	}private String parseForLinks(String msg){
		String msg2 = "";
		String[] msgArray = msg.split(" ");
		for(String s : msgArray){
			if(s.matches(regex)||s.startsWith("www.")){
				String site = "http://mlpf.im/yourls-api.php?signature=2a0114bbbb&action=shorturl&format=simple&url="+s;
				URL fURL;
				try {
					fURL = new URL(site);
					BufferedReader in = new BufferedReader(new InputStreamReader(fURL.openStream()));
					s = in.readLine();
					in.close();
				} catch (Exception e) {}
			}
			msg2 += s+" ";
		}
		return msg2;
	}
	public String getPassword() {
		return password;
	}
}
