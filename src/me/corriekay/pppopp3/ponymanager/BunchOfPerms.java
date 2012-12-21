package me.corriekay.pppopp3.ponymanager;

import java.util.HashMap;

public class BunchOfPerms{

	private HashMap<String,Boolean> perms;

	public BunchOfPerms(HashMap<String,Boolean> perms){
		this.perms = perms;
	}

	public HashMap<String,Boolean> getPerms(){
		return perms;
	}
}
