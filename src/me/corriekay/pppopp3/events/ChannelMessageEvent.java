package me.corriekay.pppopp3.events;

import net.minecraft.server.Packet3Chat;


public class ChannelMessageEvent extends PlayerRecieveMessageEvent{
	private String whosent, message, channame;
	public ChannelMessageEvent(Packet3Chat packet, String toWho ,String whoSent,String message, String chanName) {
		super(packet, toWho);
		setWhosent(whoSent);
		this.message = message;
		setChanname(chanName);
	}
	public String getWhosent() {
		return whosent;
	}
	public void setWhosent(String whosent) {
		this.whosent = whosent;
	}
	public String getMessage(){
		return message;
	}
	public void setMessage(String msg){
		message = msg;
	}
	public String getChanname() {
		return channame;
	}
	private void setChanname(String channame) {
		this.channame = channame;
	}
}
