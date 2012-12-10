package me.corriekay.pppopp3.chat;

import java.io.File;
import java.util.HashSet;

import me.corriekay.packets.server.ChatPacket;
import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.PrivateMessageEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PonySpy extends PSCmdExe {
	
	private HashSet<String> spies = new HashSet<String>();
	
	public PonySpy(){
		super("PonySpy","ponyspy");
		for(Player player : Bukkit.getOnlinePlayers()){
			Pony pony = Ponyville.getPony(player);
			if(pony.isPonySpy()&&player.hasPermission("pppopp2.ponyspy")){
				spies.add(player.getName());
			}
		}
	}
	@EventHandler
	public void onPM(PrivateMessageEvent event){
		if(ChatHandler.silenced.get(event.getReciever().getName()).contains(event.getSender().getName())){
			event.setCancelled(true);
			sendMessage(event.getSender(),"Uh oh, looks like that player has muted you!");
			return;
		}
		PonyLogger.logMessage("ChatLogs" + File.separator+ Utils.getFileDate(System.currentTimeMillis()), "Private Messages", "");
		String message = ChatColor.RED+"[PS]["+event.getSender().getDisplayName()
				+ChatColor.RED+" > "+event.getReciever().getDisplayName()+ChatColor.RED
				+"]"+ChatColor.GRAY+": "+event.getMsg();
		Bukkit.getConsoleSender().sendMessage(message);
		ChatPacket cp = new ChatPacket();
		cp.message = ChatColor.stripColor(message);
		cp.channel = "ponyspy";
		//RemotePonyAdmin.rpa.sendChatPacket(cp); TODO
		PonyLogger.logMessage("ChatLogs" + File.separator+ Utils.getFileDate(System.currentTimeMillis()), "Private Messages", message);
		for(String spy : spies){
			if(spy.equals(event.getReciever().getName())||spy.equals(event.getSender().getName())){
				continue;
			}
			Player spai = Bukkit.getPlayerExact(spy);
			if(spai!=null){
				spai.sendMessage(message);
			}
		}
	}
	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)){
			sendMessage(sender,notPlayer);
			return true;
		}
		Player player = (Player)sender;
		if(cmd.getName().equals("ponyspy")){
			Pony pony = Ponyville.getPony(player);
			boolean ps = pony.isPonySpy();
			if(ps){
				spies.remove(player.getName());
				sendMessage(player,"Ponyspy deactivated!");
				pony.setPonySpy(false);
			} else {
				spies.add(player.getName());
				sendMessage(player,"Ponyspy activated!");
				pony.setPonySpy(true);
			}
			pony.save();
			return true;
		}
		return true;
	}
	@EventHandler
	public void onJoin(JoinEvent event){
		if(event.isJoining()&&event.getPony().isPonySpy()){
			spies.add(event.getPlayer().getName());
		}
	}
	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()){
			spies.remove(event.getPlayer().getName());
		}
	}
}
