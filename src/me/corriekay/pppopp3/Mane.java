package me.corriekay.pppopp3;

import java.util.HashSet;

import me.corriekay.pppopp3.modules.EntityLogger;
import me.corriekay.pppopp3.modules.Equestria;
import me.corriekay.pppopp3.modules.RemoteChest;
import me.corriekay.pppopp3.ponymanager.PonyManager;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.plugin.java.JavaPlugin;

public class Mane extends JavaPlugin{

	private static Mane instance = null;
	private final HashSet<PSCmdExe> modules = new HashSet<PSCmdExe>();
	
	public static Mane getInstance(){
		return instance;
	}
	
	public void onEnable(){
		instance = this;
		try {modules.add(new Equestria());} catch (Exception e) {e.printStackTrace();}
		modules.add(new Ponyville());
		modules.add(new PonyManager());
		modules.add(new RemoteChest());
		try {modules.add(new EntityLogger());} catch (Exception e){e.printStackTrace();}
	}
	public void onDisable(){
		
	}
}
