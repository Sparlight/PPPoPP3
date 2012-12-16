package me.corriekay.pppopp3.modules;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class XrayLogger extends PSCmdExe {

	private File directory;
	private FileConfiguration log;
	private int day = Integer.parseInt(getSystemDay());

	public XrayLogger() throws IOException{
		super("XrayLogger");
		directory = new File(Mane.getInstance().getDataFolder()+File.separator+"Xray Logger", getSystemDate());
		if(!directory.isDirectory()){
			directory.mkdirs();
		}
		newDay(false);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Mane.getInstance(), new Runnable(){
			@Override
			public void run(){
				int eDay = Integer.parseInt(getSystemDay());
				if(eDay!=day){
					try {
						newDay(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		},0,20);
		
	}	private void newDay(boolean newDay) throws IOException{
		if(newDay){
			File parentlog = new File(directory,"dailyLog.txt");
			if(!parentlog.exists()){
				parentlog.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(parentlog));
			out.write("Daily diamond log for "+getSystemDate());
			out.newLine();
			out.newLine();
			out.write("=========================");
			out.newLine();
			out.newLine();
			for(String player : log.getKeys(false)){
				List<String> dbreaks = log.getStringList(player);
				out.write("Break log for player: "+player+". Broke "+dbreaks.size()+" Ore(s) today.");
				out.newLine();
				out.newLine();
				for(String entry : dbreaks){
					out.write(entry);
					out.newLine();
				}
				out.write("-----");
				out.newLine();
				out.newLine();
			}
			out.write("end report");
			out.close();
		}
		day = Integer.parseInt(getSystemDay());
		directory = new File(Mane.getInstance().getDataFolder()+File.separator+"Xray Logger", getSystemDate());
		if(!directory.isDirectory()){
			directory.mkdirs();
		}
		File logfile = new File(directory, "playerLogs.yml");
		if(!logfile.exists()){
			logfile.createNewFile();
		}
		log = YamlConfiguration.loadConfiguration(logfile);
	}
	@EventHandler
	public void bBreak(BlockBreakEvent event){
		if(event.getBlock().getType() == Material.DIAMOND_ORE||event.getBlock().getType() == Material.EMERALD_ORE){
			Block block = event.getBlock();
			Player player = event.getPlayer();
			int eDay = Integer.parseInt(getSystemDay());
			if(eDay!=day){
				try {
					newDay(true);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			String mLog = getSystemTime()+" Broke "+block.getType().name()+" x: "+block.getX()+" y: "+block.getY()+" z: "+block.getZ();
			List<String> list = log.getStringList(player.getName());
			list.add(mLog);
			log.set(player.getName(), list);
			try {
				log.save(new File(directory,"playerLogs.yml"));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	public static String getSystemDay(){
		DateFormat dateFormat = new SimpleDateFormat("d");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	public static String getSystemDate(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
	public static String getSystemTime(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		return dateFormat.format(cal.getTime());
	}
}
