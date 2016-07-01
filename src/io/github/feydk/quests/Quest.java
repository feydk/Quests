package io.github.feydk.quests;

import io.github.feydk.quests.QuestsRewards.RewardEntry;

import java.util.Calendar;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public abstract class Quest
{
	boolean enabled = true;
	double progress;
	double goal;
	double reward;
	
	// Generic modifiers.
	/*private List<Biome> modifierBiomes;
	private boolean modifierNighttime;
	private boolean modifierRaining;*/
	
	QuestsPlugin getPlugin()
    {
		return QuestsPlugin.getInstance();
    }

    Quests getQuests()
    {
    	return Quests.getInstance();
    }
    
    void onEnable()
    {}
    
    void onDisable()
    {}
    
    /*void configure()
    {
        //enabled = getConfig().getBoolean("Enabled", true);
    }*/
    
    abstract QuestType getQuestType();
    abstract Quest generate();
    abstract void configure();
    abstract String getDescription();
    abstract String getTypeName();
    abstract String toJson();
    abstract Quest fromJson(String json);
    
    double getProgress()
	{
		return progress;
	}
	
	double getGoal()
	{
		return goal;
	}
	
	void forceComplete(BukkitPlayer player)
	{
		this.progress = goal;
		this.onUpdateProgress(player, this, goal);
	}
	
	void updateProgress(BukkitPlayer player, double progress)
	{
		this.progress += progress;
		
		onUpdateProgress(player, this, goal);
	}
    
    void onUpdateProgress(final BukkitPlayer player, Quest quest, double goal)
    {
    	if(player.timestampBegin <= 0)
    	{
    		player.timestampBegin = Calendar.getInstance().getTime().getTime();
    	
    		PlayerModel.updateTimestamp(player.uuid, player.timestampBegin);
    	}
    	
    	if(quest.getProgress() >= goal)
		{
    		onComplete(player);
		}
		else
		{
			PlayerModel.updateProgress(player.uuid, quest);
			
			float percentage = (100f / (float)goal) * (float)quest.getProgress();
			percentage = percentage / 100f;
			
			player.bossbar.setProgress(percentage);
			
			if(player.getSettings().useSounds)
			{
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_HARP, 1, percentage);
			}
			
			if(player.getSettings().showProgressbar)
			{
				player.bossbar.setVisible(true);
				
				if(player.taskid > 0)
					getPlugin().getServer().getScheduler().cancelTask(player.taskid);
				
				player.taskid = getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
				{
					public void run()
					{
						player.bossbar.setVisible(false);
					}
				}, 200);
					
				//QuestUI.title(player.getPlayer(), "", QuestUI.progressBar(quest.getProgress(), goal));
			}
		}
    }
    
    void onComplete(BukkitPlayer player)
    {
    	player.bossbar.setVisible(false);
    	
    	if(player.getSettings().useSounds)
    		player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    	
		QuestUI.title(player.getPlayer(), "", "&aQuest Completed!");
    	
    	HistoryModel.create(player.uuid, player.activeQuest.toJson(), QuestStatus.COMPLETED.ordinal(), player.activeQuest.reward, player.activeQuest.getTypeName(), player.timestampBegin, player.timestampCreated);
    	getQuests().announceQuestCompletion(player, player.activeQuest);
    	
    	QuestCompletedEvent.call(player.getPlayer(), player.index);
    	getQuests().giveMoney(player.getPlayer(), player.activeQuest.reward);
    	
    	RewardEntry reward = getQuests().getRewards().getTriggerReward(player.index);
    	
    	if(reward != null)
    	{
    		String command = reward.Command;
			command = command.replaceAll("%player%", player.getPlayer().getName());
			command = command.replaceAll("%uuid%", player.uuid.toString());
			
			getPlugin().getServer().dispatchCommand(getPlugin().getServer().getConsoleSender(), command);
    	}
    	
    	player.index = player.getIndex();
    	
    	// Regenerate choices to avoid players having the option to do the same quest over and over.
    	List<Quest> list = getQuests().generateQuests(3);
    	player.saveChoices(list);
    	player.assignQuest(list.get(0));
    			
		QuestUI.displayQuestCompleteToPlayer(player.getPlayer());
    }
    
    boolean checkForValidModifiers(Player player)
    {
    	// Check biome.
    	/*if(modifierBiomes != null && modifierBiomes.size() > 0)
		{
			if(!modifierBiomes.contains(player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ())))
				return false;
		}
    	
    	// Check time of day.
    	if(modifierNighttime)
		{
			if(player.getWorld().getTime() < 12500)
				return false;
		}
    	
    	// Check weather.
    	if(modifierRaining)
    	{
    		if(!player.getWorld().hasStorm())
    			return false;
    	}*/
    	
    	return true;
    }
    
    /*
    private void giveMoney(Player player, double money)
    {
        if(money < 0.01)
        	return;
        
        getQuests().giveMoney(player, money);
    }*/
}