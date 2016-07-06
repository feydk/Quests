package io.github.feydk.quests;

import java.util.Date;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor
{
	Quests getQuests()
    {
        return Quests.getInstance();
    }

    QuestsPlugin getPlugin()
    {
        return QuestsPlugin.getInstance();
    }

	@SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        final Player player = sender instanceof Player ? (Player)sender : null;

        if(player == null)
        {
            sender.sendMessage("Player expected");
            return true;
        }
        
        String cmd = args.length > 0 ? args[0].toLowerCase() : "";
        BukkitPlayer p = getQuests().getBukkitPlayer(player);
        
        if(cmd.equalsIgnoreCase(""))
        {
        	if(player.hasPermission("quests.quest"))
        		QuestUI.displayQuestInfo(player, p.activeQuest);
        }
        else if(cmd.equals("stats"))
        {
        	if(player.hasPermission("quests.quest"))
        		QuestUI.displayStats(p);
        }
        else if(cmd.equals("help") || cmd.equals("?"))
        {
        	if(player.hasPermission("quests.quest"))
        		QuestUI.displayHelp(p);
        }
        else if(cmd.equals("settings"))
        {
        	if(player.hasPermission("quests.quest"))
        		QuestUI.displaySettings(player, p.getSettings());
        }
        else if(cmd.equals("hi"))
        {
        	if(player.hasPermission("quests.quest"))
        		QuestUI.displayHighscore(player);
        }
        else if(cmd.equals("set"))
        {
        	if(player.hasPermission("quests.quest"))
        	{
        		p.changeSetting(args[1], args[2]);
        		QuestUI.displaySettings(player, p.getSettings());
        	}
        }
        else if(cmd.equals("select"))
        {
        	if(player.hasPermission("quests.quest"))
        	{
        		p.selectQuest(Integer.parseInt(args[1]));
        		
        		List<Quest> list = PlayerModel.getChoices(p.uuid);
            	
        		QuestUI.displayQuestInfo(player, p.activeQuest);
        		QuestUI.displayQuestSelection(player, list);
        		//QuestUI.displayQuestSelected(player);
        	}
        }
        else if(cmd.equals("choices"))
        {
        	if(player.hasPermission("quests.quest"))
        	{
        		List<Quest> list = PlayerModel.getChoices(p.uuid);
        	
        		QuestUI.displayQuestSelection(player, list);
        	}
        }
        else if(cmd.equals("regen"))
        {
        	if(player.hasPermission("quests.quest"))
        	{
		        Date d1 = new Date(p.timestampCreated);
		        d1.setHours(0);
		        d1.setMinutes(0);
		        d1.setSeconds(0);

		        Date d2 = new Date();
		        d2.setHours(0);
		        d2.setMinutes(0);
		        d2.setSeconds(0);

		        if(!(d2.toString().equals(d1.toString())))
		        {
			        List<Quest> list = getQuests().generateQuests(3);
			        p.saveChoices(list);
			        p.assignQuest(list.get(0));
			        QuestUI.displayQuestSelected(player);
		        }
            	
        		//QuestUI.displayQuestSelection(player, list);
        	}
        }
        else if(cmd.equals("reminder"))
        {
        	if(player.hasPermission("quests.quest"))
        		QuestUI.displayReminder(p);
        }
        
        return true;
    }
}