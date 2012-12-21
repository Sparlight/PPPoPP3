package me.corriekay.pppopp3.rpa;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import me.corriekay.packets.PacketUtils;
import me.corriekay.packets.PoniFile;
import me.corriekay.packets.PoniFolder;
import me.corriekay.packets.PoniPacket;
import me.corriekay.packets.client.*;
import me.corriekay.packets.server.*;
import me.corriekay.pppopp3.Mane;
import me.corriekay.pppopp3.chat.Channel;
import me.corriekay.pppopp3.chat.ChatHandler;
import me.corriekay.pppopp3.events.JoinEvent;
import me.corriekay.pppopp3.events.QuitEvent;
import me.corriekay.pppopp3.ponymanager.PonyManager;
import me.corriekay.pppopp3.ponyville.Pony;
import me.corriekay.pppopp3.ponyville.Ponyville;
import me.corriekay.pppopp3.utils.PSCmdExe;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class RemotePonyAdmin extends PSCmdExe{
	private int port;
	private Server server;
	private HashMap<Integer,Key> connecting = new HashMap<Integer,Key>();
	private HashMap<Integer,RemotePony> connected = new HashMap<Integer,RemotePony>();
	private HashMap<Integer,Alert> alerts = new HashMap<Integer,Alert>();
	private int alertCount = 0;

	public static RemotePonyAdmin rpa;

	public RemotePonyAdmin() throws Exception{
		super("RemotePonyAdmin", "alert", "setpassword", "rpadebug");
		rpa = this;
		FileConfiguration config = getNamedConfig("remoteponyadmin.yml");
		if(config.getBoolean("debug", false)) {
			Log.DEBUG();
		} else {
			Log.NONE();
		}
		port = config.getInt("port", 25566);
		server = new Server();
		PacketUtils.registerServerPackets(server);
		server.start();
		server.bind(port);
		server.addListener(new RPAListener());
	}

	public class RPAListener extends Listener{
		public void connected(Connection c){
			try {
				for(Connection connection : server.getConnections()) {
					if(!connection.isConnected()) {
						continue;
					}
					if(!(c.equals(connection)) && (connection.getRemoteAddressTCP().getHostName().equals(c.getRemoteAddressTCP().getHostName()))) {
						System.out.print("Disconnecting RPA client: Logged in from another client on the same host");
						killClient(c, "Logged in from another client on the same host");
						return;
					}
				}
			} catch(Exception e) {}
			Integer i = c.getID();
			KeyPair kp;
			try {
				kp = PacketUtils.keyPairGen();
			} catch(Exception e) {
				e.printStackTrace();
				killClient(c, "Exception thrown serverside generating RSA keypair");
				return;
			}
			connecting.put(i, kp.getPrivate());
			byte[] pubMod, pubExp;
			RSAPublicKey pubkey = (RSAPublicKey)kp.getPublic();
			pubMod = pubkey.getModulus().toByteArray();
			pubExp = pubkey.getPublicExponent().toByteArray();
			ServerHoofshake shs = new ServerHoofshake();
			shs.ver = PacketUtils.version;
			shs.pubMod = pubMod;
			shs.pubExp = pubExp;
			try {
				c.sendTCP(shs);
			} catch(Exception e) {
				killClient(c, "An exception was caught trying to log in. The little bastard. Try again!");
			}
		}

		public void received(Connection c, Object p){
			if(p instanceof ClientHoofshake) {
				ClientHoofshake chs = (ClientHoofshake)p;
				byte[] pw = chs.pw;
				String pass;
				try {
					pass = new String(PacketUtils.rsaDecrypt(pw, connecting.get(c.getID())));
				} catch(Exception e) {
					System.out.print("Disconnecting RPA client: exception decrypting password");
					e.printStackTrace();
					killClient(c, "Serverside exception decrypting password");
					return;
				}
				connecting.remove(c.getID());
				Pony pony = Ponyville.getOfflinePony(chs.name);
				if(pony == null) {
					System.out.print("Disconnecting RPA client: pony is null");
					killClient(c, "User not found!");
					return;
				}
				boolean isOp;
				{
					String g = PonyManager.getGroup(chs.name);
					if(g == null) {
						System.out.println("Disconnecting RPA client: group is null for player \"" + pony.getName() + "\"");
						killClient(c, "Group not found!");
						return;
					}
					isOp = (g.equals("opony") || g.equals("admin"));
				}
				if(!isOp) {
					System.out.print("Disconnecting RPA client: is not OP");
					killClient(c, "User is not OP");
					return;
				}
				Integer filePass = pony.getRPAPassword();
				if(filePass == null) {
					System.out.print("Disconnecting RPA client: config password does not exist");
					killClient(c, "Please go into the minecraft server and set your password with /setpassword <password> before attempting to log in");
					return;
				}
				if(filePass.intValue() != pass.hashCode()) {
					System.out.print("Disconnecting RPA client: Incorrect password");
					killClient(c, "Password does not match!");
					return;
				}
				for(Connection connection : server.getConnections()) {
					if(!connection.isConnected()) {
						continue;
					}
					if(connection.equals(c)) {
						continue;
					}
					if(connection.isConnected()) {
						RemotePony rp = connected.get(connection.getID());
						if(rp == null) {
							continue;
						}
						if(rp.name.equals(chs.name)) {
							System.out.print("Disconnecting RPA client: logged in from another client");
							killClient(c, "Logged in from another client!");
						}
					}
				}
				RemotePony rp = new RemotePony();
				rp.name = chs.name;
				rp.nickname = pony.getNickname();
				connected.put(c.getID(), rp);
				//send data
				sendPlayerList(c);
				updateAlerts(c);
				sendFileList(c);
				if(chs.requestData) {
					ChanNamesPacket cnp = new ChanNamesPacket();
					cnp.names = ChatHandler.ch.getChannelNames();
					c.sendTCP(cnp);
				}
				return;
			} else if((p instanceof PoniPacket) && connecting.keySet().contains(c.getID())) {
				c.close();
			} else if(p instanceof PoniPacket) {
				if(p instanceof ClientChatPacket) {
					ClientChatPacket ccp = (ClientChatPacket)p;
					RemotePony rp = connected.get(c.getID());
					Channel chan = ChatHandler.ch.getChannel(ccp.channel);
					chan.broadcastToChannel(rp.nickname, rp.name, ccp.message, true);
					return;
				}
				if(p instanceof OPonyResponsePacket) {
					OPonyResponsePacket sprp = (OPonyResponsePacket)p;
					Player pl = Bukkit.getPlayerExact(sprp.player);
					Alert a = alerts.get(sprp.id);
					if(a.responder != null) {
						System.out.println("Tried to take one that already got taken! id: " + sprp.id);
						return;
					}
					a.responder = connected.get(c.getID()).name;
					if(pl != null) {
						sendMessage(pl, "Youve got the attention of an OPony! " + connected.get(c.getID()).nickname + " is on their way!");
					}
					for(Connection connection : server.getConnections()) {
						if(c.isConnected()) {
							updateAlerts(connection);
						}
					}
					return;
				}
				if(p instanceof PlayerInfoPacket) {
					if(!c.isConnected()) {
						return;
					}
					PlayerInfoResponsePacket pirp = new PlayerInfoResponsePacket();
					String playerString = ((PlayerInfoPacket)p).player;
					Pony pony = Ponyville.getOfflinePony(playerString);
					if(pony == null) {
						pirp.configExists = false;
						c.sendTCP(pirp);
						return;
					}
					pirp.playername = pony.getName();
					pirp.configExists = true;
					LinkedHashMap<String,String> m = pirp.playerInfo;
					m.put("Name", pony.getName());
					m.put("Nickname", pony.getNickname());
					m.put("First Seen", pony.getFirstLogon());
					m.put("Last Seen", pony.getLastLogout());
					m.put("Last Logged In", pony.getLastLogon());
					m.put("Online", pony.getPlayer().isOnline() + "");
					m.put("Muted", pony.isMuted() + "");
					m.put("Current Chat Channel", pony.getChatChannel());
					String channels = "";
					for(String channel : pony.getListeningChannels()) {
						channels += channel + ", ";
					}
					m.put("Channels Listening To", channels.substring(0, channels.length() - 2));
					m.put("Group", pony.getGroup());
					m.put("Banned", pony.isBanned() + "");
					if(pony.isBanned()) {
						m.put("Ban Type", pony.getBanType() + "");
					}
					int i = 0;
					ArrayList<String> notes = (ArrayList<String>)pony.getNotes();
					if(notes.size() > 0) {
						for(String s : notes) {
							m.put("Account Note " + i++, s);
						}
					}
					pirp.playerInfo = m;
					c.sendTCP(pirp);
					return;
				}
				if(p instanceof DirectoryRequestPacket) {
					sendFileList(c);
					return;
				}
				if(p instanceof FileRequestPacket) {
					FileRequestPacket frp = (FileRequestPacket)p;
					File file = new File(frp.fileDirectory, frp.fileName);
					if(!file.exists()) {
						killClient(c, "Your client requested a file that does not exist!");
						return;
					}
					try {
						FileInputStream stream = new FileInputStream(file);
						byte[] b = new byte[(int)file.length()];
						stream.read(b);
						stream.close();
						FilePacket fp = new FilePacket();
						fp.bytes = b;
						fp.directory = frp.clientDirectory;
						fp.name = frp.fileName;
						c.sendTCP(fp);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void sendFileList(final Connection c){
			PoniFolder pf = new PoniFolder();
			pf.directory = Mane.getInstance().getDataFolder().getAbsolutePath();
			fillFolder(pf);
			DirectoryResponsePacket drp = new DirectoryResponsePacket();
			drp.folder = pf;
			c.sendTCP(drp);
		}

		private void fillFolder(PoniFolder pfolder){
			File dir = new File(pfolder.directory);
			if(!dir.isDirectory()) {
				return;
			}
			ArrayList<PoniFile> files = new ArrayList<PoniFile>();
			ArrayList<PoniFolder> folders = new ArrayList<PoniFolder>();
			for(File f : dir.listFiles()) {
				if(f.isFile()) {
					PoniFile pFile = new PoniFile();
					pFile.name = f.getName();
					files.add(pFile);
				}
				if(f.isDirectory()) {
					PoniFolder folder = new PoniFolder();
					folder.directory = pfolder.directory + File.separator + f.getName();
					folder.name = f.getName();
					fillFolder(folder);
					folders.add(folder);
				}
			}
			PoniFile[] fileArray = new PoniFile[files.size()];
			PoniFolder[] folderArray = new PoniFolder[folders.size()];
			for(int i = 0; i < files.size(); i++) {
				fileArray[i] = files.get(i);
			}
			for(int i = 0; i < folders.size(); i++) {
				folderArray[i] = folders.get(i);
			}
			pfolder.files = fileArray;
			pfolder.subfolders = folderArray;
		}

		public void disconnected(Connection c){
			connecting.remove(c.getID());
			connected.remove(c.getID());
		}
	}

	private void updateAlerts(final Connection c){
		SendAlerts sa = new SendAlerts();
		for(Integer i : alerts.keySet()) {
			Alert a = alerts.get(i);
			sa.ids.add(a.id);
			sa.responders.put(a.id, a.responder);
			sa.timestamps.put(a.id, a.timestamp);
			sa.whosents.put(a.id, a.sender);
		}
		c.sendTCP(sa);
	}

	private void sendPlayerList(final Connection c){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
			@Override
			public void run(){
				final PonyList pl = new PonyList();
				HashMap<String,String> ponies = new HashMap<String,String>();
				for(Player p : Bukkit.getOnlinePlayers()) {
					ponies.put(p.getName(), p.getDisplayName());
				}
				pl.ponies = ponies;
				c.sendTCP(pl);
			}
		}, 1);
	}

	@EventHandler
	public void onJoin(JoinEvent event){
		if(event.isJoining()) {
			for(Connection c : server.getConnections()) {
				if(c.isConnected()) {
					sendPlayerList(c);
				}
			}
		}
	}

	@EventHandler
	public void onQuit(QuitEvent event){
		if(event.isQuitting()) {
			for(Connection c : server.getConnections()) {
				if(c.isConnected()) {
					sendPlayerList(c);
				}
			}
		}
	}

	public void sendChatPacket(final ChatPacket cp){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
			@Override
			public void run(){
				for(Connection c : server.getConnections()) {
					if(c.isConnected()) {
						c.sendTCP(cp);
					}
				}
			}
		});
	}

	@Override
	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equals("alert")) {
			if(sender instanceof Player) {
				int i = callInTheSeaPonies(sender.getName(), sender.getName());
				sendMessage(sender, "Alert sent out! " + i + " OPonies have been alerted!");
			}
		}
		if(cmd.getName().equals("setpassword")) {
			if(sender instanceof Player) {
				if(args.length < 1) {
					sendMessage(sender, notEnoughArgs);
					return true;
				}
				Pony pony = Ponyville.getOfflinePony((Player)sender);
				int pass = args[0].hashCode();
				pony.setRPAPassword(pass);
				pony.save();
				System.out.println("saving password");
				sendMessage(sender, "Password saved! it should now be possible to log into RPA using that password!");
				return true;
			}
		}
		if(cmd.getName().equals("rpadebug")) {
			if(args.length < 1) {
				sendMessage(sender, notEnoughArgs);
				return true;
			}
			try {
				boolean debug = Boolean.parseBoolean(args[0]);
				if(debug) {
					Log.DEBUG();
					sendMessage(sender, "RPA server debug on!");
				} else {
					Log.NONE();
					sendMessage(sender, "RPA server debug off!");
				}
				return true;
			} catch(Exception e) {
				sendMessage(sender, "Thats not a boolean!");
				return true;
			}
		}
		return true;
	}

	private int callInTheSeaPonies(String name, String alerter){
		Alert a = new Alert(alertCount++, alerter);
		alerts.put(a.id, a);
		OPonyAlertPacket opap = new OPonyAlertPacket();
		opap.player = name;
		opap.id = a.id;
		int i = 0;
		for(Connection c : server.getConnections()) {
			if(c.isConnected()) {
				updateAlerts(c);
				c.sendTCP(opap);
				i++;
			}
		}
		return i;
	}

	private void killClient(final Connection c, String msg){
		AssassinateClient ac = new AssassinateClient();
		ac.confirm = true;
		ac.note = msg;
		c.sendTCP(ac);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Mane.getInstance(), new Runnable() {
			@Override
			public void run(){
				if(c.isConnected()) {
					c.close();
				}
			}
		}, 10 * 20);
	}

	public class Alert{
		public String sender;
		public String responder = null;
		public long timestamp;
		public int id;

		public Alert(int id, String sender){
			this.id = id;
			this.sender = sender;
			timestamp = System.currentTimeMillis();
		}
	}

	public void message(String message){
		BroadcastMessage bp = new BroadcastMessage();
		bp.message = message;
		for(Connection c : server.getConnections()) {
			if(c.isConnected()) {
				c.sendTCP(bp);
			}
		}
	}

	public void messageCorrie(String message){
		for(Connection c : server.getConnections()) {
			if(c.isConnected()) {
				RemotePony rp = connected.get(c.getID());
				if(rp != null) {
					if(rp.name.equals("TheQueenOfPink")) {
						BroadcastMessage bm = new BroadcastMessage();
						bm.message = message;
						c.sendTCP(bm);
					}
				}
			}
		}
	}

	public void deactivate(){
		server.stop();
		System.out.println("Stopping RPA server");
	}

	public void reload(){
		deactivate();
	}
}
