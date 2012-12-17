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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class InvSee extends PSCmdExe {

	private final HashMap<String,ItemStack[]> invseerInvs = new HashMap<String,ItemStack[]>(); //store a players inventory here when theyre viewing an inventory
	private final HashSet<String> invseeing = new HashSet<String>(); //is the player viewing an inventory that isnt their own?
	protected final HashMap<String,String> invseers = new HashMap<String,String>(); //<Viewer,Owner> live inventory tracker
	public InvSee(){
		super("InvSee","invsee","commitinventorychange");
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)){
			sendMessage(sender,notPlayer);
			return true;
		}
		Player player = (Player)sender;
		Pony pony = Ponyville.getPony(player);
		
		// /invsee or /invsee <player> [world]
		if(cmd.getName().equals("invsee")){
			if(args.length==0){ // /invsee
				World world = Equestria.get().getParentWorld(player.getWorld());
				OfflinePlayer op = getOnlineOfflinePlayer(args[0],sender);
				boolean online = op.isOnline();
				//TODO
				if(args.length >1){
					world = Bukkit.getWorld(args[1]);
					if(world == null){
						sendMessage(player,"Cannot find world!");
						return true;
					}
				}
			} else { // /invsee <something>
				
			}
		}
		if(cmd.getName().equals("commitinventorychange")){
		}
		return true;
	}
	private void registerInvsee(Player player, Player target){
		invseers.put(player.getName(), target.getName());
		invseers.put(target.getName(), player.getName());
		player.getInventory().setContents(target.getInventory().getContents());
	}
	private void unregisterInvsee(Player player){
		invseers.remove(player.getName());
		HashSet<String> removeMe = new HashSet<String>();
		for(String s : invseers.keySet()){
			if(invseers.get(s).equals(player.getName())){
				removeMe.add(s);
			}
		}
		for(String s : removeMe){
			invseers.remove(s);
		}
	}
	private boolean isInvsee(String player){
		if(invseers.containsKey(player)||invseers.containsValue(player)){
			return true;
		} else return false;
	}
	private void updateInventory(final Player changer){
		if(!isInvsee(changer.getName())){
			return;
		}
		final InvSee invsee = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run(){
				Player player = Bukkit.getPlayerExact(invsee.invseers.get(changer.getName()));
				player.getInventory().setContents(changer.getInventory().getContents());
			}
		});
	}
	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()){
			String key = event.getPlayer().getName();
			if(invseeing.contains(key)){
				invseeing.remove(key);
				event.getPlayer().getInventory().setContents(invseerInvs.get(key));
			}
			invseers.remove(key);
			HashSet<String> removeMe = new HashSet<String>();
			for(String string : invseers.keySet()){
				if(invseers.get(string).equals(key)){
					removeMe.add(string);
					Player target = (Bukkit.getPlayerExact(string));
					if (target.hasPermission("es.invsee")) {
						sendMessage(target,"Target left the game. Switching to static inventory.");
					}
				}
			}
			for(String string : removeMe){
				invseers.remove(string);
			}
		}
	}
	@EventHandler
	public void onInv1(InventoryClickEvent event){
		if(event.getWhoClicked() instanceof Player){
			updateInventory((Player)event.getWhoClicked());
		}
	}
	@EventHandler
	public void onInv2(InventoryOpenEvent event){
		if(event.getPlayer() instanceof Player){
			updateInventory((Player)event.getPlayer());
		}
	}
	@EventHandler
	public void onInv3(InventoryCloseEvent event){
		if(event.getPlayer() instanceof Player){
			updateInventory((Player)event.getPlayer());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPickup(PlayerPickupItemEvent event){
		if(!event.isCancelled()){
			updateInventory(event.getPlayer());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDrop(PlayerDropItemEvent event){
		if(!event.isCancelled()){
			updateInventory(event.getPlayer());
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event){
		if(!event.isCancelled()){
			updateInventory(event.getPlayer());
		}
	}
	public void deactivate(){
		for(String s : invseeing){
			Player p = Bukkit.getPlayerExact(s);
			if(p == null){
				/* TODO
				IOP op = new IOP(s);
				PlayerInventory pi = new PlayerInventory(null);
				CraftInventoryPlayer cip = new CraftInventoryPlayer(pi);
				cip.setContents(invseerInvs.get(s));
				op.setInventory(cip);
				op.savePlayerData();
				*/
			} else {
				p.getInventory().setContents(invseerInvs.get(s));
			}
		}
	}
}
