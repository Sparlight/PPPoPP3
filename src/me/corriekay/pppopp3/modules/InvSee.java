package me.corriekay.pppopp3.modules;

import java.util.HashMap;
import java.util.HashSet;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InvSee extends PSCmdExe{
	private static InvSee instance;

	private final HashMap<String,InventoryContents> invseerInvs = new HashMap<String,InventoryContents>(); //store a players inventory here when theyre viewing an inventory
	protected final HashMap<String,String> invseers = new HashMap<String,String>(); //<Viewer,Owner> live inventory tracker

	public InvSee(){
		super("InvSee", "invsee", "commitinventorychange");
		instance = this;
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)) {
			sendMessage(sender, notPlayer);
			return true;
		}
		Player player = (Player)sender;

		// /invsee or /invsee <player> [world]
		if(cmd.getName().equals("invsee")) {
			System.out.println(0);
			if(invseerInvs.containsKey(player.getName())) {
				System.out.println(1); //viewing an inventory
				if(args.length > 0) { //trying to target a player

					System.out.println(2);
					OfflinePlayer target = getOnlineOfflinePlayer(args[0], player);
					if(target == null) { //not found.
						System.out.println(3);
						return true;
					}
					Pony targetPony = Ponyville.getOfflinePony(target.getName());
					World world = Equestria.get().getParentWorld(player.getWorld());
					if(args.length > 1) { //the player is targetting a specific world inventory
						System.out.println(4);
						world = Bukkit.getWorld(args[1]);
						if(world == null) {
							System.out.println(5);
							sendMessage(player, "World not found!");
							return true;
						}
						world = Equestria.get().getParentWorld(world);
					}
					if(!target.isOnline()) { //target is offline
						System.out.println(6);
						PlayerInventory pi = targetPony.getInventory(world.getName());
						unregisterInvsee(player);
						player.getInventory().setContents(pi.getContents());
						player.getInventory().setArmorContents(pi.getArmorContents());
						sendMessage(player, "Viewing static inventory, Player: " + targetPony.getName() + "/" + targetPony.getNickname() + ", World: " + world.getName() + "!");
						return true;
					} else {
						System.out.println(7);
						if(target.getName().equals(player.getName())) { //UNREGISTER. player losing invsee
							System.out.println(8);
							unregisterInvsee(player);
							invseerInvs.get(player.getName()).restoreInventory(player);
							invseerInvs.remove(player.getName());
							sendMessage(player, "Inventory returned!");
							return true;
						} else { //target is someone else
							System.out.println(9);
							unregisterInvsee(player);
							if(world.getName().equals(Equestria.get().getParentWorld(((Player)target).getWorld()).getName())) { //theyre targetting their world. Time for live invsee!
								System.out.println(10);
								registerInvsee(player, (Player)target);
								sendMessage(player, "Viewing live inventory, Player: " + targetPony.getNickname() + ", World: " + world.getName() + "!");
							} else {//targetting an online player world that the player is not in.
								System.out.println(11);
								PlayerInventory pi = targetPony.getInventory(world.getName());
								player.getInventory().setContents(pi.getContents());
								player.getInventory().setArmorContents(pi.getArmorContents());
								sendMessage(player, "Viewing static inventory, Player: " + targetPony.getNickname() + ", World: " + world.getName() + "!");
								sendMessage(player, "WARNING, NOT A LIVE INVENTORY. WILL NOT SAVE WITHOUT COMMITTING.");
							}
							return true;
						}
					}
				} else {
					unregisterInvsee(player);
					invseerInvs.get(player.getName()).restoreInventory(player);
					invseerInvs.remove(player.getName());
					sendMessage(player, "Inventory returned!");
					return true;
				}
			} else { //not viewing an inventory
				System.out.println(12);
				if(args.length > 0) { //targetting a player
					System.out.println(13);
					OfflinePlayer target = getOnlineOfflinePlayer(args[0], player);
					if(target == null) {
						System.out.println(14);
						return true;
					}
					Pony targetPony = Ponyville.getOfflinePony(target.getName());
					World world = Equestria.get().getParentWorld(player.getWorld());
					if(args.length > 1) {
						System.out.println(15);
						world = Bukkit.getWorld(args[1]);
						if(world == null) {
							System.out.println(16);
							sendMessage(player, "World not found!");
							return true;
						}
						world = Equestria.get().getParentWorld(world);
					}
					if(target.isOnline()) {
						System.out.println(17);
						if(target.getName().equals(player.getName())) { //invsee self
							System.out.println(18);
							sendMessage(player, "Already viewing your own inventory!");
							return true;
						}
						unregisterInvsee(player);
						invseerInvs.put(player.getName(), new InventoryContents(player));
						if(world.getName().equals(Equestria.get().getParentWorld(((Player)target).getWorld()).getName())) {
							System.out.println(19);
							registerInvsee(player, (Player)target);
							sendMessage(player, "Viewing live inventory, Player: " + targetPony.getNickname() + ", World: " + world.getName() + "!");
						} else {
							System.out.println(20);
							PlayerInventory pi = targetPony.getInventory(world.getName());
							player.getInventory().setContents(pi.getContents());
							player.getInventory().setArmorContents(pi.getArmorContents());
							sendMessage(player, "Viewing static inventory, Player: " + targetPony.getNickname() + ", World: " + world.getName() + "!");
							sendMessage(player, "WARNING, NOT A LIVE INVENTORY. WILL NOT SAVE WITHOUT COMMITTING.");
						}
						return true;
					} else { //Is offline
						unregisterInvsee(player);
						invseerInvs.put(player.getName(), new InventoryContents(player));
						PlayerInventory pi = targetPony.getInventory(world.getName());
						player.getInventory().setContents(pi.getContents());
						player.getInventory().setArmorContents(pi.getArmorContents());
						sendMessage(player, "Viewing static inventory, Player: " + targetPony.getName() + " World: " + world.getName() + "!");
						return true;
					}
				} else { // /invsee
					System.out.println(21);
					sendMessage(player, "Already viewing your own inventory!");
					return true;
				}
			}
		}
		if(cmd.getName().equals("commitinventorychange")) {
			//TODO
		}
		return true;
	}

	public static InvSee get(){
		return instance;
	}

	public boolean restoreInventory(Player player){
		InventoryContents ic = invseerInvs.get(player.getName());
		if(ic != null) {
			unregisterInvsee(player);
			ic.restoreInventory(player);
			invseerInvs.remove(player.getName());
			return true;
		} else return false;
	}

	private void registerInvsee(Player player, Player target){
		invseers.put(player.getName(), target.getName());
		invseers.put(target.getName(), player.getName());
		player.getInventory().setContents(target.getInventory().getContents());
	}

	private void unregisterInvsee(Player player){
		invseers.remove(player.getName());
		HashSet<String> removeMe = new HashSet<String>();
		for(String s : invseers.keySet()) {
			if(invseers.get(s).equals(player.getName())) {
				removeMe.add(s);
			}
		}
		for(String s : removeMe) {
			invseers.remove(s);
		}
	}

	public boolean isInvsee(String player){
		if(invseers.containsKey(player) || invseers.containsValue(player)) {
			return true;
		} else return false;
	}

	private void updateInventory(final Player changer){
		if(!isInvsee(changer.getName())) {
			return;
		}
		final InvSee invsee = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
			@Override
			public void run(){
				String viewer = invsee.invseers.get(changer.getName());
				if(viewer == null) {
					return;
				}
				Player player = Bukkit.getPlayerExact(invsee.invseers.get(changer.getName()));
				player.getInventory().setContents(changer.getInventory().getContents());
				player.getInventory().setArmorContents(changer.getInventory().getArmorContents());
			}
		});
	}

	@EventHandler
	public void worldChange(PlayerTeleportEvent event){
		World from, to;
		from = event.getFrom().getWorld();
		to = event.getTo().getWorld();
		from = Equestria.get().getParentWorld(from);
		to = Equestria.get().getParentWorld(to);
		if(!from.getName().equals(to.getName())) {
			for(String invseer : invseers.keySet()) {
				String invseee = invseers.get(invseer);
				if(invseee == event.getPlayer().getName()) {
					Player invseeer = Bukkit.getPlayer(invseer);
					if(invseeer != null) {
						restoreInventory(invseeer);
						sendMessage(invseeer, "Your invsee target has switched worlds! Your invsee session has been terminated!");
					}
				}
			}
			if(restoreInventory(event.getPlayer())) {
				sendMessage(event.getPlayer(), "You have teleported between universes, and your invsee session has been forcibly shut down!");
				return;
			}

		}
	}

	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()) {
			String key = event.getPlayer().getName();
			if(invseerInvs.containsKey(key)) {
				invseerInvs.get(key).restoreInventory(event.getPlayer());
			}
			invseers.remove(key);
			HashSet<String> removeMe = new HashSet<String>();
			for(String string : invseers.keySet()) {
				if(invseers.get(string).equals(key)) {
					removeMe.add(string);
					Player target = (Bukkit.getPlayerExact(string));
					if(target.hasPermission("es.invsee")) {
						sendMessage(target, "Target left the game. Switching to static inventory.");
					}
				}
			}
			for(String string : removeMe) {
				invseers.remove(string);
			}
		}
	}

	@EventHandler
	public void onInv1(InventoryClickEvent event){
		if(event.getWhoClicked() instanceof Player) {
			updateInventory((Player)event.getWhoClicked());
		}
	}

	@EventHandler
	public void onInv2(InventoryOpenEvent event){
		if(event.getPlayer() instanceof Player) {
			updateInventory((Player)event.getPlayer());
		}
	}

	@EventHandler
	public void onInv3(InventoryCloseEvent event){
		if(event.getPlayer() instanceof Player) {
			updateInventory((Player)event.getPlayer());
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPickup(PlayerPickupItemEvent event){
		if(!event.isCancelled()) {
			updateInventory(event.getPlayer());
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDrop(PlayerDropItemEvent event){
		if(!event.isCancelled()) {
			updateInventory(event.getPlayer());
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event){
		if(!event.isCancelled()) {
			updateInventory(event.getPlayer());
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event){
		Player player = event.getPlayer();
		if(!isInvsee(player.getName())) {
			updateInventory(event.getPlayer());
		}
	}

	public void deactivate(){
		for(String s : invseerInvs.keySet()) {
			Player p = Bukkit.getPlayerExact(s);
			restoreInventory(p);
		}
	}

	private class InventoryContents{
		private final ItemStack[] items, armor;

		public InventoryContents(Player player){
			items = player.getInventory().getContents();
			armor = player.getInventory().getArmorContents();
		}

		public void restoreInventory(Player player){
			player.getInventory().setArmorContents(armor);
			player.getInventory().setContents(items);
		}
	}
}
