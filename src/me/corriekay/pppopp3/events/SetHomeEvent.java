package me.corriekay.pppopp3.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SetHomeEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Location loc;
	private final Player player;
	
	public SetHomeEvent(Player player, Location loc){
		this.loc = loc;
		this.player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public Location getLoc() {
		return loc;
	}

}
