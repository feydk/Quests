package io.github.feydk.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestGrow extends Quest implements Listener
{
	// Quest values.
	private GrowData data;
	
	// Config data values.
	private List<GrowData> configData;
	
	public QuestGrow create(int goal, GrowData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.GROW;
	}

	@Override
	Quest generate()
	{
		Collections.shuffle(configData);
		GrowData data = configData.get(0);
		
		int amount = new Random().nextInt(data.Max) + data.Min;
		
		return new QuestGrow().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<GrowData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.grow").getList("types"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
			
				GrowData data = new GrowData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
				data.Plural = values.get("plural").toString();
				data.Singular = values.get("singular").toString();
				data.Type = TreeType.valueOf(set.getKey());
				
				configData.add(data);
			}
		}
	}

	@Override
	String getDescription()
	{
		String str = "Grow " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Growing";
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
			
			QuestGrow q = new QuestGrow();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new GrowData();
			q.data.Type = TreeType.valueOf(map.get("type").toString());
			
			for(GrowData bd : configData)
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
	
	public class GrowData
	{
		public TreeType Type;
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onStructureGrow(final StructureGrowEvent event)
	{
		if(event.getPlayer() == null)
			return;
		
		if(!(event.getPlayer() instanceof Player))
			return;
		
		if(!event.getPlayer().hasPermission("quests.quests"))
			return;
		
		final Player p = event.getPlayer();
		final BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		final QuestGrow q = (QuestGrow)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
		{
			public void run()
			{
				if(event.getWorld().getBlockAt(event.getLocation()).getType() != Material.SAPLING)
				{
					TreeType grown = event.getSpecies();
						
					if(grown.equals(q.data.Type))
					{
						q.updateProgress(player, 1);
					}
				}
			}
		}, 1);
	}
}