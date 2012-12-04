package me.corriekay.pppopp3.modules;

import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class WorldEditHookModule extends PSCmdExe {

	public WorldEditHookModule(){
		super("WorldEditHookModule");
	}
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event){
		String[] msgWords = event.getMessage().split(" ");
		String cmd = msgWords[0];
		String[] args = new String[msgWords.length-1];
		for(int i = 1; i<msgWords.length;i++){
			args[i-1] = msgWords[i];
		}
		System.out.println("Command: "+cmd);
		for(String s : args){
			System.out.println(s);
		}
	}
}
