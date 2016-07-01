package io.github.feydk.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestEnchant extends Quest implements Listener
{
	// Quest values.
	private EnchantData data;
	
	// Config data values.
	private List<EnchantData> configData;
	
	public QuestEnchant create(int goal, EnchantData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.ENCHANT;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		EnchantData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = 1;

		return new QuestEnchant().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<EnchantData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.enchant").getList("items"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				EnchantData data = new EnchantData();
				data.Name = values.get("name").toString();
				data.Type = Material.valueOf(set.getKey());
				
				if(values.containsKey("id"))
					data.Id = Integer.parseInt(values.get("id").toString());
				
				configData.add(data);
			}
		}
	}

	@Override
	String getDescription()
	{
		String str = "Enchant " + data.Name + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Enchanting";
	}
	
	@Override
	String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("quest", getQuestType().toString());
		arr.put("type", data.Type.toString());
		arr.put("id", data.Id);
		arr.put("goal", goal);
		arr.put("progress", progress);
		
		return JSONValue.toJSONString(arr);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	Quest fromJson(String json)
	{
		try
		{
			Object obj = new JSONParser().parse(json);
			Map<String, Object> map = (Map<String, Object>)obj;
			
			QuestEnchant q = new QuestEnchant();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new EnchantData();
			q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(map.containsKey("id"))
			{
				q.data.Id = Integer.parseInt(map.get("id").toString());
				
				for(EnchantData bd : configData)
				{
					if(bd.Type.equals(q.data.Type) && bd.Id == q.data.Id)
					{
						q.data.Name = bd.Name;
						break;
					}
				}
			}
			else
			{			
				for(EnchantData bd : configData)
				{
					if(bd.Type.equals(q.data.Type))
					{
						q.data.Name = bd.Name;
						break;
					}
				}
			}
			
			return q;
		}
		catch(ParseException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public class EnchantData
	{
		public Material Type;
		public int Id;
		public String Name;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onEnchantItem(EnchantItemEvent event)
	{
		Player p = (Player)event.getEnchanter();
		
		if(!p.hasPermission("quests.quests"))
			return;
				
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestEnchant q = (QuestEnchant)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		if(!event.getItem().getType().equals(q.data.Type))
			return;
						
		q.updateProgress(player, 1);
	}
}