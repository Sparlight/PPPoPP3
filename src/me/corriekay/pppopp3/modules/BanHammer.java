 package me.corriekay.pppopp3.modules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.scheduler.BukkitScheduler;

public class BanHammer extends PSCmdExe {

	private final String banmsg;
	private final HashSet<String> mooned = new HashSet<String>();

	public BanHammer() {
		super("BanHammer","ban","kick","tempban","unban","notes","createnote");
		FileConfiguration config = Mane.getInstance().getConfig();
		banmsg = config.getString("banMessage","You have been banned!");
	}
	public boolean handleCommand(final CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("notes")){
			if(args.length<1){
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			String target = getSinglePlayer(args[0],sender);
			if(target == null){
				return true;
			}
			Pony pony = Ponyville.getOfflinePony(target);
			List<String> notes = pony.getNotes();
			if(notes == null) notes = new ArrayList<String>();
			sender.sendMessage("**** Notes for "+pony.getName()+"/"+pony.getNickname()+ChatColor.WHITE+"*****");
			for(String note : notes){
				sender.sendMessage("- "+note);
			}
			sender.sendMessage("********************");
			return true;
		}
		if(cmd.getName().equals("createnote")){
			if(args.length<2){
				sendMessage(sender,notEnoughArgs);
				return true;
			}
			String target = getSinglePlayer(args[0],sender);
			if(target == null){
				return true;
			}
			String name;
			String message = "";
			if(sender instanceof ConsoleCommandSender){
				name = "Console";
			} else {
				name = ((Player)sender).getName();
			}
			for(int i = 1 ; i < args.length ; i ++){
				message+=args[i]+" ";
			}
			Pony pony = Ponyville.getOfflinePony(target);
			pony.addNote("added note",name,message);
			pony.save();
			sendMessage(sender,"Note added!");
			return true;
		}
		if(args.length<1){
			sendMessage(sender, notEnoughArgs);
			return true;
		}
		String cmdn = cmd.getName();
		if(cmdn.equals("ban")){
			if(args.length<2){
				sendMessage(sender,notEnoughArgs);
				return false;
			}
			if(label.equals("banana")){
				final Player player = getSingleOnlinePlayer(args[0],sender);
				if(player == null){
					sendMessage(sender,"The banana command can only be used for online targets!");
					return true;
				} else {
					String reason = getStringFromIndex(1,args);
					if(!(sender instanceof Player)){
						toTheMoonahBeyech(player,reason,sender);
						return true;
					}
					final Player mod = (Player)sender;
					final String finalreason = reason;
					Bukkit.getScheduler().scheduleAsyncDelayedTask(Mane.getInstance(), new Runnable(){
						public void run(){
							String answer = questioner.ask(mod,pinkieSays+"Are you sure you wish to banana this player?","yes","no");
							if(answer.equals("yes")){
								Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
									public void run(){
										toTheMoonahBeyech(player, finalreason,sender);
									}
								});
							}
						}
					});
					return true;
				}
			}
			String target = getSinglePlayer(args[0],sender);
			if(target == null){
				return true;
			}
			target = getSinglePlayer(args[0], sender);
			Pony pony = Ponyville.getOfflinePony(target);
			String reason = getStringFromIndex(1,args);
			if(pony.isBanned()){
				if(pony.getBanType() != 1){
					sendMessage(sender,"That player is already banned");
					return true;
				} else {
					sendMessage(sender,"Upgrading tempban to permaban.");
					pony.addNote("upgraded tempban to permaban",sender.getName(),reason);
					pony.setUnbanTime(0);
				}
			} else {
				pony.addNote("banned",sender.getName(),reason);
			}
			pony.setBanned(true);
			pony.setBanType(2);
			pony.save();
			OfflinePlayer targetPO = Bukkit.getOfflinePlayer(pony.getName());
			targetPO.setBanned(true);
			if(targetPO.isOnline()){
				Player player = (Player)targetPO;
				player.kickPlayer(banmsg);
			} else {
				broadcastMessage(ChatColor.DARK_RED+pony.getNickname()+ChatColor.DARK_RED+" was sent to the MOON! (ban)");
			}
			logAdmin(sender,"banned "+targetPO.getName()+" for "+reason);
			return true;
		}
		if(cmdn.equals("kick")){
			Player target = getSingleOnlinePlayer(args[0],sender);
			if(target == null){
				return true;
			}
			String kickedby = "Kicked by "+sender.getName();
			if(args.length<2){
				sendMessage(sender,notEnoughArgs);
				return false;
			}
			String reason = "";
			for (int i = 1; i < args.length; i++) {
				reason+= args[i]+" ";
			}
			Pony pony = Ponyville.getPony(target);
			pony.addNote("kicked",sender.getName(),reason);
			pony.save();
			target.kickPlayer(kickedby+" for: "+reason);
			logAdmin(sender,"kicked "+target.getName()+" for "+reason);
			return true;
		}
		if(cmdn.equals("tempban")){
			String target = getSinglePlayer(args[0],sender);
			if(target == null){
				return true;
			}
			OfflinePlayer op = Bukkit.getOfflinePlayer(target);
			if(op.isBanned()){
				sendMessage(sender,"That player is already banned");
				return true;
			}
			if(args.length<3){
				sendMessage(sender,notEnoughArgs);
				return false;
			}
			long time;
			try {
				time = Long.parseLong(args[1]);
			} catch (NumberFormatException e) {
				sendMessage(sender,"Invalid number: \""+args[1]+"\"");
				return true;
			} 
			long systime = System.currentTimeMillis();
			time = time*60*1000*60;
			time = time+systime;
			Pony pony = Ponyville.getOfflinePony(target);
			pony.setBanned(true);
			pony.setBanType(1);
			pony.addNote("Tempbanned",sender.getName(),getStringFromIndex(2,args));
			pony.setUnbanTime(time);
			op.setBanned(true);
			pony.save();
			if(op.isOnline()){
				Player player = (Player)op;
				player.kickPlayer("You are tempbanned! Rejoin at " +Utils.getSystemTime(time));
			} else {
				broadcastMessage(ChatColor.DARK_RED+pony.getNickname()+ChatColor.DARK_RED+" was send to the Everfree Forest! (tempban)");
			}
			logAdmin(sender,"Tempbanned "+pony.getNickname()+" for "+args[1] + " hours.");
			return true;
		}
		if(cmdn.equals("unban")){
			String target = getSinglePlayer(args[0],sender);
			if(target == null){
				return true;
			}
			OfflinePlayer targetPO = Bukkit.getOfflinePlayer(target);
			if(targetPO.isBanned()){
				targetPO.setBanned(false);
				Pony pony = Ponyville.getOfflinePony(targetPO.getName());
				pony.setBanned(false);
				pony.setBanType(0);
				pony.setUnbanTime(0);
				pony.addNote("unbanned",sender.getName(),null);
				pony.save();
				broadcastMessage(ChatColor.GREEN+pony.getNickname()+" was Loved and Tolerated by "+sender.getName());
				logAdmin(sender,"unbanned "+pony.getName());
				return true;
			} else {
				sendMessage(sender,"Player is not banned");
				return true;
			}
		}
		return false;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeny(PlayerLoginEvent event){
		if(event.getPlayer().isBanned()){
			Pony pony = Ponyville.getOfflinePony(event.getPlayer());
			if(pony == null){
				System.out.println("null");
				event.disallow(Result.KICK_BANNED, banmsg);
				return;
			} else {
				int banReason = pony.getBanType();
				System.out.println(banReason);
				if(banReason == 2){
					event.disallow(Result.KICK_BANNED, banmsg);
					return;
				} else if(banReason == 1){
					long systemTime = Calendar.getInstance().getTime().getTime();
					long unbanTime = pony.getUnbanTime();
					if(unbanTime > systemTime){
						event.disallow(Result.KICK_BANNED,"You are tempbanned! Rejoin at " +Utils.getSystemTime(unbanTime));
						return;
					} else {
						event.getPlayer().setBanned(false);
						pony.setUnbanTime(0);
						pony.setBanned(false);
						pony.setBanType(0);
						event.allow();
						pony.save();
						PonyLogger.logAdmin("Console","unbanned "+event.getPlayer().getName()+". Tempban was timed out.");
						return;
					}
				} else {
					Bukkit.getLogger().warning(pinkieSays+"Player config ban error, is the player not banned? Player: "+event.getPlayer().getName()+". Expected ban, got \""+banReason+"\"");
					event.disallow(Result.KICK_BANNED, banmsg);
					return;
				}
			}
		}
	}
	@EventHandler
	public void onkick(PlayerKickEvent event){
		Pony pony = Ponyville.getPony(event.getPlayer());
		if(event.getPlayer().isBanned()){
			if(pony.getBanType() == 1){
				event.setLeaveMessage(ChatColor.DARK_RED+event.getPlayer().getDisplayName()+ChatColor.DARK_RED+" was sent to the Everfree Forest! (tempban)");
				return;
			} else{
				event.setLeaveMessage(ChatColor.DARK_RED+event.getPlayer().getDisplayName()+ChatColor.DARK_RED+" was sent to the MOON! (ban)");
			}
		} else {
			if(event.getReason().equals("Flying is not enabled on this server")){
				PonyLogger.logAdmin("Console", "Auto-kicked "+event.getPlayer().getName()+" for flying");
				pony.addNote("autokicked for flying","CONSOLE",null);
				pony.save();
				event.setLeaveMessage(ChatColor.DARK_RED+event.getPlayer().getDisplayName()+ChatColor.DARK_RED+" was sent to the Everfree Forest! (auto-kick (flying))");
				return;
			} else if(event.getReason().equals("You logged in from another location")){
				event.setLeaveMessage(ChatColor.AQUA+event.getPlayer().getDisplayName()+ChatColor.AQUA+" has left Equestria!");
				return;
			} else if(event.getReason().equals("The server is unavailable right now, Please try again later!")){
				event.setLeaveMessage(null);
				return;
			}
			event.setLeaveMessage(ChatColor.DARK_RED+event.getPlayer().getDisplayName()+ChatColor.DARK_RED+" was sent to the Everfree Forest! (kick)");
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(QuitEvent event){
		if(mooned.contains(event.getPlayer().getName())){
			event.setQuitMessage(null);
		}
	}
	private void toTheMoonahBeyech(final Player player, final String reason, final CommandSender sender){
		mooned.add(player.getName());
		Pony pony = Ponyville.getPony(player);
		pony.addNote("banana'd",sender.getName(),reason);
		pony.setBanned(true);
		pony.setBanType(2);
		player.setBanned(true);
		pony.save();
		logAdmin(sender,"banned "+player.getName()+" for "+reason);
		String celly = "Princess Celestia: "+ChatColor.GOLD;
		broadcastMessage(celly+"Psst! Hey hey! "+player.getDisplayName()+ChatColor.GOLD+"! I got a question for you!");
		final String two = celly+"So hey I was just wondering...";
		final String three = celly+"Uh, Do you... hehe... Do you like mmmmbananas?";
		final String five = celly + "Are you an mmmmbeeyetch who likes mmmmbananas?";
		final String seven = celly + "Undecided eh?";
		final String nine = celly + "Well thats okay.. cuz you can find out...";
		final String eleven = celly + "ON THE MOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOONAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHHHHHH BEE-YETCH!";
		BukkitScheduler s = Bukkit.getScheduler();
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				broadcastMessage(two);
			}},20*6);
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				broadcastMessage(three);
			}},20*9);
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				broadcastMessage(five);
			}},20*15);
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				broadcastMessage(seven);
			}},20*21);
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				broadcastMessage(nine);
			}},20*27);
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				broadcastMessage(eleven);
			}},20*30);
		s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run() {
				mooned.remove(player.getName());
				if(player.isOnline()){
					player.kickPlayer(banmsg);
				} else {
					broadcastMessage(ChatColor.DARK_RED+player.getDisplayName()+ChatColor.DARK_RED+" was sent to the MOON! (ban)");
				}
			}},20*31);
	}
}
