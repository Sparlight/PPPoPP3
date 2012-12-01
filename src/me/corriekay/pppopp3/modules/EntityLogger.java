package me.corriekay.pppopp3.modules;

import java.io.File;
import java.sql.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.PonyLogger;
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
        
        private Connection databaseConnection;
        
	public EntityLogger() throws Exception{
		super("EntityLogger",new String[]{"finddeaths"});
		Method m = getClass().getDeclaredMethod("entityEvent",EntityDeathEvent.class);
		methodMap.put(EntityDeathEvent.class, m);
		File file = new File(Mane.getInstance().getDataFolder(),"EntitySql.yml");
		if(!file.exists()){
			file.createNewFile();
		}
		config = YamlConfiguration.loadConfiguration(file);
                connectToDatabase(config.getString("host"), config.getInt("port"),
                        config.getString("database"), config.getString("login"),
                        config.getString("password"));
	}
	public boolean handleCommand(final CommandSender sender, Command cmd, String label, final String[] args){
		if(cmd.getName().equals("finddeaths")){
			// /finddeaths <distance> <mobtype> <days>
			if(args.length == 0){
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
                                                ArrayList<String> finds = parseSQL(((Player)sender).getLocation(), args);
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
        
        /**
         * Connects the server to the database.  Happens OaOO (Once and Only Once) 
         * to prevent it from bogging down the server.
         */
        private void connectToDatabase(String host, int port, String database, String user, String password) {
            System.out.println("Connecting to the entity logging SQL database...");
            try {
                Class.forName("com.mysql.jdbc.Driver"); 
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
                databaseConnection = DriverManager.getConnection(url, user, password);
                System.out.println("Connected to the entity logging SQL database!");
            } catch (Exception e) {
                System.out.println("Failed to connect to the entity logging MySQL database!");
            }
                
        }
        
	private void logAttack(String message, EntityType et, Location loc, long timestamp) {
		//begin SparCode
                String eName = et.getName().toLowerCase();
                String world = loc.getWorld().getName();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();
                // Automatically create the table if it doesn't exist already
                // If you want to go back to the old idea of grouping deaths by table per entity...
                // replace any occurrence of entities with " + eName + "
                // and delete the "entity varchar (32) not null line.
                try {
                    executeSQL("CREATE TABLE IF NOT EXISTS entities ("
                            + "entity varchar(32) not null, "
                            + "timestamp varchar(20) not null, "
                            + "world varchar(32) not null, "
                            + "message varchar(128) null, "
                            + "x varchar(6) not null, "
                            + "y varchar(6) not null, "
                            + "z varchar(6) not null)", true);
                    // Now write the entity stuff
                    executeSQL("INSERT INTO entities "
                            + "VALUES ('" + eName + "', '" + timestamp + "', '"
                                          + world + "', '" + message + "', '" + x + "', '"
                                           + y + "', '" + z + "')", true);
                
            } catch (SQLException se) {
                // TODO CorrieCode
                // Sorry :<
            }
		//end SparCode
          } 
        // Command usage:  /finddeaths [mob [creeper | skeleton | enderman | cow | sheep | chicken | slime | zombie | pig | player]] 
        //                            [since [time]]
        //                            [area [distance]]
        //                            [coords]

	private ArrayList<String> parseSQL(Location origin, String args[]){//EntityType et, int days, int distance, Location origin){
		ArrayList<String> finds = new ArrayList<String>();
		//TODO
		//get the sql table via et.name() and iterate over the aforementioned id value
		//for loop{//iterate
                
                ArrayList <String> pArgs = new ArrayList<String>(Arrays.asList(args));
                String entity = "";
                long since = 0;
                int distance = 100000;
                boolean coordinates = false;
                for (int i = 0; i < pArgs.size(); i++) {
                    if (pArgs.get(i).equalsIgnoreCase("mob")) {
                        if (pArgs.size() == i + 1) {
                            return null; // Out of bounds
                        }
                        entity = pArgs.get(i+1).toLowerCase();
                        if (entity.equals("irongolem")) {
                            entity = "villagergolem";
                        }
                        if (entity.equals("ocelot"))
                        {
                            entity = "ozelot";
                        }
                        if (entity.equals("snowgolem"))
                        {
                            entity = "snowman";
                        }
                        pArgs.remove(i+1);
                        pArgs.remove(i);
                        i = -1;
                        continue;
                    }
                    if (pArgs.get(i).equalsIgnoreCase("since")) {
                        if (pArgs.size() == i + 1) {
                            return null; // Out of bounds
                        }
                        String toParse = pArgs.get(i+1);
                        long tstamp = 0;
                        String validate = toParse;
                        validate = validate.replaceAll("[1234567890DdHhMmSs]", "");
                        if (!validate.isEmpty()){
                            // Invalid input
                            return null;
                        }
                        String[] splitString = toParse.split("(?<=[dhms])");
                        for (String s : splitString) {
                            if (s.endsWith("d")) {
                                s = s.substring(0, s.length() - 1);
                                int dVal;
                                try { 
                                    dVal = Integer.parseInt(s);
                                } catch (NumberFormatException nfe) {
                                    return null;
                                }
                                tstamp += dVal * 86400; // to seconds
                            }
                            if (s.endsWith("h")) {
                                s = s.substring(0, s.length() - 1);
                                int hVal;
                                try { 
                                    hVal = Integer.parseInt(s);
                                } catch (NumberFormatException nfe) {
                                    return null;
                                }
                                tstamp += hVal * 3600; // to seconds
                            }
                            if (s.endsWith("m")) {
                                s = s.substring(0, s.length() - 1);
                                int mVal;
                                try { 
                                    mVal = Integer.parseInt(s);
                                } catch (NumberFormatException nfe) {
                                    return null;
                                }
                                tstamp += mVal * 60; // to seconds
                            }
                            if (s.endsWith("s")) {
                                s = s.substring(0, s.length() - 1);
                                int sVal;
                                try { 
                                    sVal = Integer.parseInt(s);
                                } catch (NumberFormatException nfe) {
                                    return null;
                                }
                                tstamp += sVal;
                            }
                            
                        }
                        since = tstamp * 1000;
                        pArgs.remove(i+1);
                        pArgs.remove(i);
                        i = -1;
                        continue;
                    }
                    if (pArgs.get(i).equalsIgnoreCase("area")) {
                        if (pArgs.size() == i + 1) {
                            return null; // Out of bounds
                        }
                        try {
                            distance = Integer.parseInt(pArgs.get(i+1));
                        } catch (NumberFormatException nfe) {
                            return null;
                        }
                        pArgs.remove(i+1);
                        pArgs.remove(i);
                        i = -1;
                        continue;
                    }
                    if (pArgs.get(i).equalsIgnoreCase("coords")) {
                        coordinates = true;
                        pArgs.remove(i);
                        i = -1;
                        continue;
                    }
                }
                
                int playerX = origin.getBlockX();
                int playerY = origin.getBlockY();
                int playerZ = origin.getBlockZ();

                try {
                    ResultSet rs = executeSQL("SELECT * "
                            + "FROM entities "
                            + "WHERE ("
                            + (entity.isEmpty()?"":"entity = '" + entity + "' AND ")
                            + (since == 0?"":"" + "timestamp > " + (System.currentTimeMillis() - since) + " AND ")
                            + "(x > " + (playerX - distance) + " AND x < " + (playerX + distance) + ") AND "
                            + "(y > " + (playerY - distance) + " AND y < " + (playerY + distance) + ") AND "
                            + "(z > " + (playerZ - distance) + " AND z < " + (playerZ + distance) + ") AND "
                            + "world = '" + origin.getWorld().getName() + "')", false);
                    while (rs.next())
                    {
                        String message = rs.getString("message"); //get message for each sql
                        long timestampLong = Long.parseLong(rs.getString("timestamp")); // get timestamp
                        double x = Double.parseDouble(rs.getString("x"));
                        double y = Double.parseDouble(rs.getString("y"));
                        double z = Double.parseDouble(rs.getString("z"));
                        String worldString = rs.getString("world"); //get the world name
                        World world = Bukkit.getWorld(worldString); //get the world instance of the worldString
                        Location l = new Location(world,x,y,z);
                        finds.add("["+Utils.getDate(timestampLong)+"]: "+message+" at x: "+x+" y: "+y+" z: "+z);
                    }
                
                } catch (SQLException se) {
                    String message = "Exception thrown on command: finddeaths";
                    message +="\nArgs: ";
                    for (String arg : args)
                    {
                        message+= arg + " ";
                    }
                    message += "\n--- SQL Command Error ---\n";
                    message += "MySQL Error Code:  " + se.getErrorCode() + " State: " + se.getSQLState();
                    message += "\nStack Trace (including generated error):  \n";
                    for (StackTraceElement ste : se.getStackTrace())
                    {
                        message += ste;
                    }
                    PonyLogger.logCmdException(se, message, name);

                    sendMessage(console, "SQL Error!  Check the log for more details.");
                }
		//}
                
                // Corrie, you may find it useful to check if finds contains any
                // data.  It's possible that nothing will be returned for the 
                // parameters defined.
		return finds;
	}

        /**
         * Method to be used to execute SQL commands (meant for entity logging)
         * @param query Query to execute on the SQL database.
         * @param update If issuing an update (INSERT, CREATE, etc.), this should
         *               be true.  Otherwise, if issuing a query (SELECT), this
         *               should be false.
         * @param args Arguments the user passed to the method.
         * @return The result set of values returned by the query.
         */
        private ResultSet executeSQL(String query, boolean update) throws SQLException{
                Statement stmt = databaseConnection.createStatement();
                // Automatically append a semicolon if the statement isn't already ended with one
                if (!query.endsWith(";")){
                    query = query + ';';
                }
                if (update)
                {
                    stmt.executeUpdate(query);
                } else {
                    return (stmt.executeQuery(query));
                }
            return(null);
        }
}
