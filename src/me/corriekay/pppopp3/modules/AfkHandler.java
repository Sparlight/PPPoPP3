package me.corriekay.pppopp3.modules;

import java.util.HashMap;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkHandler extends PSCmdExe{

	private final HashMap<String,Integer> autoAfkList = new HashMap<String,Integer>();
	//holds a list of players who are afk. If the boolean is true, theyre auto afk. if false, theyre manually afk.
	private final HashMap<String,Boolean> afkList = new HashMap<String,Boolean>();
	private final int afkseconds = getNamedConfig("config.yml").getInt("afkSeconds", 120);
	public final AfkHandler afk;
	private final int taskId;

	public AfkHandler(){
		super("AfkHandler", "afk");
		afk = this;
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!InvisibilityHandler.ih.isHidden(player.getName())) {
				registerPlayer(player.getName());
			}
		}
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Mane.getInstance(), new Runnable() {
			@Override
			public void run(){
				forLoop: for(String pName : autoAfkList.keySet()) {
					if(InvisibilityHandler.ih.isHidden(pName)) {
						continue forLoop;
					}
					if(!afkList.containsKey(pName)) {
						autoAfkList.put(pName, autoAfkList.get(pName) + 1);
						if(autoAfkList.get(pName) >= afkseconds) {
							setAfk(pName, true, true);
						}
					}
				}
			}
		}, 0, 20);
	}

	public void setAfk(String player, boolean afk, boolean manuafk){
		String playernick;
		try {
			playernick = Bukkit.getPlayer(player).getDisplayName();
		} catch(NullPointerException e) {
			autoAfkList.remove(player);
			return;
		}
		if(afk) {
			broadcastMessage(ChatColor.GRAY + playernick + ChatColor.GRAY + " is now afk.");
			afkList.put(player, manuafk);
		} else {
			broadcastMessage(ChatColor.GRAY + playernick + ChatColor.GRAY + " is no longer afk.");
			afkList.remove(player);
			autoAfkList.put(player, 0);
		}
	}

	private void playerActivity(Player player){
		if(autoAfkList.containsKey(player.getName())) {
			if(afkList.get(player.getName()) != null && afkList.get(player.getName())) {
				setAfk(player.getName(), false, false);
			}
			autoAfkList.put(player.getName(), 0);
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onJoin(JoinEvent event){
		if(!InvisibilityHandler.ih.isHidden(event.getPlayer().getName())) {
			registerPlayer(event.getPlayer().getName());
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onQuit(QuitEvent event){
		unregisterPlayer(event.getPlayer().getName());
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event){
		playerActivity(event.getPlayer());
	}

	@EventHandler
	public void onTalk(AsyncPlayerChatEvent event){
		playerActivity(event.getPlayer());
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event){
		playerActivity(event.getPlayer());
	}

	public void registerPlayer(String player){
		autoAfkList.put(player, 0);
	}

	public void unregisterPlayer(String player){
		autoAfkList.remove(player);
		afkList.remove(player);
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)) {
			sendMessage(sender, notPlayer);
			return true;
		}
		Player player = (Player)sender;
		String pName = player.getName();
		if(InvisibilityHandler.ih.isHidden(pName)) {
			sendMessage(player, "Please do not mess with afk while invisible");
			return true;
		}
		if(afkList.get(pName) == null) {//not in the afk list, not afk. make afk, and return.
			setAfk(pName, true, false);
			return true;
		} else {
			if(afkList.get(pName)) {
				afkList.put(pName, false);
				return true;
			}
			setAfk(pName, false, false);
			return true;
		}
	}

	public void deactivate(){
		Bukkit.getScheduler().cancelTask(taskId);
	}
}
