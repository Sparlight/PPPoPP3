package me.corriekay.pppopp3.modules;

import static me.corriekay.pppopp3.ponymanager.PonyManager.isFilly;

import java.util.ArrayList;

import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.ponymanager.PonyManager;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class FillyHandler extends PSCmdExe{
	public FillyHandler(){
		super("FillyHandler", "gethandbook");
	}

	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)) {
			sendMessage(sender, notPlayer);
			return true;
		}
		if(((Player)sender).getInventory().addItem(getFillyHandbook()).size() > 0) {
			sendMessage(sender, "I tried to add the book, but you have a full inventory!");
			return true;
		}
		sendMessage(sender, "Heres the book!");
		return true;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		if(isFilly(event.getPlayer())) {
			if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				event.setCancelled(true);
				sendMessage(event.getPlayer(), "Hey! You wanna build? Read that book in your hands!");
				if(!event.getPlayer().getInventory().contains(Material.WRITTEN_BOOK)) {
					event.getPlayer().getInventory().addItem(getFillyHandbook());
				}
			}
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event){
		if(isFilly(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event){
		if(isFilly(event.getPlayer())) {
			ItemStack is = event.getItemDrop().getItemStack();
			if(is.getType() == Material.WRITTEN_BOOK) {
				BookMeta bm = (BookMeta)is.getItemMeta();
				if((bm.hasAuthor() && bm.getAuthor().equals("TheQueenOfPink")) && (bm.hasTitle() && bm.getTitle().equals("MineLittlePony.se Handbook"))) {
					event.getItemDrop().remove();
				}
				if(PonyManager.getGroup(event.getPlayer()).equals("filly")) {
					event.getPlayer().getInventory().addItem(getFillyHandbook());
				}
			}
		}
	}

	@EventHandler
	public void invClick(InventoryClickEvent event){
		if((event.getWhoClicked() instanceof Player) && isFilly((Player)event.getWhoClicked())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event){
		if(isFilly(event.getEntity())) {
			ArrayList<ItemStack> removeMe = new ArrayList<ItemStack>();
			for(ItemStack is : event.getDrops()) {
				if(is.getType() == Material.WRITTEN_BOOK) {
					BookMeta bm = (BookMeta)is.getItemMeta();
					if((bm.hasAuthor() && bm.getAuthor().equals("TheQueenOfPink")) && (bm.hasTitle() && bm.getTitle().equals("MineLittlePony.se Handbook"))) {
						removeMe.add(is);
					}
				}
			}
			event.getDrops().removeAll(removeMe);
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event){
		if(isFilly(event.getPlayer())) {
			event.getPlayer().getInventory().addItem(getFillyHandbook());
		}
	}

	@EventHandler
	public void onJoin(JoinEvent event){
		if(event.isJoining()) {
			if(isFilly(event.getPlayer())) {
				if(!event.getPlayer().getInventory().contains(Material.WRITTEN_BOOK)) {
					event.getPlayer().getInventory().addItem(getFillyHandbook());
				}
			}
		}
	}

	public static ItemStack getFillyHandbook(){
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

		BookMeta bookMeta = (BookMeta)book.getItemMeta();
		bookMeta.setTitle("MineLittlePony.se Handbook");
		bookMeta.setAuthor("TheQueenOfPink");
		bookMeta.addPage("");
		bookMeta.addPage("         Introduction\nWelcome to the MLP:FiM Steam Community Group Server! Say hello, and introduce yourself! First things first, read the rules that are after this intro (or by typing /rules)");
		bookMeta.addPage("You'll notice that you cannot build currently. This is because we disallow players from building until they have read the introduction of this book, plus the rules, It really helps cut down on griefing!");
		bookMeta.addPage("To obtain build rights, you need to become a " + ChatColor.BOLD + "pretty pony" + ChatColor.RESET + " (more on that later). Now, go to the next page to start reading the rules!");
		ArrayList<String> rules = (ArrayList<String>)getNamedConfig("rules.yml").getStringList("rules");
		for(String s : rules) {
			bookMeta.addPage("               Rules\n" + ChatColor.translateAlternateColorCodes('&', s));
		}
		bookMeta.addPage("Build Rights\n\nObtaining build rights is as easy as " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "asking someone to turn you into a pretty pony." + ChatColor.RESET + ChatColor.BLACK + "\nIts as easy as that. Just dont tell anyone else the secret! they need to figure it out on their own!");
		bookMeta.addPage(ChatColor.BOLD + "By being here or stating that you would like to be a pretty pony, you acknowledge and consent to logging systems such as chat loggers, block loggers, kill logs, and ip logs. None of this logged data can or will be used against you outside of the server.");
		bookMeta.addPage("              Tips\n\nThis server has towns. If you would like to live in one, please contact the mayor of the town.");
		bookMeta.addPage("              Tips\nReis Minimap (or honestly, any minimap mod) is allowed, alongside MineLittlePony, and Optifine. If you are curious if a mod is or isnt allowed, the server staff will answer!");
		bookMeta.addPage("              Tips\nThere is a public farm near the Ponyville map spawn. Just follow the bridge out of spawn. The farm is on the left, and you cant miss it. Just make sure to replant!");
		bookMeta.addPage("              Tips\nIf you are curious as where you can build in the Ponyville map, the answer is that you can build anywhere you like, except for in towns or on anothers property. To build in a town, obtain permission through the towns mayor");
		bookMeta.addPage("              Tips\nThis server does not have racial traits. Pegasi and Unicorns are submoderator positions, and they cannot fly or use magic.");
		//12
		bookMeta.addPage("   Command Reference\n\nThe last bit will contain commonly used commands. Commands will look like this\n/command <something> [soemthing else]\nAnything in <these> are required, while anything in [these] are not required.");
		bookMeta.addPage("General Commands\n\nThese commands are for general use on the server.");
		bookMeta.addPage("/afk - toggles afk\n/list - shows online players\n/rules - displays rules\n/motd - displays the MOTD\n/whopony <player> - displays usernames\n/clearinventory - clears your inventory\n/time - displays the worlds time");
		bookMeta.addPage("Security Commands\n\nThese commands are for security and anti-griefing mesures on the server.");
		bookMeta.addPage("/alert - Alerts offline Mods\n/cprivate - locks a chest\n/cpassword <pw> - locks chest with password\n/cunlock <pw> - unlocks a chest\n/cmodify <name> - allows another player in your chests\n/cremove - removes a protection");
		bookMeta.addPage("Teleportation and Warp Commands\n\nThese commands are for getting around.");
		bookMeta.addPage("/pwlist - lists all warps\n/gw <warp> - teleports to a global warp\n/tp <player> - requests tp to a player\n/tpa - accepts a teleport\n/tpd - denies a teleport\n/home - warps home\n/sethome - sets your home");
		bookMeta.addPage("/back - teleports back to the previous location\n/spawn [world] - teleports to a spawn");
		bookMeta.addPage("Emote Commands\nThese commands are used for emoting on the server");
		bookMeta.addPage("/me <text> - sends a basic emote\n/mle - lists all MLE emotes\n/mee <emote> <target> - uses an MLE emote");
		bookMeta.addPage("Chat Commands\nThese commands are used for navigating and using the channel system on the server");
		bookMeta.addPage("/channel - lists all channels\n/<channel> <message> - sends a quick message to a channel\n/join <channel> - joins a channel\n/leave <channel> - leaves a channel");
		bookMeta.addPage("/pm <player> <message> - pms a player\n/r <message> - reponds to the last PM\n/silence <player> - filters a players messages\n/silenced - displays silenced players");
		bookMeta.addPage("/cc <color> [channel] - changes the color of a channel");
		bookMeta.addPage("Please note: you can be chatting in only one channel at a time, but you may be listening to any number. If you join another channel, you are still listening to the previous one. To stop listening to a channel, you must use /leave <channel>");
		//27
		bookMeta.addPage("   Donator Reference\nThis bit of the handbook will go over donator commands. To obtain donator priveleges, please visit this page:\nhttp://minelittlepony.se/donate/");
		bookMeta.addPage("General Commands\n/nick <nickname> - sets nickname. Colors and spaces allowed\n/horn - displays help text for the horn\n/horn <left|right> <command> - sets the command for left or right UH\n/horn <on|off> - sets the UH on or off");
		bookMeta.addPage("Emote Commands\n/mles - starts the MLE emote setup wizard\n/deleteemote - deletes your emote");
		bookMeta.addPage("RemoteChest Commands\n/w - opens mobile workbench\n/c - opens the remote chest for the world youre in\nNote: in the PVP world (badlands) your remote chest will drop with you on death. BE CAREFUL!");
		bookMeta.addPage("/ts <type> - transfers materials to your chest. Type /ts to transfer everything, /ts ores to transfer ores, /ts materials to transfer things like wood, stone, and dirt, or if you know a specific material type, /ts <material> to transfer just that.");
		bookMeta.addPage("Teleportation Commands\n/pw <warp> - teleports to a private warp\n/pwset <warp> - sets a private warp\n/pwdel <warp> - deletes a private warp");
		bookMeta.addPage("Creative Mode\n/claimplot - claims a creative plot\n/releaseplot - releases your creative plot\n/j - teleports to where youre looking\n/top - teleports up");
		int pages = rules.size();
		bookMeta.setPage(1, "               Index\n\nIntro - Page 2\nRules - Page 5\nBuild rights - Page " + (5 + pages) + "\nTips - page " + (7 + pages) + "\nCmd Ref - Page " + (12 + pages) + "\nDonator ref - Page " + (27 + pages) + "\n\n\n\n\n\nNext Page -->");
		book.setItemMeta(bookMeta);

		return book;
	}
}
