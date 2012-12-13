package me.corriekay.pppopp3.warp;

import org.bukkit.Location;

public class Warp {

	private final Location loc;
	private final String name;
	
	public Warp(String name, Location loc){
		this.name = name;
		this.loc = loc;
	}
	public String name(){
		return name;
	}
	public Location loc(){
		return loc;
	}
}
