package me.corriekay.pppopp3.modules;

import java.util.HashMap;

import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class RemoteChest extends PSCmdExe {

	private HashMap<String,Inventory> viewingInvs = new HashMap<String,Inventory>();
	public RemoteChest(){
		super("RemoteChest","c","w","transferchest");
	}
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		String cmdn = cmd.getName();
		if(!(sender instanceof Player)){
			sendMessage(sender,notPlayer);
			return true;
		}
		Player player = (Player)sender;
		if(cmdn.equals("c")){
			Pony pony = Ponyville.getPony(player);
			Inventory inv = pony.getRemoteChest(Equestria.get().getParentWorld(player.getWorld()));
			if(inv == null){
				sendMessage(player,"There is no remote chest for this world!");
				return true;
			}
			player.openInventory(inv);
			viewingInvs.put(player.getName(),inv);
			return true;
		}
		if(cmdn.equals("w")){
			player.openWorkbench(null,true);
			return true;
		}
		if(cmdn.equals("transferchest")){
			//TODO Transferchest
		}
		return true; 
	}
	@EventHandler
	public void invclick(InventoryClickEvent event){
		if(event.getWhoClicked() instanceof Player){
			Player player = (Player)event.getWhoClicked();
			if(viewingInvs.containsKey(player.getName())){
				Pony pony = Ponyville.getPony(player);
				String invWorldname = pony.getRCWorld(viewingInvs.get(player.getName())).getName();
				String actualWorldName = player.getWorld().getName();
				if(invWorldname != actualWorldName){
					event.setCancelled(true);
					player.closeInventory();
					viewingInvs.remove(player.getName());
					return;
				}
			}
		}
	}
	@EventHandler
	public void invclose(InventoryCloseEvent event){
		if(event.getPlayer() instanceof Player){
			Player p = (Player)event.getPlayer();
			if(viewingInvs.containsKey(p.getName())){
				Inventory inv2 = viewingInvs.get(p.getName());
				viewingInvs.remove(p.getName());
				Pony pony = Ponyville.getPony(p);
				pony.saveRemoteChest(pony.getRCWorld(inv2));
				pony.save();
				System.out.println("saving inventory");
				return;
			}
		}
	}
}
