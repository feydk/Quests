package io.github.feydk.quests;

import io.github.feydk.quests.QuestsRewards.RewardEntry;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestUI
{
	@SuppressWarnings("deprecation")
	static void displayQuestInfo(Player player, Quest quest)
	{
		BukkitPlayer p = Quests.getInstance().getBukkitPlayer(player);
		p.index = p.getIndex();
		quest.reward = Quests.getInstance().getRewards().getReward(p.index);
		RewardEntry reward = Quests.getInstance().getRewards().getTriggerReward(p.index);
		
		Date today = new Date();
		Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
		tomorrow.setHours(0);
		tomorrow.setMinutes(0);
		tomorrow.setSeconds(0);
		
		long diff = tomorrow.getTime() - today.getTime();
		int secs = (int)(diff / 1000);
		int days = secs / 86400;
		secs -= days * 86400;
		int hours = secs / 3600;
		secs -= hours * 3600;
		int mins = secs / 60;
		
		String next = "";
		
		if(days > 0)
			next += days + " " + (days > 1 ? "days" : "day") + ", ";
		
		if(hours > 0)
			next += hours + " " + (hours > 1 ? "hours" : "hour") + " and ";
		
		next += mins + " " + (mins > 1 || mins == 0 ? "minutes" : "minute");
		
		String json = "[";
		
		json += "{\"color\": \"aqua\", \"text\": \"\n === \"}, {\"color\": \"yellow\", \"text\": \"✦\"}, {\"color\": \"aqua\", \"text\": \" " + quest.getTypeName() + " Quest \"}, {\"color\": \"yellow\", \"text\": \"✦\"}, {\"color\": \"aqua\", \"text\": \" ===\n\"}, ";
		
		json += "{\"color\": \"white\", \"text\": \" " + quest.getDescription() + "\n\"}, ";
		
		if(quest.getProgress() > 0)
			json += "{\"color\": \"dark_aqua\", \"text\": \" Progress: \"}, {\"color\": \"aqua\", \"text\": \"" + formatDouble(quest.getProgress()) + "/" + formatDouble(quest.getGoal()) + "\n\"" + "}, ";
						
		json += "{\"color\": \"dark_aqua\", \"text\": \" Reward: \"}, {\"color\": \"aqua\", \"text\": \"" + Quests.getInstance().getPlugin().economy.format(quest.reward) + (reward != null ? " + " + reward.Text : "") + "\n\"" + "}, ";
		
		json += "{\"color\": \"gray\", \"text\": \" Your reward will reset in " + next + "\n\"}, ";
		
		
		//json += "{\"text\": \" " + progressBar(quest.getProgress(), quest.getGoal()) + "\n\", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + quest.getProgress() + " out of " + quest.getGoal() + "\"}}, ";
		
		json += "{\"text\": \" " + format("&f--") + "\n\"}, ";
		
		//json += "{\"color\": \"aqua\", \"text\": \" === \"}, {\"color\": \"aqua\", \"text\": \"Actions\"}, {\"color\": \"aqua\", \"text\": \" ===\n\"}, ";
		
		json += "{\"text\": \" " + format("&7[&3Change Quest&7]") + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q choices\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Select another quest.\"}}, ";
		
		json += "{\"text\": \" " + format("&7[&3Help&7]") + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q help\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Show the help screen for Quests.\"}}, ";
		
		json += "{\"text\": \" " + format("&7[&3Stats&7]") + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q stats\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Show your stats.\"}}, ";
		
		json += "{\"text\": \" " + format("&7[&3Settings&7]") + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q settings\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Tweak your settings.\"}}, ";
		
		json += "{\"text\": \" " + format("&7[&3Highscore&7]") + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q hi\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Check the highscore.\"}}, ";
				
		json += "{\"color\": \"white\", \"text\": \"\n\"} ";
		json += "]";
		
		sendJsonMessage(player, json);
	}
	
	static void displayStats(BukkitPlayer player)
	{
		int completed = HistoryModel.getCount(player.uuid, QuestStatus.COMPLETED.ordinal());
		int skipped = HistoryModel.getCount(player.uuid, QuestStatus.SKIPPED.ordinal());
		double rewards = HistoryModel.getRewards(player.uuid);
		Map<String, Integer> dist = HistoryModel.getDistribution(player.uuid);
		
		String json = "[";
		
		json += "{\"color\": \"aqua\", \"text\": \"\n === Quest Statistics for " + player.getPlayer().getName() + " ===\n\"}, ";
		
		json += "{\"text\": \" " + format("&3Completed quests: &b" + completed) + "\n\"}, ";
		json += "{\"text\": \" " + format("&3Skipped quests: &b" + skipped) + "\n\"}, ";
		json += "{\"text\": \" " + format("&3Total rewards: &b" + Quests.getInstance().getPlugin().economy.format(rewards)) + "\n\"}, ";
		json += "{\"text\": \" " + format("&3Quest types:") + "\n\"}, ";
		
		for(Entry<String, Integer> entry : dist.entrySet())
		{
			json += "{\"color\": \"aqua\", \"text\": \"" + entry.getKey() + "\"}, {\"color\": \"dark_aqua\", \"text\": \"(\"}, {\"color\": \"white\", \"text\": \"" + entry.getValue() + "\"}, {\"color\": \"dark_aqua\", \"text\": \") \"}, ";
		}
				
		json += "{\"text\": \"\n\"} ";
		json += "]";
		
		sendJsonMessage(player.getPlayer(), json);
	}
	
	static void displayHelp(BukkitPlayer player)
	{
		String json = "[";
		json += "{\"text\": \"\n " + format("&b§m   §r &bQuests Help §m   §r") + "\n\"}, ";
		json += "{\"color\": \"gray\", \"text\": \" Quests are small tasks that you can do to earn money. Quests are divided into various categories and every quest is randomly generated for you.\n \n\"}, ";
		json += "{\"color\": \"gray\", \"text\": \" Every day you can complete as many quests as you want, but the reward will decrease as you progress. The rewards are reset at midnight server time.\n \n\"}, ";
		json += "{\"color\": \"gray\", \"text\": \" When you do quests, make sure to read the quest description. It will say exactly what you need to do to complete the quest. Good luck and have fun!\"} ";
		json += "] ";
		
		sendJsonMessage(player.getPlayer(), json);
	}
	
	static void displaySettings(Player player, PlayerSettings settings)
	{
		String json = "[";
		
		json += "{\"text\": \"\n " + format("&b§m   §r &bQuests Settings §m   §r") + "\n\"}, ";
		
		json += "{\"color\": \"white\", \"text\": \" Show progressbar \", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + format("&2Show a progressbar when doing quests?") + "\"}}, ";
		json += "{\"color\": \"white\", \"text\": \"" + (settings.showProgressbar ? format("&f[&aOn&f]") : format("&8On")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set progressbar on\" }}, ";
		json += "{\"color\": \"white\", \"text\": \" " + (!settings.showProgressbar ? format("&f[&4Off&f]") : format("&8Off")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set progressbar off\" }}, ";
		json += "{\"color\": \"white\", \"text\": \"\n\"}, ";
		json += "{\"color\": \"white\", \"text\": \" Use sounds \", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + format("&2Get audio feedback when doing quests?") + "\"}}, ";
		json += "{\"color\": \"white\", \"text\": \"" + (settings.useSounds ? format("&f[&aOn&f]") : format("&8On")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set sound on\" }}, ";
		json += "{\"color\": \"white\", \"text\": \" " + (!settings.useSounds ? format("&f[&4Off&f]") : format("&8Off")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set sound off\" }}, ";
		json += "{\"color\": \"white\", \"text\": \"\n\"}, ";
		json += "{\"color\": \"white\", \"text\": \" Show quest completions from others \", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + format("&2Get a notification when\n&2other players complete quests?") + "\"}}, ";
		json += "{\"color\": \"white\", \"text\": \"" + (settings.showOthersCompletions ? format("&f[&aOn&f]") : format("&8On")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set announcement on\" }}, ";
		json += "{\"color\": \"white\", \"text\": \" " + (!settings.showOthersCompletions ? format("&f[&4Off&f]") : format("&8Off")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set announcement off\" }}, ";
		json += "{\"color\": \"white\", \"text\": \"\n\"}, ";
		json += "{\"color\": \"white\", \"text\": \" Remind me of quests when I log on \", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + format("&2Get a notification about your quest\n&2when you log on?") + "\"}}, ";
		json += "{\"color\": \"white\", \"text\": \"" + (settings.useReminders ? format("&f[&aOn&f]") : format("&8On")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set reminder on\" }}, ";
		json += "{\"color\": \"white\", \"text\": \" " + (!settings.useReminders ? format("&f[&4Off&f]") : format("&8Off")) + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q set reminder off\" }}, ";
		
		json += "{\"color\": \"white\", \"text\": \"\n\"} ";
		json += "]";
		
		sendJsonMessage(player, json);
	}
	
	@SuppressWarnings("deprecation")
	static void displayQuestSelection(Player player, List<Quest> quests)
	{
		BukkitPlayer p = Quests.getInstance().getBukkitPlayer(player);
		//double reward = Quests.getInstance().getRewards().getReward(p.index + 1);
		
		String json = "[";
		
		json += "{\"text\": \"\n " + format("&fChoose a quest: ") + "\"}, ";
		
		int i = 1;
		for(Quest quest : quests)
		{
			if(quest.getQuestType().equals(p.activeQuest.getQuestType()))
			{
				json += "{\"text\": \"" + format("&7[" + quest.getTypeName() + "]") + " \", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "This is your current quest." + "\"}}, ";
			}
			else
			{
				json += "{\"text\": \"" + format("&9[" + quest.getTypeName() + "]") + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q select " + i + "\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + stripNotes(quest.getDescription()) + "\"}}, ";
			}
			
			i++;
		}

		Date d1 = new Date(p.timestampCreated);
		d1.setHours(0);
		d1.setMinutes(0);
		d1.setSeconds(0);
		
		Date d2 = new Date();
		d2.setHours(0);
		d2.setMinutes(0);
		d2.setSeconds(0);
		
		/*System.out.println(d1);
		System.out.println(d2);
		System.out.println(d1.compareTo(d2));
		System.out.println(d2.compareTo(d1));
		System.out.println(d2.getTime() - d1.getTime());
		System.out.println(d2.toString().equals(d1.toString()));*/
		
		if(!(d2.toString().equals(d1.toString())))
		{
			json += "{\"color\": \"gold\", \"text\": \"" + "\n\n [Create 3 new choices]" + " \", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q regen\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "You can only do this once.\nAlso replaces your current quest." + "\"}}, ";
		}
		
		//if(!Quests.getInstance().getRewards().minimumMet(reward))
		//	json += "{\"color\": \"gray\", \"text\": \"\n Choosing a new quest will decrease the reward to " + Quests.getInstance().getPlugin().economy.format(reward) + ".\"}, ";
		
		json += "{\"text\": \"\n\"} ";
		
		json += "]";
		
		sendJsonMessage(player, json);
	}
	
	/*static void displayProgress(Player player, Quest quest)
	{
		String json = "[";
		json += "{\"text\": \" " + progressBar(quest.getProgress(), quest.getGoal()) + "\n\", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + quest.getProgress() + " out of " + quest.getGoal() + "\"}} ";
		json += "]";
		
		sendJsonMessage(player, json);
	}*/
	
	static void displayQuestSelected(Player player)
	{
		String str = format("&f[&2Quests&f] ");
				
		String json = "[";
		json += "{\"text\": \"" + str + format("&fYour quest has been updated.") + " \"}, ";
		json += "{\"text\": \"" + format("&aClick here for details.") + "\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Click to see quest details.\"}} ";
		json += "]";
		
		sendJsonMessage(player, json);
	}
	
	static void displayQuestCompleteToPlayer(Player player)
	{
		String str = format("&f[&2Quests&f] ");
				
		String json = "[";
		json += "{\"text\": \"" + str + "\"}, ";
		json += "{\"color\": \"white\", \"text\": \"Quest completed! Good job. \"}, ";
		json += "{\"color\": \"green\", \"text\": \"Here's a new one.\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Click to see quest details.\"}} ";
		json += "]";
		
		sendJsonMessage(player, json);
	}
	
	static void displayReminder(BukkitPlayer player)
	{
		String str = format("&f[&2Quests&f] ");
		
		String json = "[";
		
		json += "{\"text\": \"" + str + "\"}, ";
		
		if(player.activeQuest.getProgress() == 0)
			json += "{\"color\": \"white\", \"text\": \"You have a new quest. \"}, ";
		else
			json += "{\"color\": \"white\", \"text\": \"You have an unfinished quest. \"}, ";
		
		json += "{\"color\": \"green\", \"text\": \"Click here for details.\", \"clickEvent\": {\"action\": \"run_command\", \"value\": \"/q\" }, \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"" + ChatColor.GREEN + "Click to see quest details.\"}} ";
		json += "]";
		
		sendJsonMessage(player.getPlayer(), json);
	}
	
	static void displayHighscore(Player player)
	{
		String msg = ChatColor.AQUA + "=== Quests Highscore ===\n";
		
		Map<String, Integer> list = HistoryModel.getHighscore();
		
		if(list.size() > 0)
		{
			int i = 1;
			int max = 0;
			
			for(int v : list.values())
			{
				if(v > max)
					max = v;
			}
			
			for(Entry<String, Integer> hi : list.entrySet())
			{
				msg += "§3#" + String.format("%02d", i) + " §f" + String.format("%0" + String.valueOf(max).length() + "d", hi.getValue()) + " §b" + hi.getKey() + "\n";
				i++;
			}
		}
		
		msg += " \n";
		
		player.sendMessage(msg);
	}
	
	static void displayAdminCommands(Player player)
	{
		String msg = ChatColor.AQUA + "Quests admin interface usage" + "\n";
		msg += " §b/qa" + " §3Show this menu" + "\n";
		msg += " §b/qa complete" + " §3Force completion of your current quest" + "\n";
		msg += " §b/qa regentype <type>" + " §3Choose a new quest of same <type>" + "\n";
		msg += " §b/qa new" + " §3Replace your current quest with a new one" + "\n";
		msg += " §b/qa json" + " §3Show the JSON config for your current quest" + "\n";
		
		player.sendMessage(msg);
	}
	
	static void playerMessage(Player player, String msg)
	{
		String str = "&f[&2Quests&f] " + msg;
		player.sendMessage(format(str));
	}
	
	/*static String progressBar(double has, double needs)
    {
        final int len = 60;
        
        double percentage = Math.min(100.0, has / needs);
        has = (int)(percentage * (double)len);
        
        StringBuilder sb = new StringBuilder();
        sb.append("&3[&f");
        
        for(int i = 0; i < len; ++i)
        {
            if(has == i)
            	sb.append("&7");
            
            sb.append("|");
        }
        
        sb.append("&3]");
        
        return format(sb.toString());
    }*/
	
	static String format(String msg, Object... args)
    {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        
        if(args.length > 0)
        	msg = String.format(msg, args);
        
        return msg;
    }
	
	static String formatDouble(double d)
	{
		return new DecimalFormat("###.#").format(d);
	}
	
	static String stripNotes(String str)
	{
		if(str.indexOf("\n Note:") > 0)
		{
			int idx = str.indexOf("\n Note:");
			
			return str.substring(0, idx);
		}
		
		return str;
	}
	
	@SuppressWarnings("deprecation")
	static void title(Player player, String title, String subtitle)
    {
        player.sendTitle(format(title), format(subtitle));
    }
	
	private static boolean sendJsonMessage(Player player, String json)
	{
		if(player == null)
	    	return false;
	    
	    final CommandSender console = Quests.getInstance().getPlugin().getServer().getConsoleSender();
	    final String command = "minecraft:tellraw " + player.getName() + " " + json;
	
	    Quests.getInstance().getPlugin().getServer().dispatchCommand(console, command);
	    
	    return true;
	}
}
