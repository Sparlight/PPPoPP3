package me.corriekay.pppopp3.modules;

import java.util.HashMap;
import java.util.HashSet;

import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.FlatWorldGenerator;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;

public class Equestria extends PSCmdExe {

	//world1: subworld. world2: parent world
	private HashMap<World,World> worlds = new HashMap<World,World>();
	private final HashSet<World> parentWorlds = new HashSet<World>();
	private HashMap<World,GameMode> gamemodes = new HashMap<World,GameMode>();
	private static Equestria e;
	

	public Equestria() throws Exception{
		super("Equestria", "test");
		e = this;
		FileConfiguration worldconfig = getNamedConfig("equestria.yml");
		for(String world : worldconfig.getConfigurationSection("worldconfig").getKeys(false)){
			Environment e = Environment.NORMAL;
			WorldType wt;
			try{
				wt = WorldType.valueOf(worldconfig.getString("worldconfig."+world+".type"));
			} catch (Exception e1){
				wt = WorldType.NORMAL;
				System.out.println(world+" type is null, defaulting to normal");
			}
			GameMode gm;
			try{
				gm = GameMode.valueOf(worldconfig.getString("worldconfig."+world+".gamemode"));
			} catch (Exception e1){
				gm = GameMode.SURVIVAL;
				System.out.println(world+" gamemode is null, defaulting to survival");
			}
			World w = loadWorld(world,e,wt);
			worlds.put(w,w);
			parentWorlds.add(w);
			gamemodes.put(w,gm);
			if(worldconfig.getBoolean("worldconfig."+world+".end")){
				World end = loadWorld(world+"_the_end",Environment.THE_END,WorldType.LARGE_BIOMES);
				worlds.put(end,w);
			}
			if(worldconfig.getBoolean("worldconfig."+world+".end")){
				World nether = loadWorld(world+"_nether",Environment.NETHER,WorldType.LARGE_BIOMES);
				worlds.put(nether,w);
			}
		}
		for(Entity e : Bukkit.getWorld("equestria").getEntities()){
			if(e instanceof LivingEntity){
				LivingEntity le = (LivingEntity)e;
				if(!(le instanceof Player)){
					le.remove();
				}
			}
		}
	}
	public World loadWorld(String worldname, Environment e,WorldType wt){
		World w = Bukkit.getWorld(worldname);
		if(w!=null){
			return w;
		}
		WorldCreator wc = new WorldCreator(worldname);
		wc.environment(e).type(wt);
		if(worldname.equals("equestria")){
			wc.generateStructures(false);
			wc.generator(new FlatWorldGenerator());
		}
		return wc.createWorld();
	}
	public World getParentWorld(World world){
		return worlds.get(world);
	}
	@SuppressWarnings("unchecked")
	public HashSet<World> getParentWorlds(){
		return (HashSet<World>) parentWorlds.clone();
	}
	public boolean isSubWorld(World child, World parent){
		return worlds.get(child).equals(parent);
	}
	public World getNether(World w){
		World parent = getParentWorld(w);
		for(World sub : worlds.keySet()){
			if(getParentWorld(sub).equals(parent)){
				if(sub.getEnvironment() == Environment.NETHER){
					return sub;
				}
			}
		}
		return null;
	}
	@EventHandler
	public void onPortalEnter(PlayerPortalEvent event){
		World w = event.getPlayer().getWorld();
		World parent = getParentWorld(w);
		World toWorld;
		double ratio = 0;
		if(!(parent.getName().equals("badlands")))return;
		if(w.getEnvironment() == Environment.NORMAL){
			ratio = 0.125;
			toWorld = getNether(w);
		} else if(w.getEnvironment() == Environment.NETHER){
			ratio = 8.0;
			toWorld = getParentWorld(w);
		} else {
			return;
		}
		Location from = event.getFrom();
		Location finalLoc = new Location(toWorld,from.getX()*ratio,from.getY(),from.getZ()*ratio);
		event.setTo(finalLoc);
	}
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent event){
		Player player = event.getPlayer();
		World fromWorld = event.getFrom().getWorld();
		World toWorld = event.getTo().getWorld();
		World fromParent = getParentWorld(fromWorld);
		World toParent = getParentWorld(toWorld);
		if(fromParent.equals(toParent)){
			return;
		} else {
			Pony p = Ponyville.getPony(player);
			p.setInventory(player.getInventory(),fromParent.getName());
			Pony.setWorldStats(player, fromParent.getName());
			PlayerInventory pi = p.getInventory(toParent.getName());
			player.getInventory().setContents(pi.getContents());
			player.getInventory().setArmorContents(pi.getArmorContents());
			Pony.getWorldStats(player, toParent.getName());
			p.save();
			player.setGameMode(gamemodes.get(toParent));
		}
	}
	@EventHandler
	public void playerRespawn(PlayerRespawnEvent event){
		World w = getParentWorld(event.getPlayer().getLocation().getWorld());
		event.setRespawnLocation(w.getSpawnLocation());
	}
	@EventHandler
	public void spawn(CreatureSpawnEvent event){
		if(event.getLocation().getWorld().getName().equals("equestria")){
			event.setCancelled(true);
		}
	}
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		World w = Bukkit.getWorld(args[0]);
		((Player)sender).teleport(w.getSpawnLocation());
		return true;
	}
	public static Equestria get() {
		return e;
	}
}
