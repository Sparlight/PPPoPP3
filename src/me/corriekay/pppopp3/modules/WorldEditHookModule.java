package me.corriekay.pppopp3.modules;

import java.util.ArrayList;
import java.util.HashMap;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.Region;

import de.diddiz.LogBlockQuestioner.LogBlockQuestioner;

public class WorldEditHookModule extends PSCmdExe {

	private final ArrayList<String> commands = new ArrayList<String>();
	private final HashMap<String, Selection> playerAreas = new HashMap<String, Selection>();
	private final FileConfiguration config;
	private final LogBlockQuestioner questioner = (LogBlockQuestioner)Bukkit.getPluginManager().getPlugin("LogBlockQuestioner");
	//private final WorldEditPlugin wep = ((WorldEditPlugin)Bukkit.getPluginManager().getPlugin("WorldEdit"));
	public WorldEditHookModule(){
		super("WorldEditHookModule", "registercreativearea","unregistercreativearea");
		config = getNamedConfig("wehook.yml");
		loadAreas();
	}
	private void loadAreas(){
		playerAreas.clear();
		for(String player : config.getKeys(false)){
			Location l;
			double x,z;
			x = config.getInt(player+".x");
			z = config.getInt(player+".z");
			World w = Bukkit.getWorld("equestria");
			l = new Location(w,x,1,z);
			Location max,min;
			max = new Location(l.getWorld(),l.getX()-50,1,l.getZ()-50);
			min = new Location(l.getWorld(),l.getX()+50,l.getWorld().getMaxHeight(),l.getZ()+50);
			Selection s = new CuboidSelection(w, min, max);
			playerAreas.put(player, s);
		}
	}
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!(sender instanceof Player)){
			sendMessage(sender,notPlayer);
			return true;
		}
		final Player player = (Player)sender;
		final BukkitScheduler s = Bukkit.getScheduler();
		if(cmd.getName().equals("registercreativearea")){
			s.scheduleAsyncDelayedTask(Mane.getInstance(), new Runnable(){
				public void run(){
					Selection sel = playerAreas.get(player.getName());
					if(sel != null){
						String answer = questioner.ask(player, pinkieSays+"You already have a claimed area! Are you sure you wish to abandon it for a new one?", "yes","no");
						if(answer.equals("no")){
							sendSyncMessage(player,"Okay! Aborting!");
							return;
						} else if(answer.equals("yes")){
							String answer2 = questioner.ask(player, pinkieSays+"Are you ABSOLUTELY sure? This will REMOVE your protection in the area, allowing others to claim it!", "yes","no");
							if(answer2.equals("no")){
								sendSyncMessage(player,"Okay! Aborting!");
								return;
							} else if(answer2.equals("yes")){
								sendSyncMessage(player,"Alright! Removing old creative claim and setting the new one!");
								s.scheduleSyncDelayedTask(Mane.getInstance(),new Runnable(){
									public void run(){
										try {
											removeProtection(player.getName());
											addProtection(player);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
							}
						}
					} else {
						s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
							public void run(){
								sendMessage(player,"Claiming area!");
								try {
									addProtection(player);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return;
							}
						});
					}
				}
			});
		}
		if(cmd.getName().equals("unregistercreativearea")){
			Selection sel = playerAreas.get(player.getName());
			if(sel == null){
				sendMessage(player,"Silly, you dont have a registered creative area!");
				return true;
			}
			s.scheduleAsyncDelayedTask(Mane.getInstance(), new Runnable(){
				public void run(){
					String answer = questioner.ask(player, pinkieSays+"Are you sure you want to remove your creative registered area? Players will be able to edit what you've created there!", "yes", "no");
					if(answer.equals("no")){
						sendSyncMessage(player,"Okay! Aborting!");
						return;
					} else {
						answer = questioner.ask(player, pinkieSays+"Are you ABSOLUTELY sure? This will REMOVE your protection in the area, allowing others to claim it!", "yes","no");
						if(answer.equals("no")){
							sendSyncMessage(player,"Okay! Aborting!");
							return;
						} else {
							s.scheduleSyncDelayedTask(Mane.getInstance(), new Runnable(){
								public void run(){
									try {
										removeProtection(player.getName());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
					}
				}
			});
		}
		return true;
	}
	private void removeProtection(String player) throws Exception {
		removeBedrock(playerAreas.get(player).getRegionSelector().getRegion(),Bukkit.getWorld("equestria"));
		config.set(player,null);
		saveNamedConfig("wehook.yml",config);
		loadAreas();
	}
	private void removeBedrock(Region r,World w){
		for(BlockVector bv : r){
			Block b = w.getBlockAt(bv.getBlockX(),bv.getBlockY(),bv.getBlockZ());
			if(b.getY()!=1){
				continue;
			}
			if(b.getType() == Material.BEDROCK){
				b.setType(Material.GRASS);
			}
		}
	}
	private boolean addProtection(Player player) throws Exception{/* TODO FIX DIS SHIT
		Location l = player.getLocation();
		l.setY(1);
		
		//get selection
		Location one, two, three, four;
		one = new Location(l.getWorld(),l.getX()-50,1,l.getZ()-50);
		two = new Location(l.getWorld(),l.getX()-50,1,l.getZ()+50);
		three = new Location(l.getWorld(),l.getX()+50,1,l.getZ()+50);
		four = new Location(l.getWorld(),l.getX()+50,1,l.getZ()-50);
		CuboidSelection finalsel = new CuboidSelection(l.getWorld(),one, new Location(three.getWorld(),three.getBlockX(),three.getWorld().getMaxHeight()-1,three.getBlockZ()));
		
		for(BlockVector bv : finalsel.getRegionSelector().getRegion()){
			Block b = l.getWorld().getBlockAt(bv.getBlockX(),bv.getBlockY(),bv.getBlockZ());
			Material m = b.getType();
			if(!(m==Material.BEDROCK||m==Material.AIR||m==Material.GRASS||m==Material.DIRT)){
				System.out.println(m.name());
				sendMessage(player,"This area has built stuff on it already! It is unclaimable! (hint: there needs to be a 50 block radius in unclaimed, untouched area to claim an area!)");
				return false;
			}
			
		}
		
		//compare if it intersects
		for(Selection otherSel : playerAreas.values()){
			if(otherSel.contains(one)||otherSel.contains(two)||otherSel.contains(three)||otherSel.contains(four)){
				sendMessage(player,"This area is intersecting an already claimed area! It is unclaimable! (hint: there needs to be a 50 block radius in unclaimed, untouched area to claim an area!)");
				return false;
			}
		}
		
		//All checks out! lets lay down the bedrock, and commit the selection
		CuboidSelection sel = new CuboidSelection(l.getWorld(),one,two);
		makeBedrock(sel.getRegionSelector().getRegion(),l.getWorld());
		sel = new CuboidSelection(l.getWorld(),one,four);
		makeBedrock(sel.getRegionSelector().getRegion(),l.getWorld());
		sel = new CuboidSelection(l.getWorld(),three, four);
		makeBedrock(sel.getRegionSelector().getRegion(),l.getWorld());
		sel = new CuboidSelection(l.getWorld(),three, two);
		makeBedrock(sel.getRegionSelector().getRegion(),l.getWorld());
		
		config.set(player.getName()+".x", l.getBlockX());
		config.set(player.getName()+".z", l.getBlockZ());
		saveNamedConfig("wehook.yml",config);
		
		loadAreas();
		*/
		return true;
	}
	private void makeBedrock(Region bvs, World w){
		for(BlockVector bv : bvs){
			Block b = w.getBlockAt(bv.getBlockX(),bv.getBlockY(),bv.getBlockZ());
			b.setType(Material.BEDROCK);
		}
	}
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event){
		String[] msgWords = event.getMessage().split(" ");
		String cmd = msgWords[0];
		String[] args = new String[msgWords.length-1];
		for(int i = 1; i<msgWords.length;i++){
			args[i-1] = msgWords[i];
		}
		if(commands.contains(cmd)){
			//we command
		}
	}
	@EventHandler
	public void chunkCreate(ChunkLoadEvent event) throws IncompleteRegionException{
		if(event.isNewChunk()){
			if(event.getWorld().getName().equals("equestria")){
				Selection cr = new CuboidSelection(event.getWorld(),
						event.getChunk().getBlock(0, 0, 0).getLocation(),
						event.getChunk().getBlock(15, event.getWorld().getMaxHeight()-1,15).getLocation());
				for(BlockVector bv : cr.getRegionSelector().getRegion()){
					Block b = event.getWorld().getBlockAt(bv.getBlockX(),bv.getBlockY(),bv.getBlockZ());
					if(b.getY()==0){
						b.setType(Material.BEDROCK);
					} else if(b.getY()==1){
						b.setType(Material.GRASS);
					} else {
						b.setType(Material.AIR);
					}
				}
			}
		}
	}
	@EventHandler
	public void breakBlock(BlockBreakEvent event){
		if(!event.getBlock().getWorld().getName().equals("equestria")){
			return;
		}
		Player p = event.getPlayer();
		event.setCancelled(!canModify(event.getBlock().getLocation(),p));
	}
	@EventHandler
	public void placeBlock(BlockPlaceEvent event){
		if(!event.getBlock().getWorld().getName().equals("equestria")){
			return;
		}
		Player p = event.getPlayer();
		event.setCancelled(!canModify(event.getBlock().getLocation(),p));
	}
	private boolean canModify(Location l, Player p){
		if(p.hasPermission("pppopp3.creativeadmin")){
			return true;
		}
		if(l.getBlock().getType()==Material.BEDROCK){
			return false;
		}
		for(String p2 : playerAreas.keySet()){
			if(p2.equals(p.getName())){
				continue;
			} else {
				Selection s = playerAreas.get(p2);
				if(s.contains(l)){
					return false;
				}
			}
		}
		return true;
	}
}
