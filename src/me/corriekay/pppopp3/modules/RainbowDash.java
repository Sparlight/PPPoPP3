package me.corriekay.pppopp3.modules;

import java.util.HashSet;

import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class RainbowDash extends PSCmdExe{
	private static String rainbowSays = "§cR§6a§ei§an§9b§5o§cw §6D§ea§as§9h" + ChatColor.AQUA + ": ";
	private HashSet<String> zeuses = new HashSet<String>();

	public RainbowDash(){
		super("RainbowDash", "weather", "zeus", "time");
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("zeus")) {
			Player player;
			if(!(sender instanceof Player)) {
				sendMessage(sender, notPlayer);
				return true;
			}
			player = (Player)sender;
			String name = player.getName();
			if(zeuses.contains(name)) {
				zeuses.remove(name);
				sendMessage(player, "Lightning disabled!");
			} else {
				zeuses.add(name);
				sendMessage(player, "Lightning enabled!");
			}
			return true;
		}
		if(cmd.getName().equals("time")) {
			World world;
			if(args.length > 0) {
				world = Bukkit.getServer().getWorld(args[0]);
				if(world == null) {
					sendMessage(sender, "World not found!");
					return true;
				}
			} else if(!(sender instanceof Player)) {
				sendMessage(sender, "No world specified. Please specify a world!");
				return false;
			} else {
				world = ((Player)sender).getWorld();
			}
			long time = world.getTime() + 6000;
			if(time > 24000) {
				time = time - 24000;
			}
			String half = "am";
			if(time > 12000) {
				half = "pm";
				time = time - 12000;
			}
			int minutes = (int)Math.floor(time / 16.66);
			int hour = 0;
			while(minutes > 59) {
				hour++;
				minutes = minutes - 60;
			}
			if(hour == 0) {
				hour = 12;
			}
			String minuteString = minutes + "";
			if(minuteString.length() == 1) {
				if(minuteString.equals("0")) {
					minuteString = "00";
				} else {
					minuteString = "0" + minuteString;
				}
			}
			sendMessage(sender, "The time is " + hour + ":" + minuteString + " " + half);
			return true;
		}
		World world = Bukkit.getWorld("world");
		if(label.equals("rain")) {
			world.setWeatherDuration(10 * 60 * 20);
			world.setStorm(true);
			sender.sendMessage(rainbowSays + "A rainstorm? alright! Lemme just get out my rainclouds!");
			return true;
		}
		if(label.equals("storm")) {
			world.setWeatherDuration(10 * 60 * 20);
			world.setStorm(true);
			world.setThundering(true);
			world.setThunderDuration(10 * 60 * 20);
			sender.sendMessage(rainbowSays + "I LOVE THUNDERSTORMS! Heres a good one!");
			return true;
		}
		if(label.equals("sun")) {
			world.setWeatherDuration(10 * 60 * 20);
			world.setThundering(false);
			world.setStorm(false);
			sender.sendMessage(rainbowSays + "Weather set to sun! Ill clear the skies in ten seconds flat!");
			return true;
		}
		if(label.equals("day")) {
			world.setTime(6000);
			sender.sendMessage("Princess Celestia: " + ChatColor.GOLD + "Let there be light!");
			return true;
		}
		if(label.equals("night")) {
			world.setTime(18000);
			sender.sendMessage(ChatColor.BLUE + "Princess Luna: " + ChatColor.DARK_BLUE + "I shall bring forth the moon!");
			return true;
		} else {
			sendMessage(sender, "Invalid weather setting!");
			return true;
		}
	}

	@EventHandler
	public void weatherChange(WeatherChangeEvent event){
		if(event.getWorld().getName().equals("equestria")) {
			if(event.toWeatherState()) {
				event.setCancelled(true);
				event.getWorld().setStorm(false);
				event.getWorld().setThundering(false);
			}
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event){
		if(zeuses.contains(event.getPlayer().getName())) {
			if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				event.setCancelled(true);
				Location loc = event.getPlayer().getTargetBlock(null, 100).getLocation();
				loc.getWorld().strikeLightningEffect(loc);
			}
		}
	}
}
