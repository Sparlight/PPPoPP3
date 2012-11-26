package me.corriekay.pppopp3.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import me.corriekay.pppopp3.Mane;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PonyLogger {
	public static void logCmdException(Exception e, String errorMessage, String handlerName){
		Mane plugin = Mane.getInstance();
		File directory = new File(plugin.getDataFolder()+File.separator+"Error Logging"+File.separator+handlerName);
		if(!directory.exists()){
			directory.mkdirs();
		}
		File file = new File(plugin.getDataFolder()+File.separator+"Error Logging"+File.separator+handlerName+File.separator+e.getClass().getName().substring(e.getClass().getName().lastIndexOf(".")+1)+".txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.write("Exception thrown on "+errorMessage);
			out.newLine();
			out.write("StackTrace:");
			out.newLine();
			for(StackTraceElement ste : e.getStackTrace()){
				out.write(ste.toString());
				out.newLine();
			}
			out.newLine();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	public static void logListenerException(Exception e, String errorMessage, String handlerName, String eventName){
		Mane plugin = Mane.getInstance();
		File directory = new File(plugin.getDataFolder()+File.separator+"Error Logging"+File.separator+handlerName+File.separator+eventName);
		if(!directory.isDirectory()){
			directory.mkdirs();
		}
		File file = new File(directory,e.getClass().getName().substring(e.getClass().getName().lastIndexOf(".")+1)+".txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file,true));
			out.write("Exception thrown on "+errorMessage+"\r\n");
			out.write("StackTrace: \r\n");
			for(StackTraceElement ste : e.getStackTrace()){
				out.write(ste.toString()+"\r");
			}
			out.newLine();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}
	public static void logMessage(String path, String fileName, String messageToLog){
		Mane plugin = Mane.getInstance();
		File directory = new File (plugin.getDataFolder()+File.separator+path);
		if(!directory.exists()){
			directory.mkdirs();
		}
		File file = new File(plugin.getDataFolder()+File.separator+path+File.separator+fileName+".txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file,true));
			out.write(messageToLog+"\n");
			out.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	public static void logCommand(String path, String fileName, String cmdToLog){
		File directory = new File(path);
		if(!directory.exists()){
			directory.mkdirs();
		}
		File file = new File(directory+File.separator+fileName+".txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file,true));
			out.write(cmdToLog);
			out.newLine();
			out.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	public static void logAdmin(CommandSender sender, String msg2Log){
		String name = "Console";
		if(sender instanceof Player){
			name = ((Player)sender).getName();
		}
		logAdmin(name,msg2Log);
	}
	public static void logAdmin(String adminName, String msg2Log){
		String path;
		String fileName;
		String messageToLog;
		path = "Admin Logs";
		fileName = Utils.getFileDate(System.currentTimeMillis());
		messageToLog = "["+Utils.getTimeStamp(System.currentTimeMillis())+"] ["+adminName+"]: "+msg2Log;
		logMessage(path, fileName, messageToLog);
	}
}
