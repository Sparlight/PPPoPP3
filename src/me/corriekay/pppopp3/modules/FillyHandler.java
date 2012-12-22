package me.corriekay.pppopp3.modules;

import static me.corriekay.pppopp3.ponymanager.PonyManager.isFilly;

import java.util.ArrayList;

import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.ponymanager.PonyManager;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
		super("FillyHandler");
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
		int pages = rules.size();
		bookMeta.setPage(1, "               Index\n\nIntro - Page 2\nRules - Page 5\nBuild rights - Page " + (5 + pages) + "\nTips - page " + (7 + pages) + "\nCmd Ref - Page " + (12 + pages) + "\n\n\n\n\n\n\nNext Page -->");
		book.setItemMeta(bookMeta);

		return book;
	}
}
