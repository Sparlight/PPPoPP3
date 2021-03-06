package me.corriekay.pppopp3;

import java.util.HashSet;

import me.corriekay.pppopp3.chat.ChatHandler;
import me.corriekay.pppopp3.chat.PonySpy;
import me.corriekay.pppopp3.emote.MineLittleEmote;
import me.corriekay.pppopp3.modules.*;
import me.corriekay.pppopp3.ponymanager.PonyManager;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.remotechest.RemoteChest;
import me.corriekay.pppopp3.rpa.RemotePonyAdmin;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.warp.WarpHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Mane extends JavaPlugin{

	private static Mane instance = null;
	private final HashSet<PSCmdExe> modules = new HashSet<PSCmdExe>();

	public static Mane getInstance(){
		return instance;
	}

	public void onEnable(){
		instance = this;
		try {
			modules.add(new Equestria());
		} catch(Exception e) {
			e.printStackTrace();
		}
		modules.add(new Ponyville());
		modules.add(new PonyManager());
		modules.add(new BanHammer());
		try {
			modules.add(new InvisibilityHandler());
		} catch(Exception e) {
			e.printStackTrace();
		}
		modules.add(new AfkHandler());
		modules.add(new RemoteChest());
		try {
			modules.add(new EntityLogger());
		} catch(Exception e) {
			e.printStackTrace();
		}
		modules.add(new WorldEditHookModule());
		try {
			modules.add(new ChatHandler());
		} catch(Exception e) {
			e.printStackTrace();
		}
		modules.add(new PonySpy());
		modules.add(new AntiDiscordModule());
		try {
			modules.add(new WarpHandler());
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			modules.add(new RemotePonyAdmin());
		} catch(Exception e) {
			e.printStackTrace();
		}
		modules.add(new UnicornHorn());
		try {
			modules.add(new XrayLogger());
		} catch(Exception e) {
			e.printStackTrace();
		}
		modules.add(new SpamHandler());
		modules.add(new MineLittleEmote());
		modules.add(new InvSee());
		modules.add(new FillyHandler());
		modules.add(new AdminHandler());
		modules.add(new Lockdown());
		modules.add(new MiscCommands());
		modules.add(new RainbowDash());
		modules.add(new GodHandler());
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run(){
				Bukkit.broadcastMessage(ChatColor.DARK_RED + "ATTENTION: This server is unstable! We are in the process of testing new code for the server. Please keep calm, and report any bugs you find at this link: http://mlpf.im/PPPoPP3 Please bear with us as there will be random server reboots, restarts, and even rollbacks. I appreciate your cooperation!");
			}
		}, 0, 30 * 60 * 20);
	}

	public void onDisable(){
		for(PSCmdExe exe : modules) {
			exe.deactivate();
		}
		for(BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
			if(task.getOwner() == this) {
				task.cancel();
			}
		}
	}
}
