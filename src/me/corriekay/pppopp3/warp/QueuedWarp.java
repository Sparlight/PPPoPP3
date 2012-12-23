package me.corriekay.pppopp3.warp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class QueuedWarp{

	private String player;
	private Location teleport;
	private int countdown;

	public QueuedWarp(String player, Location teleport, int countdown) throws Exception{
		if(teleport == null) {
			throw new Exception();
		}
		this.player = player;
		this.teleport = teleport;
		this.countdown = countdown;
	}

	public boolean countdown(){
		countdown--;
		if(countdown <= 3 && countdown > 0) {
			Player player = Bukkit.getPlayerExact(this.player);
			if(player == null) {
				return true;
			} else {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Pinkie Pie: " + countdown + "...");
			}
		}
		if(countdown <= 0) {
			Player player = Bukkit.getPlayerExact(this.player);
			if(player == null) {
				return true;
			} else {
				List<Entity> entities = player.getNearbyEntities(5, 3, 5);
				for(Entity entity : entities) {
					if(entity instanceof Monster || entity instanceof Flying) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + "Oh no! Catastrophic failure! Warp aborted! Get these monsters off of me!!");
						return true;
					}
				}
				player.teleport(teleport);
				player.sendMessage(ChatColor.LIGHT_PURPLE + "Pinkie Pie: Success! Teleportation victory!");
				return true;
			}
		}
		return false;
	}
}
