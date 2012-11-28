package me.corriekay.pppopp3.events;

import me.corriekay.pppopp3.ponyville.Pony;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class JoinEvent extends Event implements Cancellable{

	private boolean cancelled = false;
	private final boolean joining;
	private final Pony pony;
	private final Player player;
	private static final HandlerList handlers = new HandlerList();
	private String joinMessage;
	private final String jmDef;

	public JoinEvent(Player player, Pony pony, boolean isJoining){
		joining = isJoining;
		jmDef = ChatColor.RED+pony.getNickname()+ChatColor.AQUA+" has returned to Equestria!";
		joinMessage = jmDef;
		this.pony = pony;
		this.player = player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		if(arg0){
			joinMessage = null;
		} else {
			joinMessage = jmDef;
		}
		cancelled = arg0;
	}
	
	public boolean isJoining(){
		return joining;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}

	public void setJoinMessage(String msg){
		joinMessage = msg;
	}
	public String getMsg(){
		return joinMessage;
	}
	public Pony getPony(){
		return pony;
	}

	public Player getPlayer() {
		return player;
	}
}
