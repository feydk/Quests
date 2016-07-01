package io.github.feydk.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestKill extends Quest implements Listener
{
	// Quest values.
	private KillData data;
	
	// Config data values.
	private List<KillData> configData;
	
	public QuestKill create(int goal, KillData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.KILL;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		KillData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = new Random().nextInt(data.Max) + data.Min;
		
		return new QuestKill().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<KillData>();
		
		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.kill").getList("entities"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				KillData data = new KillData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
				data.Plural = values.get("plural").toString();
				data.Singular = values.get("singular").toString();
				data.Type = EntityType.valueOf(set.getKey());
				
				configData.add(data);
			}
		}
	}

	@Override
	String getDescription()
	{
		String str = "Kill " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Killing";
	}	
	
	@Override
	String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("quest", getQuestType().toString());
		arr.put("type", data.Type.toString());
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
			
			QuestKill q = new QuestKill();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new KillData();
			q.data.Type = EntityType.valueOf(map.get("type").toString());
			
			for(KillData bd : configData)
			{
				if(bd.Type.equals(q.data.Type))
				{
					q.data.Singular = bd.Singular;
					q.data.Plural = bd.Plural;
					break;
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
			
	public class KillData
	{
		public EntityType Type;
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onDeath(EntityDeathEvent event)
	{
		if(event.getEntity() instanceof Player)
			return;
		
		if(!(event.getEntity().getKiller() instanceof Player))
			return;

		if(event.getEntity().getCustomName() != null && !event.getEntity().getCustomName().isEmpty())
			return;
		
		Player p = (Player)event.getEntity().getKiller();
						
		if(!p.hasPermission("quests.quests"))
			return;
		
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestKill q = (QuestKill)player.activeQuest;
						
		if(!q.checkForValidModifiers(p))
			return;
		
		if(event.getEntity().getType().equals(q.data.Type))
		{
			q.updateProgress(player, 1);
		}
	}
}