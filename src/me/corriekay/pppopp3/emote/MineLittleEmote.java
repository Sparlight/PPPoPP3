package me.corriekay.pppopp3.emote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MineLittleEmote extends PSCmdExe{

	protected static MineLittleEmote emoteHandler;
	private static HashMap<String,EmoteSetWizard> emoteSetupList = new HashMap<String,EmoteSetWizard>();
	protected static FileConfiguration emoteConfig = getNamedConfig("emotes.yml");
	protected HashMap<String,Emote> emoteList;
	private ArrayList<String> banned = new ArrayList<String>();

	public MineLittleEmote(){
		super("MineLittleEmote", "mle", "mles", "mee", "deleteemote", "mleban");
		emoteHandler = this;
		reloadEmotes();
		reloadBanned();
	}

	private void reloadBanned(){
		banned = (ArrayList<String>)emoteConfig.getStringList("banned");
	}

	protected void reloadEmotes(){
		emoteList = new HashMap<String,Emote>();
		for(String emoteName : emoteConfig.getConfigurationSection("global").getKeys(false)) {
			emoteList.put(emoteName, new Emote(emoteConfig.getString("global." + emoteName + ".sender"), emoteConfig.getString("global." + emoteName + ".receiver"), emoteConfig.getString("global." + emoteName + ".server"), false, false, "server", emoteName, true));
		}
		List<String> registeredEmotes = emoteConfig.getStringList("user");
		for(String player : emoteConfig.getStringList("user")) {
			try {
				Pony pony = Ponyville.getOfflinePony(player);
				Emote emote = new Emote(pony);
				if(!emote.name.equals("none")) {
					emoteList.put(pony.getEmoteName(), emote);
				} else {
					registeredEmotes.remove(player);
				}
			} catch(Exception e) {
				Bukkit.getLogger().warning("Issue loading player emote: " + emoteConfig);
			}
		}
		emoteConfig.set("user", registeredEmotes);
		saveConfig();
	}

	private static void saveConfig(){
		saveNamedConfig("emotes.yml", emoteConfig);
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)) {
			sendMessage(sender, notPlayer);
			return true;
		}
		Player player = (Player)sender;
		if(banned.contains(player.getName().toLowerCase())) {
			sendMessage(player, "Youve been banned from MLE!");
			return true;
		}
		if(cmd.getName().equals("mles")) {
			if(emoteSetupList.containsKey(player.getName())) {
				sendMessage(player, "Youre already utilizing the assistant silly! just type into chat without commands!");
				return true;
			}
			HashMap<String,Emote> newEmoteList = new HashMap<String,Emote>();
			for(Emote emote : emoteList.values()) {
				if(!emote.ownerName.equals(player.getName())) {
					newEmoteList.put(emote.name, emote);
				}
			}
			emoteList = newEmoteList;
			emoteSetupList.put(player.getName(), new EmoteSetWizard(player));
			return true;
		}
		if(cmd.getName().equals("mle")) {
			String globalEmotes = ChatColor.GRAY + "Global Emotes: ";
			String userEmotes = ChatColor.GRAY + "User Emotes: ";
			for(Emote emote : emoteList.values()) {
				if(emote.global) {
					globalEmotes += ChatColor.RED + emote.name + ", ";
				} else {
					if((!emote.isPrivate || player.getName().equals(emote.ownerName)) || player.isOp()) {
						userEmotes += ChatColor.RED + emote.name + ", ";
					}
				}
			}
			globalEmotes = globalEmotes.substring(0, globalEmotes.length() - 2);
			userEmotes = userEmotes.substring(0, userEmotes.length() - 2);
			player.sendMessage(globalEmotes);
			player.sendMessage(userEmotes);
			return true;
		}
		if(cmd.getName().equals("mee")) {
			if(args.length < 2) {
				sendMessage(player, "Not enough arguments!");
				return true;
			}
			Emote emote = emoteList.get(args[0]);
			if(emote == null) {
				sendMessage(player, "Aw shucks, i couldnt find that emote!");
				return true;
			}
			if((emote.isPrivate && !emote.ownerName.equals(player.getName())) && !player.isOp()) {
				sendMessage(player, "hey! how did you get that! put that down, you cant use that!");
				return true;
			} else {
				Player target = getOnlinePlayer(args[1], player);
				if(target == null) {
					return true;
				} else {
					emote.useEmote(player, target);
					return true;
				}
			}
		}
		if(cmd.getName().equals("deleteemote")) {
			List<String> registeredEmotes = emoteConfig.getStringList("user");
			registeredEmotes.remove(player.getName());
			emoteConfig.set("user", registeredEmotes);
			saveConfig();
			reloadEmotes();
			sendMessage(player, "awh, I thought that emote was pretty cool! D: oh well. Your emote was sent to the moon!");
			return true;
		}
		if(cmd.getName().equals("mleban")) {
			if(args.length == 0) {
				sendMessage(player, "Not enough arguments silly!");
				return true;
			}
			String playerName = args[0].toLowerCase();
			List<String> bannedPlayers = emoteConfig.getStringList("banned");
			bannedPlayers.add(playerName);
			emoteConfig.set("banned", bannedPlayers);
			saveConfig();
			reloadBanned();
			sendMessage(player, "Player banned!");
			return true;
		}
		return false;
	}

	protected static void endWizard(String name){
		emoteSetupList.remove(name);
		emoteHandler.reloadEmotes();
		List<String> emotes = emoteConfig.getStringList("user");
		if(!emotes.contains(name)) {
			emotes.add(name);
		}
		List<String> registeredEmotes = emoteConfig.getStringList("user");
		if(!registeredEmotes.contains(name)) {
			registeredEmotes.add(name);
		}
		emoteConfig.set("user", registeredEmotes);
		saveConfig();
		emoteHandler.reloadEmotes();
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event){
		if(emoteSetupList.containsKey(event.getPlayer().getName())) {
			EmoteSetWizard ems = emoteSetupList.get(event.getPlayer().getName());
			ems.onChat(event.getMessage(), event.getPlayer());
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()) {
			endWizard(event.getPlayer().getName());
		}
	}

}