package me.corriekay.pppopp3.modules;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.rpa.RemotePonyAdmin;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class Lockdown extends PSCmdExe{

	private boolean lockdown = false;

	public Lockdown(){
		super("Lockdown", "lockdown");
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("lockdown")) {
			if(lockdown) {
				lockdown = false;
				Bukkit.broadcast(ChatColor.GREEN + "Lockdown disabled!", "pppopp3.lockdown");
				RemotePonyAdmin.rpa.message("Lockdown disabled!");
			} else {
				lockdown = true;
				Bukkit.broadcast(ChatColor.DARK_RED + "Lockdown enabled!", "pppopp3.lockdown");
				RemotePonyAdmin.rpa.message("Lockdown enabled!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
					@Override
					public void run(){
						lockdown = false;
						Bukkit.broadcast(ChatColor.GREEN + "Lockdown disabled!", "pppopp3.lockdown");
						RemotePonyAdmin.rpa.message("Lockdown disabled!");
					}
				}, 50 * 60 * 60 * 12);
			}
			return true;
		}
		return true;
	}

	@EventHandler
	public void join(final AsyncPlayerPreLoginEvent event){
		if(event.isAsynchronous()) {
			Future<Boolean> kick = Bukkit.getScheduler().callSyncMethod(Mane.getInstance(), new Callable<Boolean>() {
				public Boolean call(){
					if(lockdown) {
						File file = new File(Mane.getInstance().getDataFolder() + File.separator + "Players", event.getName() + ".yml");
						OfflinePlayer player = Bukkit.getOfflinePlayer(event.getName());
						if(!player.hasPlayedBefore() && !file.exists()) {
							return true;
						}
					}
					return false;
				}
			});
			try {
				if(kick.get()) {
					event.disallow(Result.KICK_OTHER, "The server is unavailable right now, Please try again later!");
					return;
				} else {
					return;
				}
			} catch(Exception e) {
				e.printStackTrace();
				return;
			}
		}
		if(lockdown) {
			File file = new File(Mane.getInstance().getDataFolder() + File.separator + "Players", event.getName());
			OfflinePlayer player = Bukkit.getOfflinePlayer(event.getName());
			if(!player.hasPlayedBefore() && !file.exists()) {
				event.disallow(Result.KICK_OTHER, "The server is unavailable right now, Please try again later!");

			}
		}
	}
}
