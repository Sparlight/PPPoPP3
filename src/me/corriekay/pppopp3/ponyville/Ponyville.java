package me.corriekay.pppopp3.ponyville;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class Ponyville extends PSCmdExe{

	private HashMap<String,Pony> ponies = new HashMap<String,Pony>();
	private static Ponyville pv;
	
	public Ponyville(){
		super("Ponyville");
		pv = this;
		File dir = new File(Mane.getInstance().getDataFolder()+File.separator+"Players");
		if(!dir.isDirectory()){
			dir.mkdirs();
		}
		init();
	}
	private void init(){
		for(Player p : Bukkit.getOnlinePlayers()){
			Ponyville.addPony(p);
		}
	}
	
	public static Pony getPony(Player p){
		return getPony(p.getName());
	}
	public static Pony getPony(String s){
		return pv.ponies.get(s);
	}
	public static void removePony(Player p){
		removePony(p.getName());
	}
	public static void removePony(Pony p){
		removePony(p.getName());
	}
	public static void removePony(String s){
		Pony p = getPony(s);
		p.save();
		pv.ponies.remove(s);
	}
	public static Pony addPony(Player p){
		Pony po = Pony.moveToPonyville(p);
		pv.ponies.put(p.getName(), po);
		return po;
	}
	public static Pony getOfflinePony(Player p ){
		return getOfflinePony(p.getName());
	}
	public static Pony getOfflinePony(String p){
		Pony pony = getPony(p);
		if(pony == null){
			try {
				return new Pony(p);
			} catch (FileNotFoundException e) {
				return null;
			}
		} else {
			return pony;
		}
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		JoinEvent je = new JoinEvent(event.getPlayer(),addPony(event.getPlayer()),true);
		Bukkit.getPluginManager().callEvent(je);
		event.setJoinMessage(je.getMsg());
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		QuitEvent qe = new QuitEvent(event.getPlayer(),getPony(event.getPlayer()),true);
		Bukkit.getPluginManager().callEvent(qe);
		event.setQuitMessage(qe.getMsg());
		Pony p = getPony(event.getPlayer());
		p.save();
		removePony(event.getPlayer());
	}
}
