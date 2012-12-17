package me.corriekay.pppopp3.emote;

import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EmoteSetWizard {

	private final String name;
	/**
	 * the stage is what will be set when they type into the chat.
	 * stage 0 is emote name. this is what the user types in /mee <emoteName> <target>
	 * stage 1 is sender
	 * stage 2 is receiver
	 * stage 3 is server
	 * satage 4 is private
	 * stage 5 is silent
	 */
	private int stage = 0;
	private boolean exit = false;
	private final ChatColor gray = ChatColor.GRAY;
	private final Pony pony;

	public EmoteSetWizard(Player player){
		name = player.getName();
		pony = Ponyville.getPony(player);
		player.sendMessage(ChatColor.DARK_GRAY+"===================");
		player.sendMessage(gray+"Welcome, "+name+", to the MLE Setup assistant! This module is designed to help streamline, and/or otherwise make setting up your emote to be REALLY easy! "+ChatColor.DARK_RED+"Warning: do not create any emote that may be viewed as unsavory, violent, rude, or otherwise breaks any server rule. You can be banned from using MLE. You have been warned.");
		player.sendMessage(ChatColor.RED+"say \"exit\" at any time to leave the assistant. Type \"help\" to repeat what stage in the assistant youre at. Type \"colors\" for a list of colors.  Type \"view\" to see your emote so far! Type \"skip\" to skip to the next part of the emote! If youve made a mistake, and wish to go back, type \"back\". To control the assistant, just type without forward slashes, it is controlled through talking, not commands.");
		player.sendMessage(gray+"To get started, lets set the name of your emote!  The name of your emote is what players type in when they use it. /mee <emoteName> <target>. Type anything you want:");
	}
	protected void onChat(String message, Player player){
		if(exit||stage>=6){
			exit(player);
			return;
		}
		String[] args = message.split(" ");
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("colors")) {
				player.sendMessage(ChatColor.DARK_GRAY+"===================");
				player.sendMessage(gray+ "Here are a list of colors you can use.");
				String colors = "";
				for (ChatColor color : ChatColor.values()) {
					colors += color + color.name() + ", " + ChatColor.RESET;
				}
				player.sendMessage(colors);
				player.sendMessage(gray+ "To use a color, type \"<colorname>\" in the sentance, and all words after it, will have the same color, until you override it with another color.");player.sendMessage(gray+ "The funky looking one is \"magic\". Try not to use it. its rather annoying. RESET will wipe all formatting, and reset back to just generic white.");
				return;
			}
			if (args[0].equals("help")) {
				player.sendMessage(ChatColor.DARK_GRAY+"===================");
				player.sendMessage(ChatColor.RED+"say \"exit\" at any time to leave the assistant. Type \"help\" to repeat what stage in the assistant youre at. Type \"colors\" for a list of colors.  Type \"view\" to see your emote so far! Type \"skip\" to skip to the next part of the emote! If youve made a mistake, and wish to go back, type \"back\". To control the assistant, just type without forward slashes, it is controlled through talking, not commands.");
				switch (stage) {
					case 0:{
						player.sendMessage(gray+"Setting \"name\"");
						break;
					}
					case 1:{
						player.sendMessage(gray+"Setting \"sender\"");
						break;
					}
					case 2:{
						player.sendMessage(gray+"Setting \"receiver\"");
						break;
					}
					case 3:{
						player.sendMessage(gray+"Setting \"server\"");
						break;
					}
					case 4:{
						player.sendMessage(gray+"Setting \"private\"");
						break;
					}
					case 5:{
						player.sendMessage(gray+"Setting \"silent\"");
						break;
					}
				}
				return;
			}
			if(args[0].equals("view")){
				player.sendMessage(ChatColor.DARK_GRAY+"===================");
				view(player);
				return;
			}
			if(args[0].equals("exit")){
				exit(player);
				return;
			}
			if(args[0].equals("skip")){
				player.sendMessage(ChatColor.DARK_GRAY+"===================");
				stage++;
				next(player);
				return;
			}
			if(args[0].equals("back")){
				if(stage != 0){
					stage--;
				}
				next(player);
				return;
			}
		}
		switch (stage) {
			case 0:{
				setName(args[0],player);
				break;
			}
			case 1:{
				setSender(args,player);
				break;
			}
			case 2:{
				setReceiver(args,player);
				break;
			}
			case 3:{
				setServer(args,player);
				break;
			}
			case 4:{
				setPrivate(args[0],player);
				break;
			}
			case 5:{
				setSilent(args[0],player);
				break;
			}
			default:{
				exit(player);
			}
		}
		if(exit){
			exit(player);
			return;
		}
	}
	private void setName(String arg,Player player){
		if(MineLittleEmote.emoteHandler.emoteList.containsKey(arg)){
			player.sendMessage(gray+"This emote name is already in use! If you own the emote, No need to set it twice! Type \"skip\" to skip this part!");
			return;
		}
		player.sendMessage(gray+"Your emotes name is now set to: "+ChatColor.RED+arg+gray+". Whenever a player wants to utilize your emote, they will have to type /mee "+arg+" <target>");
		pony.setEmoteName(arg);
		pony.save();
		stage++;
		next(player);
	}
	private void setSender(String[] args,Player player){
		String emoteString = concat(args);
		pony.setEmoteSender(emoteString);
		pony.save();
		player.sendMessage(gray+"Awesome, your sender message is set to:");
		player.sendMessage(Emote.format(Emote.parseColors(emoteString), "sender", "receiver"));
		stage++;
		next(player);
	}
	private void setReceiver(String[] args,Player player){
		String emoteString = concat(args);
		pony.setEmoteReceiver(emoteString);
		pony.save();
		player.sendMessage(gray+"Awesome, your recevier message is set to:");
		player.sendMessage(Emote.format(Emote.parseColors(emoteString), "sender", "receiver"));
		stage++;
		next(player);
	}
	private void setServer(String[] args,Player player){
		String emoteString = concat(args);
		pony.setEmoteServer(emoteString);
		pony.save();
		player.sendMessage(gray+"Awesome, your server message is set to:");
		player.sendMessage(Emote.format(Emote.parseColors(emoteString), "sender", "receiver"));
		stage++;
		next(player);
	}
	private void setPrivate(String arg,Player player){
		boolean argBool;
		try {
			argBool = Boolean.parseBoolean(arg);
		} catch (Exception e) {
			player.sendMessage(gray+"Sorry, that doesnt seem to be either true or false. Please type \"true\" or \"false\", thank you!");
			return;
		}
		pony.setEmotePrivate(argBool);
		pony.save();
		player.sendMessage(gray+"Awesome, your emote is set to privacy: "+argBool);
		stage++;
		next(player);
	}
	private void setSilent(String arg,Player player){
		boolean argBool;
		try {
			argBool = Boolean.parseBoolean(arg);
		} catch (Exception e) {
			player.sendMessage(gray+"Sorry, that doesnt seem to be either true or false. Please type \"true\" or \"false\", thank you!");
			return;
		}
		pony.setEmoteSilent(argBool);
		pony.save();
		player.sendMessage(gray+"Awesome, your emote is set to silent: "+argBool);
		exit(player);
	}
	private void exit(Player player){
		player.sendMessage("Emote Finished! Heres what it looks like: ");
		view(player);
		MineLittleEmote.endWizard(name);
	}
	private void next(Player player){
		switch (stage){
		case 0:
			player.sendMessage(ChatColor.DARK_GRAY+"===================");
			player.sendMessage(gray+"Time to set your emotes Name! The name of your emote is what players type in when they use it. /mee <emoteName> <target>"); break;
		case 1: 
			player.sendMessage(ChatColor.DARK_GRAY+"===================");
			player.sendMessage(gray+"Time to set your sender text! The sender text is the message whoever is using your emote will see! \"<r>\" and \"<s>\" will be replaced automatically when used with the receiver and senders names, respectively."); break;
		case 2: 
			player.sendMessage(ChatColor.DARK_GRAY+"===================");
			player.sendMessage(gray+"Time to set your receiver text! The receiver text is the message that the target will recieve! \"<r>\" and \"<s>\" will be replaced automatically when used with the receiver and senders names, respectively."); break;
		case 3: 
			player.sendMessage(ChatColor.DARK_GRAY+"===================");
			player.sendMessage(gray+"Time to set the server message! The server text is the message that the rest of the server, besides you and the target, will see! \"<r>\" and \"<s>\" will be replaced automatically when used with the receiver and senders names, respectively."); break;
		case 4: 
			player.sendMessage(ChatColor.DARK_GRAY+"===================");
			player.sendMessage(gray+"Time to set your emotes private status! Type either true, or false. If your emote is private, only you can use it!"); break;
		case 5: 
			player.sendMessage(ChatColor.DARK_GRAY+"===================");
			player.sendMessage(gray+"Time to set your emotes silent status! Type either true, or false. IF your emote is silent, only the sender and receiever may see it, but not the rest of the server!"); break;
		}
	}
	private String concat(String[] words){
		String sentance = "";
		for(String word : words){
			sentance+=word+" ";
		}
		return sentance.trim();
	}
	private void view(Player player){
		player.sendMessage(gray+"Emote Name: "+pony.getEmoteName());
		player.sendMessage(gray+"Emote Sender: "+Emote.format(Emote.parseColors(pony.getEmoteSender()), "sender", "receiver"));
		player.sendMessage(gray+"Emote Receiver: "+Emote.format(Emote.parseColors(pony.getEmoteReceiver()), "sender", "receiver"));
		player.sendMessage(gray+"Emote Server: "+Emote.format(Emote.parseColors(pony.getEmoteServer()), "sender", "receiver"));
		player.sendMessage(gray+"Is Private: "+pony.getEmotePrivate());
		player.sendMessage(gray+"Is Silent: "+pony.getEmoteSilent());
	}
}