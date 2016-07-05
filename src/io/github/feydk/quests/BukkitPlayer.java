package io.github.feydk.quests;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BukkitPlayer
{
	final UUID uuid;
	Quest activeQuest;
	private PlayerSettings settings;
	private String settingsJson;
	long timestampBegin = -1;
	long timestampCreated;
	int index = 1;
	BossBar bossbar;
	int taskid;
	
	BukkitPlayer(UUID uuid)
    {
        this.uuid = uuid;
        bossbar = Bukkit.createBossBar("Quest Progress", BarColor.GREEN, BarStyle.SOLID);
        bossbar.addPlayer(getPlayer());
        bossbar.setVisible(false);
        //getSettings();
    }
	
	static Quests getQuests()
    {
        return Quests.getInstance();
    }

    static QuestsPlugin getPlugin()
    {
        return QuestsPlugin.getInstance();
    }

    Player getPlayer()
    {
        return Bukkit.getServer().getPlayer(uuid);
    }
    
    PlayerSettings getSettings()
    {
    	if(settings == null)
    		settings = PlayerSettings.fromJson(settingsJson);
    	
    	return settings;
    }
    
    void changeSetting(String setting, String value)
    {
    	if(setting.equals("progressbar"))
    		getSettings().showProgressbar = (value.equals("on"));
    	else if(setting.equals("sound"))
    		getSettings().useSounds = (value.equals("on"));
    	else if(setting.equals("announcement"))
    		getSettings().showOthersCompletions = (value.equals("on"));
    	else if(setting.equals("reminder"))
    		getSettings().useReminders = (value.equals("on"));

    	PlayerModel.updateSettings(uuid, getSettings());
    	
    	if(!getSettings().showProgressbar)
    	{
    		bossbar.setVisible(false);
    	}
    	else
    	{
    		if(activeQuest.getProgress() > 0)
    			bossbar.setVisible(true);
    	}
    }
    
    void saveChoices(List<Quest> list)
    {
    	PlayerModel.updateChoices(uuid, list.get(0), list.get(1), list.get(2));
    }
    
    void selectQuest(int idx)
    {
    	//HistoryModel.create(uuid, activeQuest.toJson(), QuestStatus.SKIPPED.ordinal(), 0, activeQuest.getTypeName(), timestampBegin, timestampCreated);
    	Quest q = PlayerModel.getChoice(uuid, idx);
    	//index++;
    	assignQuest(q);
    }
    
    /*
     * Initialize the player instance. Get/create active quest and update name if needed.
     */
    void init()
    {
    	PlayerModel obj = PlayerModel.loadByUuid(uuid);
    	
    	if(obj == null)
    	{
    		PlayerModel.create(uuid, getPlayer().getName(), PlayerSettings.getDefaultJson());
    		
    		List<Quest> list = getQuests().generateQuests(3);
        	saveChoices(list);
    		
    		assignQuest(list.get(0));
    		settingsJson = PlayerSettings.getDefaultJson();
    	}
    	else
    	{
    		if(!obj.name.equals(getPlayer().getName()))
    			PlayerModel.updateName(uuid, getPlayer().getName());
    		
    		index = PlayerModel.getIndex(uuid);
		    //System.out.println("index: " + index);
    		
    		if(!obj.quest_active.isEmpty())
    		{
    			activeQuest = getQuests().fromJson(obj.quest_active);

			    if(activeQuest == null)
			    {
				    List<Quest> list = getQuests().generateQuests(3);
				    saveChoices(list);

				    assignQuest(list.get(0));
			    }

    			activeQuest.reward = getQuests().getRewards().getReward(index);
    		}
    		else
    		{
    			assignQuest(getQuests().getNewQuest());
    		}
    		
    		timestampCreated = obj.timestamp_created;
    		timestampBegin = obj.timestamp_begin;
    		settingsJson = obj.settings;
    	}
    }
    
    void assignQuest(Quest quest)
    {
    	activeQuest = quest;
    	activeQuest.reward = getQuests().getRewards().getReward(index);
    	timestampBegin = 0;
    	timestampCreated = Calendar.getInstance().getTime().getTime();
    	PlayerModel.updateQuest(uuid, activeQuest, timestampBegin, timestampCreated);
    }
    
    int getIndex()
    {
    	return PlayerModel.getIndex(uuid);
    }
}