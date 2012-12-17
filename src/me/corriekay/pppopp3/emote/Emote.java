package me.corriekay.pppopp3.emote;

import java.util.ArrayList;

import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.rpa.RemotePonyAdmin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Emote {

	protected final String name;
	protected final String sender;
	protected final String receiver;
	protected final String server;
	protected final String ownerName;
	protected final boolean isSilent;
	protected final boolean isPrivate;
	protected final boolean global;
	/**
	 * 
	 * @param sender
	 * @param receiver
	 * @param server
	 * @param isSilent
	 * @param isPrivate
	 * @param ownerName
	 * @param name
	 * @param global
	 */
	public Emote(String sender, String receiver, String server, boolean isSilent, boolean isPrivate, String ownerName, String name, boolean global){
		this.name = name;
		this.sender = parseColors(sender);
		this.receiver = parseColors(receiver);
		this.server = parseColors(server);
		this.ownerName = ownerName;
		this.isSilent = isSilent;
		this.isPrivate = isPrivate;
		this.global = global;
	}
	public Emote(Pony pony){
		this(pony.getEmoteSender(),pony.getEmoteReceiver(),pony.getEmoteServer(),pony.getEmoteSilent(),pony.getEmotePrivate(),pony.getName(),pony.getEmoteName(), false);
	}
	protected static String parseColors(String string){
		String string2 = "";

		string = string.replaceAll("<", " <");
		string = string.replaceAll(">", "> "); 
		string.replace("  ", " ");
		String[] array = string.trim().split(" ");
		String color = "<white>";
		for(int i = 0; i<array.length; i++){
			if(array[i].equals("<r>")||array[i].equals("<s>")){
				array[i] = array[i]+" "+color;
			} else if (array[i].startsWith("<")&&array[i].endsWith(">")){
				color = array[i];
			}
		}
		for(String words : array){
			string2 += words;
			if(!(words.startsWith("<")&&words.endsWith(">"))&&!words.equals("")){
				string2+=" ";
			}
			if((words.equals("<r>")||words.equals("<s>"))){
				if (!words.equals("")) {
					string2 += " ";
				}
			}
		}

		string2.replaceAll("  "," ");

		return string2.trim();
	}
	public static String format(String string, String sender, String reciever) {
	    String s = string;
	    for (ChatColor color : ChatColor.values()) {
	        s = s.replaceAll("(?i)<" + color.name() + ">", "" + color);
	    }
	    s = s.replaceAll("<s>", sender);
	    s = s.replaceAll("<r>", reciever);
	    s = s.replaceAll("  ", " ");
	    return s;
	}	
	protected void useEmote(Player sender, Player reciever){
		ArrayList<Player> players = new ArrayList<Player>();
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			players.add(player);
		}
		players.remove(sender);
		players.remove(reciever);
		if (!isSilent) {
			for (Player player : players) {
				player.sendMessage(format(server, sender.getDisplayName(),
						reciever.getDisplayName()));
			}
		}
		Bukkit.getConsoleSender().sendMessage(format(server, sender.getDisplayName(),
						reciever.getDisplayName()));
		RemotePonyAdmin.rpa.message(ChatColor.stripColor(format(server, sender.getDisplayName(),
						reciever.getDisplayName())));
		sender.sendMessage(format(this.sender,sender.getDisplayName(),reciever.getDisplayName()));
		reciever.sendMessage(format(this.receiver,sender.getDisplayName(),reciever.getDisplayName()));
	}
}