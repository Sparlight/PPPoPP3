package me.corriekay.pppopp3.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.rpa.RemotePonyAdmin;
import me.corriekay.pppopp3.utils.IOP;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.Utils;
import me.corriekay.pppopp3.warp.WarpHandler;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class AdminHandler extends PSCmdExe{
	private final int spawncount = 10;
	private final HashMap<String,Location> roundup = new HashMap<String,Location>();
	private final HashMap<String,String> viewingEnderchest = new HashMap<String,String>();

	public AdminHandler(){
		super("AdminHandler", "viewenderchest", "playerinformation", "rcv", "j", "tploc", "runcustomtask", "mobtype", "extinguish", "spawnmob", "killmob", "heal", "sudo", "roundup", "fullrollback");
	}

	private HashSet<String> convertArrayString(List<String> array){
		HashSet<String> set = new HashSet<String>();
		for(String s : array) {
			set.add(s);
		}
		return set;
	}

	public boolean handleCommand(final CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("viewenderchest")) {
			if(!(sender instanceof Player)) {
				sendMessage(sender, notPlayer);
				return true;
			}
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			Player player = (Player)sender;
			OfflinePlayer target = getOnlineOfflinePlayer(args[0], player);
			if(target == null) {
				return true;
			}
			{
				OfflinePlayer op = Bukkit.getOfflinePlayer(target.getName());
				if(op.hasPlayedBefore() && op.isOnline()) {
					Player p = (Player)op;
					player.openInventory(p.getEnderChest());
					sendMessage(player, "Player is online! Editing enderchest in real time!");
					return true;
				}
			}
			IOP op = new IOP(target);
			if(!op.exists()) {
				sendMessage(player, cantFindPlayer);
				return true;
			}
			player.openInventory(op.getEnderChest());
			viewingEnderchest.put(player.getName(), op.getName());
			sendMessage(player, "Showing " + op.getName() + "'s enderchest!");
			return true;
		}
		if(cmd.getName().equals("runcustomtask")) {
			Bukkit.getScheduler().runTaskAsynchronously(Mane.getInstance(), new Runnable() {
				public void run(){
					File dir = new File(Mane.getInstance().getDataFolder() + File.separator + "Old Players");
					int errors = 0;
					int total = dir.listFiles().length + 1;
					int current = 1;
					int percentage = 0;
					for(File file : dir.listFiles()) {
						int thisPercentage = (int)Math.floor((current / total) * 100);
						if(thisPercentage != percentage) {
							System.out.println("Files parsed: " + thisPercentage + "%");
							percentage = thisPercentage;
						}
						current++;
						try {
							FileConfiguration config = YamlConfiguration.loadConfiguration(file);
							OfflinePlayer op = Bukkit.getOfflinePlayer(config.getName());
							//CODE
							Pony pony = Pony.moveToPonyville(config.getString("name"), "", Bukkit.getOfflinePlayer(config.getName()).getFirstPlayed());
							pony.setNickname(config.getString("nickname"));
							pony.setMuted(config.getBoolean("muted"));
							pony.setSilenced(convertArrayString(config.getStringList("silenced")));
							pony.setGodMode(config.getBoolean("god"));
							pony.setInvisible(config.getBoolean("invisible"));
							pony.setNickHistory((ArrayList<String>)config.getStringList("nickHistory"));
							pony.setChatChannel(config.getString("chatChannel"));
							pony.setListenChannels(convertArrayString(config.getStringList("listenChannels")));
							pony.setPonySpy(config.getBoolean("ponyspy"));
							pony.setGroup(config.getString("group"));
							pony.setPerms((ArrayList<String>)config.getStringList("perms"));
							pony.setIps((ArrayList<String>)config.getStringList("ipAddress"));
							pony.setHornLeft(config.getString("horn.left"));
							pony.setHornRight(config.getString("horn.right"));
							pony.setHornOn(config.getBoolean("horn.isOn"));
							pony.setEmoteName(config.getString("emote.name"));
							pony.setEmoteSender(config.getString("emote.sender"));
							pony.setEmoteReceiver(config.getString("emote.receiver"));
							pony.setEmoteServer(config.getString("emote.server"));
							pony.setEmotePrivate(config.getBoolean("emote.private"));
							pony.setEmoteSilent(config.getBoolean("emote.silent"));
							pony.setFirstLogon(config.getString("firstLogon"));
							pony.setLastLogon(config.getString("lastLogon"));
							pony.setLastLogout(config.getString("lastLogout"));
							pony.setHomeWarp(Utils.getLoc((ArrayList<String>)config.getStringList("warps.other.home")));
							pony.setOfflineWarp(Utils.getLoc((ArrayList<String>)config.getStringList("warps.other.offline")));
							pony.setBackWarp(Utils.getLoc((ArrayList<String>)config.getStringList("warps.other.back")));
							for(String s : config.getConfigurationSection("warps").getKeys(false)) {
								if(!s.equals("other")) {
									pony.setNamedWarp(s, Utils.getLoc((ArrayList<String>)config.getStringList("warps." + s)));
								}
							}
							pony.setBanned(config.getBoolean("ban.banned"));
							if(pony.isBanned()) {
								pony.setBanType(config.getString("ban.banType").equals("permaban") ? 2 : 1);
							} else {
								pony.setBanType(0);
							}
							if(pony.getBanType() == 0 && op.isBanned()) {
								Mane.getInstance().getLogger().severe("ALERT, PLAYER CONFIGURATION DESYNCHRONIZED FROM BAN LIST: " + op.getName());
							}
							pony.setUnbanTime(config.getLong("ban.unbanTime", 0));
							pony.setNotes((ArrayList<String>)config.getStringList("notes"));
							pony.save();
							try {
								IOP ponyOP = new IOP(pony.getName());
								if(ponyOP.exists()) {
									Pony.setWorldStats(ponyOP, pony, "world");
									pony.setInventory(ponyOP.getInventory(), "world");
								}

							} catch(Exception e) {
								e.printStackTrace();
							}
							pony.save();
							//CODE
						} catch(Exception e) {
							errors++;
							Mane.getInstance().getLogger().severe("Error on parsing config: " + file.getName());
							e.printStackTrace();
						}
					}
					sendMessage(sender, "Configs converted! " + errors + " errors!");
				}
			});
		}
		if(cmd.getName().equals("fullrollback")) {
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			OfflinePlayer target = getOnlineOfflinePlayer(args[0], sender);
			if(target == null) {
				return true;
			}
			target = Bukkit.getOfflinePlayer(target.getName());
			if(target.isOnline()) {
				((Player)target).kickPlayer("Your account has been fully rolled back. Because of this, you are required to rejoin the server.");
			}
			IOP op = new IOP(target.getName());
			Pony pony = Ponyville.getOfflinePony(target.getName());
			Inventory i = op.getEnderChest();
			i.clear();
			op.setEnderChest(i);
			op.setLocation(WarpHandler.getWorldSpawn());
			op.setGameMode(GameMode.SURVIVAL);
			op.savePlayerData();
			pony.setGodMode(false);
			pony.setBackWarp(null);
			pony.setHomeWarp(null);
			pony.setOfflineWarp(null);
			for(String warp : pony.getAllNamedWarps().keySet()) {
				pony.removeNamedWarp(warp);
			}
			pony.wipeWorldStats();
			for(World w : Equestria.get().getParentWorlds()) {
				PlayerInventory pi = pony.getInventory(w.getName());
				pi.clear();
				pony.setInventory(pi, w.getName());
			}
			for(World w : Equestria.get().getParentWorlds()) {
				if(!w.getName().equals("equestria")) {
					pony.getRemoteChest(w).clear();
					pony.saveRemoteChest(w);
				}
			}
			pony.save();
			sendMessage(sender, "User inventory and warps are wiped!");
			PonyLogger.logAdmin(sender, "Full rollbacked " + target);
			return true;
		}
		if(cmd.getName().equals("mobtype")) {
			Player player;
			if(sender instanceof Player) {
				player = (Player)sender;
			} else {
				sendMessage(sender, notPlayer);
				return true;
			}
			Block block = player.getTargetBlock(null, 100);
			if(block.getState() instanceof CreatureSpawner) {
				CreatureSpawner cs = (CreatureSpawner)block.getState();
				sendMessage(player, "That mobspawner spawns " + cs.getCreatureTypeName().toLowerCase() + "!");
				return true;
			}
		}
		if(cmd.getName().equals("j")) {
			if(sender instanceof Player) {
				Player p = (Player)sender;
				List<Block> blocks = p.getLineOfSight(null, 100);
				p.teleport(blocks.get(blocks.size() - 2).getLocation());
				//				Location loc = p.getTargetBlock(null, 1000).getLocation();
				//				loc.setY(loc.getY() + 1);
				//				loc.setPitch(p.getLocation().getPitch());
				//				loc.setYaw(p.getLocation().getYaw());
				//				p.teleport(loc);
				return true;
			} else {
				sendMessage(sender, notPlayer);
				return true;
			}
		}
		if(cmd.getName().equals("tploc")) {
			Player player;
			if(sender instanceof Player) {
				player = (Player)sender;
			} else {
				sendMessage(sender, notPlayer);
				return true;
			}
			if(args.length <= 1) {
				sendMessage(player, "theres not enough coordinates here!");
				return true;
			}
			Location loc;
			if(args.length == 2) {
				try {
					loc = new Location(player.getWorld(), Double.parseDouble(args[0]), 0, Double.parseDouble(args[1]));
				} catch(IllegalArgumentException e) {
					sendMessage(player, "Silly, those arent coordinates!");
					return true;
				}
				boolean Continue = true;
				loc.setY(player.getWorld().getMaxHeight() - 2);
				while(Continue) {
					if(loc.getBlock().getType() != Material.AIR || loc.getY() == 0) {
						Continue = false;
					}
					loc.setY(loc.getY() - 1);
				}
				loc.setY(loc.getY() + 1);
			} else {
				try {
					loc = new Location(player.getWorld(), Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
				} catch(IllegalArgumentException e) {
					sendMessage(player, "Silly, those arent coordinates!");
					return true;
				}
			}
			player.teleport(loc);
			return true;
		}
		if(cmd.getName().equals("playerinformation")) {
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			OfflinePlayer target = getOnlineOfflinePlayer(args[0], sender);
			if(target == null) {
				return true;
			}
			Pony pony = Ponyville.getOfflinePony(target.getName());
			sender.sendMessage(ChatColor.DARK_RED + "===== " + ChatColor.WHITE + pony.getName() + "'s info" + ChatColor.DARK_RED + " =====");
			sender.sendMessage(ChatColor.DARK_RED + "Nickname: " + ChatColor.WHITE + pony.getNickname());
			sender.sendMessage(ChatColor.DARK_RED + "Nickname History:");
			for(String nickname : pony.getNickHistory()) {
				sender.sendMessage("- " + nickname);
			}
			sender.sendMessage(ChatColor.DARK_RED + "First seen: " + ChatColor.WHITE + pony.getFirstLogon());
			sender.sendMessage(ChatColor.DARK_RED + "Last seen: " + ChatColor.WHITE + pony.getLastLogout());
			sender.sendMessage(ChatColor.DARK_RED + "Last login: " + ChatColor.WHITE + pony.getLastLogon());
			sender.sendMessage(ChatColor.DARK_RED + "Online: " + ChatColor.WHITE + target.isOnline());
			sender.sendMessage(ChatColor.DARK_RED + "Ip Addresses:");
			for(String s : pony.getIps()) {
				sender.sendMessage(ChatColor.DARK_RED + "- " + s);
			}
			sender.sendMessage(ChatColor.DARK_RED + "Muted: " + ChatColor.WHITE + pony.isMuted());
			sender.sendMessage(ChatColor.DARK_RED + "God: " + ChatColor.WHITE + pony.isGodMode());
			sender.sendMessage(ChatColor.DARK_RED + "Chatting in: " + ChatColor.WHITE + pony.getChatChannel());
			sender.sendMessage(ChatColor.DARK_RED + "Listening channels:");
			for(String chan : pony.getListeningChannels()) {
				sender.sendMessage("- " + chan);
			}
			sender.sendMessage(ChatColor.DARK_RED + "PonySpy: " + ChatColor.WHITE + pony.isPonySpy());
			sender.sendMessage(ChatColor.DARK_RED + "PonyManager group: " + ChatColor.WHITE + pony.getGroup());
			sender.sendMessage(ChatColor.DARK_RED + "Banned: " + ChatColor.WHITE + pony.isBanned());
			if(pony.isBanned()) {
				sender.sendMessage(ChatColor.DARK_RED + "Ban Type: " + ChatColor.WHITE + (pony.getBanType() == 1 ? "Tempban" : "Permaban"));
			}
			sender.sendMessage(ChatColor.DARK_RED + "====================");
		}
		if(cmd.getName().equals("rcv")) {
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			String MESSAGE = "";
			for(String WORD : args) {
				MESSAGE += WORD.toUpperCase() + " ";
			}
			String NAME = "";
			if(sender instanceof ConsoleCommandSender) {
				NAME = "CONSOLE";
			} else if(sender instanceof Player) {
				NAME = ((Player)sender).getDisplayName();
			}
			for(Player ROYALSUBJECT : Bukkit.getOnlinePlayers()) {
				ROYALSUBJECT.sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[ROYALCANTERLOTVOICE](" + NAME + ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "): " + ChatColor.BLUE + ChatColor.BOLD + MESSAGE);
			}
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[ROYALCANTERLOTVOICE](" + NAME + ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "): " + ChatColor.BLUE + ChatColor.BOLD + MESSAGE);
			RemotePonyAdmin.rpa.message(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "[ROYALCANTERLOTVOICE](" + NAME + ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "): " + ChatColor.BLUE + ChatColor.BOLD + MESSAGE);
			return true;
		}
		if(cmd.getName().equals("extinguish")) {
			if(!(sender instanceof Player)) {
				sendMessage(sender, notPlayer);
				return true;
			}
			int x = 25;
			if(args.length > 0) {
				try {
					x = Integer.parseInt(args[0]);
				} catch(NumberFormatException e) {
					sendMessage(sender, "Thats not a number! defaulting to 25 radius!");
					x = 25;
				}
			}
			if(x > 100) {
				x = 100;
			}
			Player player = (Player)sender;
			HashSet<Block> blocks = Utils.getBlocks(x, x, x, player.getLocation());
			for(Block block : blocks) {
				if(block.getType() == Material.FIRE) {
					block.setType(Material.AIR);
				}
			}
			return true;
		}
		if(cmd.getName().equals("spawnmob")) {
			int number = 1;
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			EntityType et;
			String mtString = args[0].toLowerCase();
			if(mtString.equals("zombie")) {
				et = EntityType.ZOMBIE;
			} else if(mtString.equals("skeleton")) {
				et = EntityType.SKELETON;
			} else if(mtString.equals("spider")) {
				et = EntityType.SPIDER;
			} else if(mtString.equals("creeper")) {
				et = EntityType.CREEPER;
			} else if(mtString.equals("cavespider")) {
				et = EntityType.CAVE_SPIDER;
			} else if(mtString.equals("sheep")) {
				et = EntityType.SHEEP;
			} else if(mtString.equals("pig")) {
				et = EntityType.PIG;
			} else if(mtString.equals("cow")) {
				et = EntityType.COW;
			} else if(mtString.equals("wolf")) {
				et = EntityType.WOLF;
			} else if(mtString.equals("ocelot")) {
				et = EntityType.OCELOT;
			} else if(mtString.equals("chicken")) {
				et = EntityType.CHICKEN;
			} else if(mtString.equals("squid")) {
				et = EntityType.SQUID;
			} else {
				sendMessage(sender, "Uh uh! thats not a spawnable mob!");
				return true;
			}
			Location loc;
			if(label.equals("spawnmobtarget")) {
				if(args.length < 2) {
					sendMessage(sender, notEnoughArgs);
					return true;
				} else {
					Player p = getOnlinePlayer(args[1], sender);
					if(p == null) {
						return true;
					} else {
						loc = p.getLocation();
					}
					if(args.length >= 3) {
						try {
							number = Integer.parseInt(args[2]);
						} catch(NumberFormatException e) {
							sendMessage(sender, "Thats not a number!");
							return true;
						}
					}
				}
			} else {
				if(!(sender instanceof Player)) {
					sendMessage(sender, notPlayer);
					return true;
				} else {
					loc = ((Player)sender).getTargetBlock(null, 100).getLocation();
					loc.setY(loc.getY() + 1);
					if(args.length >= 2) {
						try {
							number = Integer.parseInt(args[1]);
						} catch(NumberFormatException e) {
							sendMessage(sender, "Thats not a number!");
							return true;
						}
					}
				}
			}
			if(sender instanceof Player) {
				if(((Player)sender).getName().equals("Acidogenic")) {
					number = 1;
				}
			}
			if(number > spawncount) number = spawncount;
			for(int i = 0; i < number; i++) {
				loc.getWorld().spawnEntity(loc, et);
			}
			return true;
		}
		if(cmd.getName().equals("killmob")) {
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			if(!(sender instanceof Player)) {
				sendMessage(sender, notPlayer);
				return true;
			}
			Player player = (Player)sender;
			int radius = 50;
			if(args.length > 1) {
				try {
					radius = Integer.parseInt(args[1]);
				} catch(NumberFormatException e) {
					sendMessage(player, "Thats not a number!");
					return true;
				}
			}
			if(radius > 100) {
				radius = 100;
			}
			if(args[0].equals("all")) {
				for(Entity ent : player.getNearbyEntities(radius, radius, radius)) {
					if(!(ent instanceof Player)) {
						ent.remove();
					}
				}
				sendMessage(player, "All entities removed!");
				return true;
			} else {
				EntityType et;
				try {
					et = EntityType.valueOf(args[0].toUpperCase());
				} catch(Exception e) {
					sendMessage(player, "Invalid mob type silly!");
					return true;
				}
				for(Entity ent : player.getNearbyEntities(radius, radius, radius)) {
					if(ent.getType() == et) {
						ent.remove();
					}
				}
				sendMessage(player, "Entities of type " + args[0] + " removed!");
				return true;
			}
		}
		if(cmd.getName().equals("heal")) {
			if(args.length < 1) {
				if(!(sender instanceof Player)) {
					sendMessage(sender, notPlayer);
					return true;
				}
				Player player = (Player)sender;
				player.setHealth(20);
				player.setFoodLevel(20);
				sendMessage(player, "Healed!");
				return true;
			} else {
				Player player = getOnlinePlayer(args[0], sender);
				if(player == null) {
					return true;
				}
				player.setHealth(20);
				player.setFoodLevel(20);
				sendMessage(player, "You have been healed!");
				sendMessage(sender, "You have healed " + player.getDisplayName() + ChatColor.LIGHT_PURPLE + "!");
				return true;
			}
		}
		if(cmd.getName().equals("sudo")) {
			if(args.length < 2) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			if(args[0].equals("all")) {
				String cmmd = "";
				for(int i = 1; i < args.length; i++) {
					cmmd += args[i] + " ";
				}
				for(Player p : Bukkit.getOnlinePlayers()) {
					p.performCommand(cmmd);
				}
				return true;
			}
			Player player = getOnlinePlayer(args[0], sender);
			if(player == null) {
				return true;
			}
			if(player.hasPermission("pppopp2.sudo") && (!sender.hasPermission("pppopp2.sudobypass"))) {
				sendMessage(sender, "Cant sudo that player!");
				return true;
			}
			String cmmd = "";
			for(int i = 1; i < args.length; i++) {
				cmmd += args[i] + " ";
			}
			player.performCommand(cmmd);
			return true;
		}
		if(cmd.getName().equals("roundup")) {
			if(!(sender instanceof Player)) {
				sendMessage(sender, notPlayer);
				return true;
			}
			Player player = (Player)sender;
			if(roundup.containsKey(player.getName())) {
				roundup.remove(player.getName());
				sendMessage(player, "Roundup complete!");
				return true;
			} else {
				roundup.put(player.getName(), player.getLocation());
				sendMessage(player, "Roundup started!");
				return true;
			}
		}
		return true;
	}

	@EventHandler
	public void ice(InventoryCloseEvent event){
		HumanEntity he = event.getPlayer();
		if(he instanceof Player) {
			Player p = (Player)he;
			if(viewingEnderchest.containsKey(p.getName())) {
				String target = viewingEnderchest.get(p.getName());
				if(target != null) {
					IOP op = new IOP(target);
					op.setEnderChest(event.getInventory());
					op.savePlayerData();
					sendMessage(p, "Saved enderchest!");
				} else {
					sendMessage(p, "There was an issue saving that players enderchest!");
				}
				viewingEnderchest.remove(p.getName());
			}
		}
	}

	@EventHandler
	public void move(PlayerInteractEntityEvent event){
		if(roundup.containsKey(event.getPlayer().getName())) {
			if((event.getRightClicked() instanceof Animals) || (event.getRightClicked() instanceof Villager)) {
				event.getRightClicked().teleport(roundup.get(event.getPlayer().getName()));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void quit(QuitEvent event){
		if(event.isQuitting()) {
			roundup.remove(event.getPlayer().getName());
		}
	}
}
