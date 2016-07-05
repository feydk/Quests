package io.github.feydk.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Quests
{
	private static Quests instance;
	final Map<UUID, BukkitPlayer> players = new HashMap<UUID, BukkitPlayer>();
	final Map<QuestType, Quest> questMap = new EnumMap<QuestType, Quest>(QuestType.class);
	private QuestsRewards rewards;
	
	public Quests()
	{
		instance = this;
		
		questMap.put(QuestType.BREED, new QuestBreed());
		questMap.put(QuestType.CRAFT, new QuestCraft());
		questMap.put(QuestType.EAT, new QuestEat());
		questMap.put(QuestType.ENCHANT, new QuestEnchant());
		questMap.put(QuestType.FISH, new QuestFish());
		questMap.put(QuestType.GROW, new QuestGrow());
		questMap.put(QuestType.KILL, new QuestKill());
		questMap.put(QuestType.MINE, new QuestMine());
		questMap.put(QuestType.SMELT, new QuestSmelt());
		questMap.put(QuestType.TAKE_DAMAGE, new QuestDamage());
		questMap.put(QuestType.TAME, new QuestTame());
		questMap.put(QuestType.THROW, new QuestThrow());
		questMap.put(QuestType.TRADE, new QuestTrade());
	}
	
	public static Quests getInstance()
	{
		return instance;
	}
	
	void configure()
    {
		for(Quest q : questMap.values())
		{
			q.configure();
		}
		
		rewards = new QuestsRewards();
    }
	
	QuestsPlugin getPlugin()
    {
		return QuestsPlugin.getInstance();
    }
	
	public Collection<? extends Quest> getQuests()
	{
		return questMap.values();
	}
	
	public QuestsRewards getRewards()
	{
		return rewards;
	}
	
	void giveMoney(Player player, double amount)
	{
		getPlugin().economy.depositPlayer(player, amount);
	}
	
	public Quest getQuestOfType(QuestType type)
	{
		return questMap.get(type);
	}
	
	BukkitPlayer getBukkitPlayer(UUID uuid)
    {
        BukkitPlayer result = players.get(uuid);
        
        if(result == null)
        {
            result = new BukkitPlayer(uuid);
            players.put(uuid, result);
        }
        
        return result;
    }

    BukkitPlayer getBukkitPlayer(Player player)
    {
        return getBukkitPlayer(player.getUniqueId());
    }
    
    void announceQuestCompletion(BukkitPlayer player, Quest quest)
    {
    	final CommandSender console = getPlugin().getServer().getConsoleSender();
    	
    	String json = "[{\"text\": \"" + QuestUI.format("&f" + player.getPlayer().getName() + " completed a") + " \"},";
    	json += "{ \"text\": \"" + QuestUI.format("&a" + quest.getTypeName() + " quest") + "\", \"hoverEvent\": {\"action\": \"show_text\", \"value\": \"�a" + QuestUI.stripNotes(quest.getDescription()) + "\"}}]";
    	
    	for(Player p : getPlugin().getServer().getOnlinePlayers())
    	{
    		if(p.getUniqueId().equals(player.uuid))
    			continue;
    		
    		if(players.containsKey(p.getUniqueId()))
    		{
    			if(players.get(p.getUniqueId()).getSettings().showOthersCompletions)
    			{
    				final String command = "minecraft:tellraw " + p.getName() + " " + json;

    				getPlugin().getServer().dispatchCommand(console, command);
    			}
    		}
    	}
    }
    
    public List<Quest> generateQuests(int count)
    {
    	// Pick random quest types.
    	List<QuestType> picked = new ArrayList<QuestType>();
    	
    	List<QuestType> types = Arrays.asList(QuestType.values());
    	Collections.shuffle(types);
    	
    	for(int i = 0; i < count; i++)
    		picked.add(types.get(i));
    	
    	List<Quest> quests = new ArrayList<Quest>();
    	
    	for(QuestType type : picked)
    	{
    		Quest q = QuestGenerator.generate(type);
    		quests.add(q);
    	}
    	
    	return quests;
    }
    
    public List<Quest> generateQuests(int count, QuestType type)
    {
    	List<Quest> quests = new ArrayList<Quest>();
    	
    	for(int i = 0; i < count; i++)
    	{
    		Quest q = QuestGenerator.generate(type);
    		quests.add(q);
    	}
    	
    	return quests;
    }
    
    public Quest getNewQuest()
    {
    	List<Quest> list = generateQuests(1);
		
		return list.get(0);
    }
    
    @SuppressWarnings("unchecked")
	public Quest fromJson(String json)
    {
    	try
		{
			Object obj = new JSONParser().parse(json);
			Map<String, Object> map = (Map<String, Object>)obj;
			
			Quest q = getQuestOfType(QuestType.valueOf(map.get("quest").toString()));
			return q.fromJson(json);
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			System.out.println("Quest JSON couldn't load: " + json);
		}
    	
    	return null;
    }
}