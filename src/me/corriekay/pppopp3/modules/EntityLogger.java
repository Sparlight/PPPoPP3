package me.corriekay.pppopp3.modules;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityLogger extends PSCmdExe {

	private final FileConfiguration config;

	public EntityLogger() throws Exception{
		super("EntityLogger",new String[]{"finddeaths"});
		Method m = getClass().getDeclaredMethod("entityEvent",EntityDeathEvent.class);
		methodMap.put(EntityDeathEvent.class, m);
		File file = new File(Mane.getInstance().getDataFolder(),"EntitySql.yml");
		if(!file.exists()){
			file.createNewFile();
		}
		config = YamlConfiguration.loadConfiguration(file);
	}
	public boolean handleCommand(final CommandSender sender, Command cmd, String label, final String[] args){
		if(cmd.getName().equals("finddeaths")){
			// /finddeaths <distance> <mobtype> <days>
			if(args.length<3){
				sendMessage(sender,notEnoughArgs);
				return true;
			}
			if(!(sender instanceof Player)){
				sendMessage(sender,notPlayer);
				return true;
			}
			
			Bukkit.getScheduler().scheduleAsyncDelayedTask(Mane.getInstance(), new Runnable(){
				public void run(){
					
					try{
						EntityType et = EntityType.valueOf(args[1]);
						int days = Integer.parseInt(args[2]), distance = Integer.parseInt(args[0]);
						ArrayList<String> finds = parseSQL(et, days, distance, ((Player)sender).getLocation());
						StringBuilder sb = new StringBuilder();
						sb.append(ChatColor.GOLD+"");
						for(String s : finds){
							sb.append(s+"\n");
						}
						sender.sendMessage(sb.toString());
						return;
					} catch (Exception e){
						sendMessage(sender,"Check your arguments! Something went wrong!");
						return;
					}
				}
			});
			
		}
		return true;
	}
	@EventHandler 
	public void entityEvent(EntityDeathEvent event){
		//entity that died
		Entity e = event.getEntity();
		
		//location that the entity died in
		Location l = e.getLocation();
		
		String whokilled, deadEntity, targetEntity;
		targetEntity = null;
		deadEntity = e.getType().name().toLowerCase();
		
		//document various types of mobs
		if(e instanceof Tameable){
			Tameable t = (Tameable)e;
			if(t.isTamed()){
				deadEntity = t.getOwner().getName()+"'s "+e.getType().name().toLowerCase();
			}
		}
		if(e instanceof Sheep){
			Sheep s = (Sheep)e;
			deadEntity = s.getColor().name().toLowerCase()+" sheep";
		}
		if(e instanceof Villager){
			Villager v = (Villager)e;
			deadEntity = v.getProfession().name().toLowerCase()+" villager";
		}
		if(e instanceof Player){
			Player p = (Player)e;
			deadEntity = "player "+p.getName();
		}
		
		//document type of death
		EntityDamageEvent ede = e.getLastDamageCause();
		if(ede instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)ede;
			Entity killer = edbee.getDamager();
			if(killer instanceof Projectile){
				Projectile p = (Projectile)killer;
				killer = p.getShooter();
			}
			if(killer instanceof Creature){
				Creature creature = (Creature)killer;
				Entity target = creature.getTarget();
				if(target instanceof Player){
					Player targetP = (Player)target;
					targetEntity = "player "+targetP.getName();
				} else {
					targetEntity = target.getType().name().toLowerCase();
				}
			}
			if(killer instanceof Player){
				Player player = (Player)killer;
				whokilled = "player "+player.getName();
			} else {
				whokilled = killer.getType().name().toLowerCase();
			}
		} else {
			whokilled = ede.getCause().name().toLowerCase();
		}
		String message;
		message = whokilled+" killed "+deadEntity;
		if(targetEntity != null){
			message+= " while targetting "+targetEntity;
		}
		logAttack(message, e.getType(),l, System.currentTimeMillis());
	}
	private void logAttack(String message, EntityType et, Location loc, long timestamp) {
		//begin SparCode
		/*//TODO
		 * I need you to include the id for each entry in each table. check for a set schema (you decide the name)
		 * String tableName = et.name();
		 * if the table doesnt exist, create one.
		 * 
		 * add to the table, (world,message,timestamp,x,y,z) as fields, and put (loc.getWorld().getName(),message,timestamp,loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()) as the values
		 * but also, create an incrementing id value, so we can iterate over that. Or however you thing will work.
		 */
		//end SparCode
	}
	private ArrayList<String> parseSQL(EntityType et, int days, int distance, Location origin){
		ArrayList<String> finds = new ArrayList<String>();
		//TODO
		//get the sql table via et.name() and iterate over the aforementioned id value
		//for loop{//iterate
			String message = null; //get message for each sql
			long timestampLong = 0; // get timestamp
			double x = 0,y = 0,z =0; //you'll need to cast the xyz integers into doubles for this
			String worldString = null; //get the world name
			World world = Bukkit.getWorld(worldString); //get the world instance of the worldString
			Location l = new Location(world,x,y,z);
			if(!(origin.distance(l)>distance)){
				finds.add("["+Utils.getDate(timestampLong)+"]: "+message+" at x: "+x+" y: "+y+" z: "+z);
			}
		//}
		return finds;
	}
}
