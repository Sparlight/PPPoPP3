package me.corriekay.pppopp3.events;
/**
 * @Class: QuitEvent
 * @Author: CorrieKay
 * @Purpose: This event class is called whenever a player quits the game (real) or goes invisible (fake)
 */

import me.corriekay.pppopp3.ponyville.Pony;

import org.bukkit.ChatColor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QuitEvent extends Event implements Cancellable{
	
	private boolean cancelled = false;
	private final boolean quitting;
	private static final HandlerList handlers = new HandlerList();
	private String quitMessage;
	private final String qmDef;
	private Pony pony;
	
	public QuitEvent(Pony pony, boolean isQuitting){
		quitting = isQuitting;
		qmDef = ChatColor.RED+pony.getNickname()+ChatColor.AQUA+" has left Equestria!";
		quitMessage = qmDef;
		this.pony = pony;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList(){
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		if(arg0){
			setQuitMessage(null);
		} else {
			setQuitMessage(qmDef);
		}
		cancelled = arg0;
	}

	public Pony getPlayer(){
		return pony;
	}
	
	public boolean isQuitting(){
		return quitting;
	}

	public void setQuitMessage(String msg){
		quitMessage = msg;
	}
	public String getMsg(){
		return quitMessage;
	}
}
