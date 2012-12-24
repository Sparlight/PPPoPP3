package me.corriekay.pppopp3.modules;

import java.util.HashMap;

import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.utils.PSCmdExe;
import me.corriekay.pppopp3.utils.PonyLogger;
import me.corriekay.pppopp3.utils.PonyString;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SpamHandler extends PSCmdExe{

	private final int grace;
	private final boolean cancel;
	private final HashMap<String,Long> lastMsg = new HashMap<String,Long>();
	private final HashMap<String,PonyString[]> msgQueue = new HashMap<String,PonyString[]>();

	public SpamHandler(){
		super("SpamHandler");
		grace = Mane.getInstance().getConfig().getInt("", 150);
		cancel = Mane.getInstance().getConfig().getBoolean("cancelSpam", false);
		for(Player player : Bukkit.getOnlinePlayers()) {
			lastMsg.put(player.getName(), (long)0);
			PonyString[] psArray = new PonyString[3];
			for(int i = 0; i < psArray.length; i++) {
				psArray[i] = new PonyString("");
			}
			msgQueue.put(player.getName(), psArray);
		}
	}

	private PonyString[] shiftMessages(PonyString[] psArray, String msg){
		psArray[2] = psArray[1];
		psArray[1] = psArray[0];
		psArray[0] = new PonyString(msg);
		return psArray;
	}

	private int checkOffenses(PonyString[] psArray){
		int offenses = 0;
		for(PonyString s1 : psArray) {
			for(PonyString s2 : psArray) {
				if(s1.hashCode() != s2.hashCode() && s1.toString().equals(s2.toString())) {
					if(!s1.toString().equals("") && !s2.toString().equals("")) {
						offenses++;
					}
				}
			}
		}
		return offenses;
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event){
		if(event.isCancelled()) {
			return;
		}
		final Player player = event.getPlayer();
		long now = System.currentTimeMillis();
		long last = lastMsg.get(player.getName());
		if(now - grace < last) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
				@Override
				public void run(){
					player.kickPlayer("Please do not spam. (messaged too fast. Lag?)");
				}
			});
			PonyLogger.logAdmin("Console", "Kicked " + event.getPlayer().getName() + " for spamming. Type: Messaged too fast");
			if(cancel) {
				event.setCancelled(true);
			}
			return;
		}
		msgQueue.put(player.getName(), shiftMessages(msgQueue.get(player.getName()), event.getMessage()));
		PonyString[] psArray = msgQueue.get(player.getName());
		if(checkOffenses(psArray) >= 6) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
				@Override
				public void run(){
					player.kickPlayer("Please do not spam. (Repeat messages)");
				}
			});
			PonyLogger.logAdmin("Console", "Kicked " + event.getPlayer().getName() + " for spamming. Type: Repeat messages");
			if(cancel) {
				event.setCancelled(true);
			}
			return;
		}
	}

	@EventHandler
	public void onJoin(JoinEvent event){
		if(event.isJoining()) {
			lastMsg.put(event.getPlayer().getName(), (long)0);
			PonyString[] psArray = new PonyString[3];
			for(int i = 0; i < psArray.length; i++) {
				psArray[i] = new PonyString("");
			}
			msgQueue.put(event.getPlayer().getName(), psArray);
		}
	}

	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()) {
			lastMsg.remove(event.getPlayer().getName());
			msgQueue.remove(event.getPlayer().getName());
		}
	}
}
