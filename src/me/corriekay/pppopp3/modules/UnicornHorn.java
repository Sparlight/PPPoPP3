package me.corriekay.pppopp3.modules;

import java.util.HashMap;

import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class UnicornHorn extends PSCmdExe{

	private HashMap<String,PlayerMacros> macros = new HashMap<String,PlayerMacros>();

	public UnicornHorn(){
		super("UnicornHorn", "horn");
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.hasPermission("pppopp3.unicornhorn")) {
				macros.put(player.getName(), PlayerMacros.getMacros(player));
			}
		}
	}

	@EventHandler
	public void onJoin(JoinEvent event){
		if(event.isJoining()) {
			if(event.getPlayer().hasPermission("pppopp3.unicornhorn")) {
				macros.put(event.getPlayer().getName(), PlayerMacros.getMacros(event.getPlayer()));
			}
		}
	}

	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()) {
			macros.remove(event.getPlayer().getName());
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event){
		Player player = event.getPlayer();
		ItemStack s = player.getItemInHand();
		if(s == null) {
			return;
		}
		if(s.getType() != Material.STICK) {
			return;
		}
		if(player.hasPermission("pppopp3.unicornhorn")) {
			PlayerMacros macro = macros.get(player.getName());
			if(macro != null) {
				if(macro.isOn()) {
					Action a = event.getAction();
					if(a == Action.LEFT_CLICK_AIR || a == Action.LEFT_CLICK_BLOCK) {
						event.setCancelled(true);
						player.performCommand(macro.getLeft());
						return;
					}
					if(a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
						event.setCancelled(true);
						player.performCommand(macro.getRight());
						return;
					}
				}
				return;
			}
			macros.put(player.getName(), PlayerMacros.getMacros(player));
			sendMessage(player, "Uh oh, something went awry with your macro! I tried to regenerate it, so try again! If the issue persists, contact administration!");
		}
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player player;
		if(!(sender instanceof Player)) {
			sendMessage(sender, notPlayer);
			return true;
		}
		player = (Player)sender;
		if(cmd.getName().equals("horn")) {
			if(args.length < 1) {
				sendMessage(player, notEnoughArgs);
				return true;
			}
			if(args[0].equals("help")) {
				sendMessage(player, "Unicorn Horn is a plugin designed to let you bind commands to the left and right click actions of the stick.");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "type /horn on/off to turn the use of the plugin on or off.");
				player.sendMessage(ChatColor.LIGHT_PURPLE + "type /horn left/right \"command blah blah\" to set a command.");
				return true;
			}
			PlayerMacros macro = macros.get(player.getName());
			if(args[0].equals("on")) {
				sendMessage(player, "Horn on!");
				macro.setOn(true);
			}
			if(args[0].equals("off")) {
				sendMessage(player, "horn off!");
				macro.setOn(false);
			}
			if(args[0].equals("left") || args[0].equals("right")) {
				if(args.length < 2) {
					sendMessage(player, notEnoughArgs);
					return true;
				}
				String command = "";
				for(int i = 1; i < args.length; i++) {
					command += args[i] + " ";
				}
				if(args[0].equals("left")) {
					sendMessage(player, "Horn left set to: /" + command.trim() + "!");
					macro.setLeft(command.trim());
				}
				if(args[0].equals("right")) {
					sendMessage(player, "Horn right set to: /" + command.trim() + "!");
					macro.setRight(command.trim());
				}
			}
			macro.saveMacro();
			macros.put(player.getName(), PlayerMacros.getMacros(player));
			return true;
		}
		return true;

	}

	private static class PlayerMacros{

		private boolean on;
		String left;
		String right;
		String player;

		public PlayerMacros(String player, String left, String right, boolean on){
			this.on = on;
			this.left = left;
			this.right = right;
			this.player = player;
		}

		public String getLeft(){
			return left;
		}

		public void setLeft(String left){
			this.left = left;
		}

		public String getRight(){
			return right;
		}

		public void setRight(String right){
			this.right = right;
		}

		public void setOn(boolean on){
			this.on = on;
		}

		public boolean isOn(){
			return on;
		}

		public void saveMacro(){
			Pony pony = Ponyville.getPony(player);
			pony.setHornOn(on);
			pony.setHornLeft(left);
			pony.setHornRight(right);
			pony.save();
		}

		public static PlayerMacros getMacros(Player player){
			return getMacros(player.getName());
		}

		public static PlayerMacros getMacros(String player){
			Pony pony = Ponyville.getOfflinePony(player);
			String left, right;
			left = pony.getHornLeft();
			right = pony.getHornLeft();
			boolean on = pony.getHornOn();
			return new PlayerMacros(player, left, right, on);
		}
	}
}
