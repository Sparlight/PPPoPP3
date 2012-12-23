package me.corriekay.pppopp3.modules;

import java.util.ArrayList;

import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiscCommands extends PSCmdExe{

	public MiscCommands(){
		super("MiscCommands", "nick", "reloadnicks", "whopony", "clearinventory");
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("nick")) {
			Player player;
			if(sender instanceof Player) {
				player = (Player)sender;
			} else {
				sendMessage(sender, notPlayer);
				return true;
			}
			if(args.length < 1) {
				sendMessage(player, notEnoughArgs);
				return true;
			}
			String nickname = "";
			for(String s : args) {
				nickname += s + " ";
			}
			nickname = nickname.substring(0, nickname.length() - 1);
			nickname = nickname.replace("&k", "");
			if(!player.hasPermission("pppopp3.formattednickname")) {
				nickname = nickname.replace("&l", "").replace("&m", "").replace("&n", "").replace("&o", "").replace("&0", "").replace("&1", "");
			}
			nickname = nickname.replaceAll("[^(a-zA-Z0-9 !?.&~)]", "");
			if(player.getName().equals("Acidogenic")) {
				nickname = nickname.replace("&0", "");
			}
			nickname = ChatColor.translateAlternateColorCodes('&', nickname);
			if(ChatColor.stripColor(nickname).length() > 20) {
				sendMessage(player, "Boy, thats a long nickname! You should shorten it a bit!");
				return true;
			}
			if(ChatColor.stripColor(nickname).length() < 1) {
				sendMessage(player, "Boy, thats a short nickname! You should lengthen it a bit!");
				return true;
			}
			Pony pony = Ponyville.getPony(player);
			ArrayList<String> nicks = pony.getNickHistory();
			String oldnick = pony.getNickname();
			if(!nicks.contains(oldnick)) {
				nicks.add(oldnick);
			}
			pony.setNickname(nickname);
			pony.setNickHistory(nicks);
			pony.save();
			player.setDisplayName(nickname);
			sendMessage(player, "Nickname changed, " + player.getDisplayName() + ChatColor.LIGHT_PURPLE + "!");
			return true;
		}
		if(cmd.getName().equals("whopony")) {
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			ArrayList<Player> players = new ArrayList<Player>();
			for(Player p : Bukkit.getOnlinePlayers()) {
				String nick = ChatColor.stripColor(p.getDisplayName()).toLowerCase();
				String name = p.getName().toLowerCase();
				String arg = args[0];
				if(nick.contains(arg) || name.contains(arg)) {
					if(!InvisibilityHandler.ih.isHidden(p.getName()) || sender.hasPermission("pppopp2.seehidden")) {
						players.add(p);
					}
				}
			}
			if(players.size() < 1) {
				sendMessage(sender, "I looked high and low, but I couldnt find that pony! :C");
				return true;
			}
			for(Player p : players) {
				sendMessage(sender, "Silly, " + p.getDisplayName() + ChatColor.LIGHT_PURPLE + " is " + p.getName() + "!");
			}
			return true;
		}
		if(cmd.getName().equals("clearinventory")) {
			if(sender instanceof Player) {
				((Player)sender).getInventory().clear();
				sendMessage(sender, "Your inventory was sent to the mooooonaaah!!");
				return true;
			} else {
				sendMessage(sender, notPlayer);
				return true;
			}
		}
		return true;
	}
}
