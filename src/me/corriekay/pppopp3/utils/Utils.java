package me.corriekay.pppopp3.utils;

/**
 * @Class: Utils
 * @Author: CorrieKay
 * @Purpose: Utils class. Non-instantiated, abstract. Contains utility methods, (functions hurr hurr)
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public abstract class Utils{

	public static String getDate(long time){
		Date date = new Date(time);
		return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
	}

	public static String getSystemTime(long time){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date cal = Calendar.getInstance().getTime();
		cal.setTime(time);
		return dateFormat.format(cal.getTime());
	}

	public static String getFileDate(long time){
		Date date = new Date(time);
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	public static String getTimeStamp(long time){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date cal = Calendar.getInstance().getTime();
		cal.setTime(time);
		return dateFormat.format(cal.getTime());
	}

	public static HashSet<Block> getBlocks(int length, int height, int width, Location loc){
		HashSet<Block> blocks = new HashSet<Block>();
		double startX = loc.getX() - length / 2;
		double startY = loc.getY() - height / 2;
		double startZ = loc.getZ() - width / 2;
		double y = startY;
		double z = startZ;
		double x = startX;
		double maxX = loc.getX() + length / 2;
		double maxY = loc.getY() + height / 2;
		double maxZ = loc.getZ() + width / 2;
		boolean Continue = true;
		while(Continue) {
			if(x < maxX) {
				x++;
			} else {
				x = startX;
				if(y < maxY) {
					y++;
				} else {
					y = startY;
					if(z < maxZ) {
						z++;
					} else {
						y = startY;
						Continue = false;
					}
				}
			}
			Location loc2 = new Location(loc.getWorld(), x, y, z);
			blocks.add(loc2.getBlock());
		}
		return blocks;
	}

	public static Location getLoc(ArrayList<String> array){
		if(array == null) {
			return null;
		}
		try {
			World w;
			double x, y, z;
			float pitch, yaw;
			w = Bukkit.getWorld(array.get(0));
			x = Double.parseDouble(array.get(1));
			y = Double.parseDouble(array.get(2));
			z = Double.parseDouble(array.get(3));
			pitch = Float.parseFloat(array.get(4));
			yaw = Float.parseFloat(array.get(5));
			Location l = new Location(w, x, y, z);
			l.setPitch(pitch);
			l.setYaw(yaw);
			return l;
		} catch(Exception e) {
			return null;
		}
	}
}
