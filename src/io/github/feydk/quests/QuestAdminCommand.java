package io.github.feydk.quests;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestAdminCommand implements CommandExecutor
{
	Quests getQuests()
    {
        return Quests.getInstance();
    }

    QuestsPlugin getPlugin()
    {
        return QuestsPlugin.getInstance();
    }

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
        		QuestUI.displayAdminCommands(player);
        }
        else if(cmd.equals("regentype"))
        {
        	if(player.hasPermission("quests.admin"))
        	{
        		List<Quest> list = getQuests().generateQuests(3, QuestType.valueOf(args[1].toUpperCase()));
        	
        		p.saveChoices(list);
        	
        		QuestUI.displayQuestSelection(player, list);
        	}
        }
        else if(cmd.equals("new"))
        {
        	if(player.hasPermission("quests.admin"))
        		p.assignQuest(getQuests().getNewQuest());
        }
        else if(cmd.equals("json"))
        {
        	if(player.hasPermission("quests.admin"))
        		player.sendMessage(p.activeQuest.toJson());
        }
        else if(cmd.equals("complete"))
        {
        	if(player.hasPermission("quests.admin"))
        		p.activeQuest.forceComplete(p);
        }
        
        return true;
    }
}