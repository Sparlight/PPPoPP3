package me.corriekay.pppopp3.ponymanager;

import java.util.HashMap;
import java.util.HashSet;

import me.corriekay.pppopp3.modules.Equestria;

import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

public class PonyGroup {

	protected final HashMap<String,BunchOfPerms> worldPerms = new HashMap<String,BunchOfPerms>();
	public final String perm2add2;
	private String name;
	
	public PonyGroup(FileConfiguration config, String name){
		this.name = name;
		perm2add2 = config.getString("groups."+name+".perm2add2");
		HashMap<String,Boolean> globalPerms = new HashMap<String,Boolean>();
		{//get global perms
			HashSet<String> negPerms = new HashSet<String>();
			for(String gperm : config.getStringList("groups."+name+".globalPermissions")){
				if(gperm.startsWith("-")){
					negPerms.add(gperm.substring(1,gperm.length()));
				} else {
					globalPerms.put(gperm,true);
				}
			}
			for(String negPerm : negPerms){
				globalPerms.put(negPerm,false);
			}
		}
		{//get world perms, add to worldPerms
			for(String worldString : config.getConfigurationSection("groups."+name+".worldPermissions").getKeys(false)){
				BunchOfPerms worldBunchPerms;
				HashMap<String,Boolean> finalWorldPerms = new HashMap<String,Boolean>();
				for(String perm : globalPerms.keySet()){
					finalWorldPerms.put(perm, globalPerms.get(perm));
				}
				HashSet<String> negPerms = new HashSet<String>();
				for(String worldPermission : config.getStringList("groups."+name+".worldPermissions."+worldString)){
					if(worldPermission.startsWith("-")){
						negPerms.add(worldPermission.substring(1,worldPermission.length()));
					} else {
						finalWorldPerms.put(worldPermission, true);
					}
				}
				for(String negPerm : negPerms){
					finalWorldPerms.put(negPerm, false);
				}
				worldBunchPerms = new BunchOfPerms(finalWorldPerms);
				worldPerms.put(worldString, worldBunchPerms);
			}
		}
	}
	protected HashMap<String,Boolean> getPermissions(World w){
		return worldPerms.get(Equestria.get().getParentWorld(w).getName()).getPerms();
	}
	protected boolean canMoveTo(Player asker){
		return asker.hasPermission(perm2add2);
	}
	protected boolean canMoveTo(ConsoleCommandSender asker){
		return asker.hasPermission(perm2add2);
	}
	protected boolean canMoveTo(Permissible asker){
		return asker.hasPermission(perm2add2);
	}
	protected String getName(){
		return name;
	}

}
