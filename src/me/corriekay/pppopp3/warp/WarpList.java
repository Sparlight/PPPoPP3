package me.corriekay.pppopp3.warp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.Location;

public class WarpList{

	private HashMap<String,Warp> warps = new HashMap<String,Warp>();
	private ArrayList<String> warpNames = new ArrayList<String>();

	public WarpList(HashMap<String,Warp> warps){
		this.warps = warps;
		warpNames.addAll(warps.keySet());
		Collections.sort(warpNames);
	}

	protected ArrayList<String> warps(){
		return warpNames;
	}

	protected int size(){
		return warpNames.size();
	}

	protected Location getWarp(String warpName){
		Location loc;
		try {
			loc = warps.get(warpName).loc();
		} catch(NullPointerException e) {
			return null;
		}
		return loc;
	}
}
