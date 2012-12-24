package me.corriekay.pppopp3.modules;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityLogger extends PSCmdExe{

	private final FileConfiguration config;
	private Connection databaseConnection;
	private final HashMap<String,ArrayList<String>> playerFinds = new HashMap<String,ArrayList<String>>();

	public EntityLogger() throws Exception{
		super("EntityLogger", "finddeaths");
		Method m = getClass().getDeclaredMethod("entityEvent", EntityDeathEvent.class);
		methodMap.put(EntityDeathEvent.class, m);
		config = getNamedConfig("EntitySql.yml");
		connectToDatabase(config.getString("host"), config.getInt("port"), config.getString("database"), config.getString("login"), config.getString("password"));
	}

	private void displayPage(Player p, int i){
		ArrayList<String> finds = playerFinds.get(p.getName());
		if(finds == null) {
			sendMessage(p, "No pages!");
		} else {
			int pages = (int)Math.ceil((double)finds.size() / 15);
			if(i > pages) {
				sendMessage(p, "Page not found!");
			} else {
				int maxPage = ((i - 1) * 15) + 15;
				p.sendMessage(ChatColor.GOLD + "Finds, page " + i + "/" + pages);
				for(int page = ((i - 1) * 15); (page < maxPage) || (page > finds.size()); page++) {
					String s;
					try {
						s = finds.get(page);
					} catch(IndexOutOfBoundsException e) {
						return;
					}
					p.sendMessage(ChatColor.GOLD + s);
				}
			}
		}
	}

	public boolean handleCommand(final CommandSender sender, Command cmd, String label, final String[] args){
		if(cmd.getName().equals("finddeaths")) {
			// /finddeaths <distance> <mobtype> <days>
			if(!(sender instanceof Player)) {
				sendMessage(sender, notPlayer);
				return true;
			}
			if(args.length == 0) {
				sendMessage(sender, "Find mob deaths by specifying any of the following: mob <mob>, area <distance>, and since <time> (example, would be 24h5m for 24 hours and 5 minutes (d,h,m,s). If you have logs, type /finddeaths page # to look at a specific page.");
				return true;
			}
			final Player player = (Player)sender;
			if(args[0].equals("page")) {
				if(args.length < 2) {
					sendMessage(player, notEnoughArgs);
					return true;
				}
				ArrayList<String> pages = playerFinds.get(player.getName());
				if(pages == null) {
					sendMessage(player, "No logs found!");
					return true;
				} else {
					try {
						int selPage = Integer.parseInt(args[1]);
						displayPage(player, selPage);
						return true;
					} catch(NumberFormatException e) {
						sendMessage(player, "Thats not a number!");
						return true;
					}
				}
			}
			Bukkit.getScheduler().runTaskLaterAsynchronously(Mane.getInstance(), new Runnable() {
				public void run(){
					try {
						ArrayList<String> finds = parseSQL(player.getLocation(), args);
						Collections.reverse(finds);
						if(finds.size() == 0) {
							finds = null;
						}
						playerFinds.put(player.getName(), finds);
						displayPage(player, 1);
						return;
					} catch(Exception e) {
						sendMessage(sender, "Check your arguments! Something went wrong!");
						return;
					}
				}
			}, 0);

		}
		return true;
	}

	@EventHandler
	public void entityExplode(EntityExplodeEvent event){
		if(event.getEntity() instanceof Creeper) {
			Creeper c = (Creeper)event.getEntity();
			Entity target = c.getTarget();
			String message = "Creeper exploded";
			if(target != null) {
				message += " while targetting " + target.getType().name().toLowerCase();
				if(target instanceof Player) {
					Player p = (Player)target;
					message += " " + p.getName();
				}
			}
			logAttack(message, EntityType.CREEPER, c.getLocation(), System.currentTimeMillis(), event);
		}
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
		if(e instanceof Tameable) {
			Tameable t = (Tameable)e;
			if(t.isTamed()) {
				deadEntity = t.getOwner().getName() + "'s " + e.getType().name().toLowerCase();
			}
		}
		if(e instanceof Sheep) {
			Sheep s = (Sheep)e;
			deadEntity = s.getColor().name().toLowerCase() + " sheep";
		}
		if(e instanceof Villager) {
			Villager v = (Villager)e;
			deadEntity = v.getProfession().name().toLowerCase() + " villager";
		}
		if(e instanceof Player) {
			Player p = (Player)e;
			deadEntity = "player " + p.getName();
		}

		//document type of death
		EntityDamageEvent ede = e.getLastDamageCause();
		if(ede instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)ede;
			Entity killer = edbee.getDamager();
			if(killer instanceof Projectile) {
				Projectile p = (Projectile)killer;
				killer = p.getShooter();
			}
			if(killer instanceof Creature) {
				Creature creature = (Creature)killer;
				Entity target = creature.getTarget();
				if(target instanceof Player) {
					Player targetP = (Player)target;
					targetEntity = "player " + targetP.getName();
				} else {
					targetEntity = target.getType().name().toLowerCase();
				}
			}
			if(killer instanceof Player) {
				Player player = (Player)killer;
				whokilled = "player " + player.getName();
			} else {
				whokilled = killer.getType().name().toLowerCase();
			}
		} else {
			try {
				whokilled = ede.getCause().name().toLowerCase();
			} catch(NullPointerException e1) {
				whokilled = "Unknown killer (probably sun death)";
			}
		}
		String message;
		message = whokilled + " killed " + deadEntity;
		if(targetEntity != null) {
			message += " while targetting " + targetEntity;
		}
		logAttack(message, e.getType(), l, System.currentTimeMillis(), event);
	}

	/**
	 * Connects the server to the database.  Happens OaOO (Once and Only Once) 
	 * to prevent it from bogging down the server.
	 */
	private void connectToDatabase(String host, int port, String database, String user, String password){
		System.out.println("Connecting to the entity logging SQL database...");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
			databaseConnection = DriverManager.getConnection(url, user, password);
			System.out.println("Connected to the entity logging SQL database!");
		} catch(Exception e) {
			System.out.println("Failed to connect to the entity logging MySQL database!");
		}

	}

	private void logAttack(final String message, final EntityType et, final Location loc, final long timestamp, final Event event){
		//begin SparCode
		final String eName = et.getName().toLowerCase();
		final String world = loc.getWorld().getName();
		final int x = loc.getBlockX();
		final int y = loc.getBlockY();
		final int z = loc.getBlockZ();
		// Automatically create the table if it doesn't exist already
		// If you want to go back to the old idea of grouping deaths by table per entity...
		// replace any occurrence of entities with " + eName + "
		// and delete the "entity varchar (32) not null line.
		Bukkit.getScheduler().runTaskAsynchronously(Mane.getInstance(), new Runnable() {
			public void run(){
				try {
					executeSQL("CREATE TABLE IF NOT EXISTS entities (" + "entity varchar(32) not null, " + "timestamp varchar(20) not null, " + "world varchar(32) not null, " + "message varchar(128) null, " + "x varchar(6) not null, " + "y varchar(6) not null, " + "z varchar(6) not null)", true);
					// Now write the entity stuff
					executeSQL("INSERT INTO entities " + "VALUES ('" + eName + "', '" + timestamp + "', '" + world + "', '" + message + "', '" + x + "', '" + y + "', '" + z + "')", true);
					//end SparCode
				} catch(final SQLException se) {
					final String exceptionMessage = "Error on event: " + se.getMessage() + "\n" + "\n--- SQL Command Error ---\n" + "MySQL Error Code:  " + se.getErrorCode() + " State: " + se.getSQLState();;
					Bukkit.getScheduler().runTask(Mane.getInstance(), new Runnable() {
						public void run(){
							PonyLogger.logListenerException(se, exceptionMessage, name, event.getClass().getCanonicalName());
						}
					});
				}
			}
		});
	}

	// Command usage:  /finddeaths [mob [creeper | skeleton | enderman | cow | sheep | chicken | slime | zombie | pig | player]] 
	//							[since [time]]
	//							[area [distance]]
	//							[coords]

	private ArrayList<String> parseSQL(Location origin, String args[]){//EntityType et, int days, int distance, Location origin){
		ArrayList<String> finds = new ArrayList<String>();

		ArrayList<String> pArgs = new ArrayList<String>(Arrays.asList(args));
		String entity = "";
		long since = 0;
		int distance = 100000;
		boolean coordinates = false;
		for(int i = 0; i < pArgs.size(); i++) {
			if(pArgs.get(i).equalsIgnoreCase("mob")) {
				if(pArgs.size() == i + 1) {
					return null; // Out of bounds
				}
				entity = pArgs.get(i + 1).toLowerCase();
				if(entity.equals("irongolem")) {
					entity = "villagergolem";
				}
				if(entity.equals("ocelot")) {
					entity = "ozelot";
				}
				if(entity.equals("snowgolem")) {
					entity = "snowman";
				}
				pArgs.remove(i + 1);
				pArgs.remove(i);
				i = -1;
				continue;
			}
			if(pArgs.get(i).equalsIgnoreCase("since")) {
				if(pArgs.size() == i + 1) {
					return null; // Out of bounds
				}
				String toParse = pArgs.get(i + 1);
				long tstamp = 0;
				String validate = toParse;
				validate = validate.replaceAll("[1234567890DdHhMmSs]", "");
				if(!validate.isEmpty()) {
					// Invalid input
					return null;
				}
				String[] splitString = toParse.split("(?<=[dhms])");
				for(String s : splitString) {
					if(s.endsWith("d")) {
						s = s.substring(0, s.length() - 1);
						int dVal;
						try {
							dVal = Integer.parseInt(s);
						} catch(NumberFormatException nfe) {
							return null;
						}
						tstamp += dVal * 86400; // to seconds
					}
					if(s.endsWith("h")) {
						s = s.substring(0, s.length() - 1);
						int hVal;
						try {
							hVal = Integer.parseInt(s);
						} catch(NumberFormatException nfe) {
							return null;
						}
						tstamp += hVal * 3600; // to seconds
					}
					if(s.endsWith("m")) {
						s = s.substring(0, s.length() - 1);
						int mVal;
						try {
							mVal = Integer.parseInt(s);
						} catch(NumberFormatException nfe) {
							return null;
						}
						tstamp += mVal * 60; // to seconds
					}
					if(s.endsWith("s")) {
						s = s.substring(0, s.length() - 1);
						int sVal;
						try {
							sVal = Integer.parseInt(s);
						} catch(NumberFormatException nfe) {
							return null;
						}
						tstamp += sVal;
					}

				}
				since = tstamp * 1000;
				pArgs.remove(i + 1);
				pArgs.remove(i);
				i = -1;
				continue;
			}
			if(pArgs.get(i).equalsIgnoreCase("area")) {
				if(pArgs.size() == i + 1) {
					return null; // Out of bounds
				}
				try {
					distance = Integer.parseInt(pArgs.get(i + 1));
				} catch(NumberFormatException nfe) {
					return null;
				}
				pArgs.remove(i + 1);
				pArgs.remove(i);
				i = -1;
				continue;
			}
			if(pArgs.get(i).equalsIgnoreCase("coords")) {
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
			ResultSet rs = executeSQL("SELECT * " + "FROM entities " + "WHERE (" + (entity.isEmpty() ? "" : "entity = '" + entity + "' AND ") + (since == 0 ? "" : "" + "timestamp > " + (System.currentTimeMillis() - since) + " AND ") + "(x > " + (playerX - distance) + " AND x < " + (playerX + distance) + ") AND " + "(y > " + (playerY - distance) + " AND y < " + (playerY + distance) + ") AND " + "(z > " + (playerZ - distance) + " AND z < " + (playerZ + distance) + ") AND " + "world = '" + origin.getWorld().getName() + "')", false);
			while(rs.next()) {
				String message = rs.getString("message"); //get message for each sql
				long timestampLong = Long.parseLong(rs.getString("timestamp")); // get timestamp
				int x = (int)Double.parseDouble(rs.getString("x"));
				int y = (int)Double.parseDouble(rs.getString("y"));
				int z = (int)Double.parseDouble(rs.getString("z"));
				String addTo = "[" + Utils.getDate(timestampLong) + "]: " + message;
				if(coordinates) {
					addTo += " at " + x + ", " + y + ", " + z;
				}
				finds.add(addTo);
			}

		} catch(SQLException se) {
			String message = "Exception thrown on command: finddeaths";
			message += "\nArgs: ";
			for(String arg : args) {
				message += arg + " ";
			}
			message += "\n--- SQL Command Error ---\n";
			message += "MySQL Error Code:  " + se.getErrorCode() + " State: " + se.getSQLState();
			PonyLogger.logCmdException(se, message, name);
			sendMessage(console, "SQL Error!  Check the log for more details.");
		}

		// Corrie, you may find it useful to check if finds contains any
		// data.  It's possible that nothing will be returned for the 
		// parameters defined.
		return finds;
	}

	/**
	 * Method to be used to execute SQL commands (meant for entity logging)
	 * @param query Query to execute on the SQL database.
	 * @param update If issuing an update (INSERT, CREATE, etc.), this should
	 *			   be true.  Otherwise, if issuing a query (SELECT), this
	 *			   should be false.
	 * @param args Arguments the user passed to the method.
	 * @return The result set of values returned by the query.
	 */
	private ResultSet executeSQL(String query, boolean update) throws SQLException{
		Statement stmt = databaseConnection.createStatement();
		// Automatically append a semicolon if the statement isn't already ended with one
		if(!query.endsWith(";")) {
			query = query + ';';
		}
		if(update) {
			stmt.executeUpdate(query);
		} else {
			return(stmt.executeQuery(query));
		}
		return(null);
	}
}
