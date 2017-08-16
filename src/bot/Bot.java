package bot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {
	static Attachment image;
	static ArrayList<Insult> insults = new ArrayList<Insult>();
	public static void main(String[] args) {
		try {
			JDA	 jda = new JDABuilder(AccountType.BOT).setToken("MzQxNjY2NDQxMzk4ODQ1NDUw.DHSV6g.NK5f2yowHpxcAW2jw6hEL8nRCe4").buildBlocking();
			jda.addEventListener(new Bot());
			AudioSender as = new AudioSender();
//			jda.addEventListener(as);
			try {
				as.init();
			} catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace();
			}
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("insults.txt"));
			String line = br.readLine();
			while(line != null) {
				if(line.equals("#")) {
					String name = br.readLine();
					String insult = br.readLine();
					String indexStr = br.readLine();
					System.out.println(name + "; " + insult + "; " + indexStr);
					int userIndex = Integer.parseInt(indexStr);
					insults.add(new Insult(name, insult, userIndex));
				}
				line = br.readLine();
			}
			br.close();
			System.out.println(insults.get(0).name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void onMessageReceived(MessageReceivedEvent event) {
		JDA jda = event.getJDA();
		String msg = event.getMessage().getRawContent();
		MessageChannel channel = event.getChannel();
		
		if(!event.getMessage().getAttachments().isEmpty()) {
			if(event.getMessage().getAttachments().get(0).isImage()) image = event.getMessage().getAttachments().get(0);
		}
		
		String start;
		int index = 0;
		boolean done = false;
		String query;
		while(index < msg.length() && msg.charAt(index) != ' ') {
				index++;
		}
		start = msg.substring(0, index);
		System.out.println(start);
		switch(start) {
		case "!suggestsuicide" :
			query = msg.replace("!suggestsuicide", "");
			System.out.println(query);
			if(query.equals("")) {
				channel.sendMessage("Kill yourself you worthless piece of trash").queue();
			} else {
				channel.sendMessage(query + ", please kill yourself you worthless piece of trash. You are literally the most useless person (if you could even call yourself that) on the entire planet. Your carbon would be better off as fuel to heat a home in some poor African village, so please, end your own life for humanity's sake.").queue();
			}
			event.getMessage().delete().queue();
		break;
		
		case "!starving" :
			query = msg.replace("!starving", "");
			System.out.println(query);
			if(query.equals("")) {
				channel.sendMessage("Lol, you look like a shriveled skeleton. Are you Ethiopian or something?").queue();
			} else {
				channel.sendMessage("Lol, " + query + " you look like a shriveled skeleton. Are you Ethiopian or something? Go eat a deep fried snickers.").queue();
			}
			event.getMessage().delete().queue();
		break;
		
		case "!addinsult" :
			query = msg.replace("!addinsult ", "");
			if(!query.contains("{{user}}")) {
				channel.sendMessage("***Sorry, but you must specify where the target's username will be placed by using {{user}}***").queue();
				event.getMessage().addReaction("❌").queue();
				break;
			}
			System.out.println(query);
			String insultName;
			index = 0;
			while(index < query.length() && query.charAt(index) != ' ') index++;
			insultName = query.substring(0, index);
			String insult = query.replace(insultName, "").trim();
			insults.add(new Insult(insultName, insult.replace(" {{user}}", ""), insult.lastIndexOf("{{user}}")));
			System.out.println(insultName);
			System.out.println(insult);
			writeInsultArray();
			event.getMessage().addReaction("✅").queue();
		break;
		
		case "!removeinsult" : 
			query = msg.replace("!removeinsult ", "");
			System.out.println(query);
			boolean deleted = false;
			for(Insult i : insults) {
				if(i.name.equals(query)) {
					insults.remove(i);
					writeInsultArray();
					deleted = true;
					event.getMessage().addReaction("✅").queue();
				}
			}
			if(!deleted) {
				channel.sendMessage("***Sorry, but the insult you have requested was not found.***").queue();
				event.getMessage().addReaction("❌").queue();
			}
		break;
			
		case "!insult" :
			query = msg.replace("!insult ", "");
			index = 0;
			while(index < query.length() && query.charAt(index) != ' ') index++;
			String name = query.substring(0, index);
			String user = query.replace(name, "");
			Insult requestedInsult = null;
			System.out.println(name);
			for(Insult i : insults) {
				if(i.name.equals(name)) {
					requestedInsult = i;
				}
			}
			if(requestedInsult == null) {
				channel.sendMessage("***Sorry, but the insult you have requested was not found.***").queue();
				event.getMessage().addReaction("❌").queue();
			} else {
				channel.sendMessage(putChar(requestedInsult.insult, requestedInsult.userIndex, user)).queue();
				event.getMessage().addReaction("✅").queue();
			}
		break;
		
		case "!play" :
			query = msg.replace("!play ", "");
			if(!event.getAuthor().getJDA().getVoiceChannels().isEmpty()) {
				VoiceChannel vc = event.getAuthor().getJDA().getVoiceChannels().get(0);
			}
			
		break;
		
		case "!dotimage" :
			new File("dot.png").delete();
			try {
				URL url = new URL(image.getUrl());
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
				connection.connect();
				InputStream is = connection.getInputStream();
				OutputStream os = new FileOutputStream(image.getFileName());
			    byte[] b = new byte[2048];
			    int length;

			    while ((length = is.read(b)) != -1) {
			        os.write(b, 0, length);
			    }
			    is.close();
			    os.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Runtime.getRuntime().exec("java -jar dotimage.jar " + image.getFileName());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while(!new File("dot.png").exists()) {
				
			}
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			channel.sendFile(new File("dot.png"), null).queue();
			new File(image.getFileName()).delete();
		break;
		}

	}
	
	public static String putChar(String input, int index, String a) {
		String firstHalf = input.substring(0, index);
		String secondHalf = input.substring(index, input.length());
		
		return firstHalf + a + secondHalf;
	}
	
	public static void writeInsultArray() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("insults.txt")));
			for(Insult i : insults) {
				writer.write("#");
				writer.newLine();
				writer.write(i.name);
				writer.newLine();
				writer.write(i.insult);
				writer.newLine();
				writer.write(Integer.toString(i.userIndex));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
