package me.corriekay.pppopp3.events;

import net.minecraft.server.v1_4_5.Packet3Chat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRecieveMessageEvent extends Event implements Cancellable{
	private final static HandlerList handlers = new HandlerList();
	private Packet3Chat packet;
	private String message;
	private final String pName;
	private boolean cancel = false;

	public PlayerRecieveMessageEvent(Packet3Chat packet, String name){
		this.packet = packet;
		this.message = packet.message;
		pName = name;
	}

	public boolean isCancelled(){
		return this.cancel;
	}

	public void setCancelled(boolean cancel){
		this.cancel = cancel;
	}

	public boolean isServer(){
		return this.packet.isServer();
	}

	public String getMessage(){
		return this.message;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public HandlerList getHandlers(){
		return PlayerRecieveMessageEvent.handlers;
	}

	public static HandlerList getHandlerList(){
		return PlayerRecieveMessageEvent.handlers;
	}

	public String getName(){
		return pName;
	}
}
