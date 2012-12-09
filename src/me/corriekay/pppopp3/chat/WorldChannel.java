package me.corriekay.pppopp3.chat;

import java.io.File;

import me.corriekay.pppopp3.modules.Equestria;
import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldChannel extends Channel{

	public WorldChannel(String channelName, String icon, String permission, ChatColor channelColor, String quick) {
		super(channelName, icon, permission, channelColor, quick);
	}


	public String broadcastToChannel(String who, String whoplayer, String message, boolean log){
		message = parseForLinks(message);
		if(message.startsWith(">")){
			message = ChatColor.GREEN+message;
		}
		String message2send = cc+icon+" "+w+who+w+": "+cc+message;
		World senderWorld = Equestria.get().getParentWorld(Bukkit.getPlayerExact(whoplayer).getWorld());
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
				World recieverworld = Equestria.get().getParentWorld(player.getWorld());
				if(recieverworld.getName().equals(senderWorld.getName())||player.hasPermission("pppopp3.chat.staff")){
					player.sendMessage(message2send);
				}
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
}