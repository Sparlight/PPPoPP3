package me.corriekay.pppopp3.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrivateMessageEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	private final Player sender, reciever;
	private String message;
	private boolean cancelled = false;

	public PrivateMessageEvent(Player sender, Player reciever, String message){
		this.sender = sender;
		this.reciever = reciever;
		this.message = message;
	}

	@Override
	public HandlerList getHandlers(){
		return handlers;
	}

	public void setMessage(String msg){
		message = msg;
	}

	public static HandlerList getHandlerList(){
		return handlers;
	}

	public String getMsg(){
		return message;
	}

	public Player getSender(){
		return sender;
	}

	public Player getReciever(){
		return reciever;
	}

	public boolean isCancelled(){
		return cancelled;
	}

	public void setCancelled(boolean iscancel){
		cancelled = iscancel;
	}

}
