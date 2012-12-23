package me.corriekay.pppopp3.modules;

import java.util.HashSet;

import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class GodHandler extends PSCmdExe{

	private final HashSet<String> invuln = new HashSet<String>();

	public GodHandler(){
		super("AdminHandler", "gm", "god");
		try {
			methodMap.put(EntityDamageByEntityEvent.class, this.getClass().getDeclaredMethod("dmgEvent", EntityDamageEvent.class));
			methodMap.put(EntityDamageByBlockEvent.class, this.getClass().getDeclaredMethod("dmgEvent", EntityDamageEvent.class));
		} catch(Exception e) {
			e.printStackTrace();
		}
		for(Player p : Bukkit.getOnlinePlayers()) {
			login(p, false);
		}
	}

	private void login(Player player, boolean notify){
		Pony pony = Ponyville.getPony(player);
		if(pony.isGodMode() && player.hasPermission("pppopp3.celestia")) {
			invuln.add(player.getName());
			if(notify) {
				sendMessage(player, "Yay! youre a pretty princess now!");
			}
		}
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender instanceof ConsoleCommandSender) {
			if(cmd.getName().equals("gm")) {
				if(args.length < 2) {
					sendMessage(sender, notEnoughArgs);
					return true;
				} else {
					Player player = getOnlinePlayer(args[0], sender);
					if(player == null) {
						return true;
					} else {
						int gmint;
						try {
							gmint = Integer.parseInt(args[1]);
						} catch(NumberFormatException e) {
							sendMessage(sender, "Thats not a number!");
							return true;
						}
						GameMode gm = GameMode.getByValue(gmint);
						if(gm == GameMode.CREATIVE) {
							player.setGameMode(gm);
						} else if(gm == GameMode.SURVIVAL) {
							player.setGameMode(gm);
						} else {
							sendMessage(sender, "We dont use that game mode!");
							return true;
						}
						sendMessage(sender, player.getDisplayName() + ChatColor.LIGHT_PURPLE + "'s game mode set to " + gm.name().toLowerCase() + "!");
						return true;
					}
				}
			}
		}
		Player player;
		if(sender instanceof Player) {
			player = (Player)sender;
		} else {
			sendMessage(sender, notPlayer);
			return true;
		}
		if(cmd.getName().equals("gm")) {
			GameMode gm = player.getGameMode();
			if(gm == GameMode.SURVIVAL) {
				player.setGameMode(GameMode.CREATIVE);
			} else {
				player.setGameMode(GameMode.SURVIVAL);
			}
			return true;
		}
		if(cmd.getName().equals("god")) {
			boolean isgod = invuln.contains(player.getName());
			Pony pony = Ponyville.getPony(player);
			if(isgod) {
				pony.setGodMode(false);
				invuln.remove(player.getName());
				sendMessage(player, "Awh, Okay! Taking off the crown now..");
			} else {
				pony.setGodMode(true);
				invuln.add(player.getName());
				sendMessage(player, "Yay! youre a pretty princess now!");
			}
			pony.save();
			return true;
		}
		return true;
	}

	@EventHandler
	public void onJoin(JoinEvent event){
		if(event.isJoining()) {
			login(event.getPlayer(), true);
		}
	}

	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()) {
			invuln.remove(event.getPlayer().getName());
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void dmgEvent(EntityDamageEvent event){
		if((event.getEntity() instanceof Player) && (invuln.contains(((Player)event.getEntity()).getName()))) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void foodChange(FoodLevelChangeEvent event){
		if((event.getEntity() instanceof Player) && (invuln.contains(((Player)event.getEntity()).getName()))) {
			event.setCancelled(true);
		}
	}
}
