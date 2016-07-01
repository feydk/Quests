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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestEat extends Quest implements Listener
{
	// Quest values.
	private EatData data;
	
	// Config data values.
	private List<EatData> configData;
	
	public QuestEat create(int goal, EatData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.EAT;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		EatData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = new Random().nextInt(data.Max) + data.Min;

		return new QuestEat().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<EatData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.eat").getList("foods"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				EatData data = new EatData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
				data.Plural = values.get("plural").toString();
				data.Singular = values.get("singular").toString();
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
		String str = "Eat " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Eating";
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
			
			QuestEat q = new QuestEat();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new EatData();
			q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(map.containsKey("id"))
			{
				q.data.Id = Integer.parseInt(map.get("id").toString());
				
				for(EatData bd : configData)
				{
					if(bd.Type.equals(q.data.Type) && bd.Id == q.data.Id)
					{
						q.data.Singular = bd.Singular;
						q.data.Plural = bd.Plural;
						break;
					}
				}
			}
			else
			{			
				for(EatData bd : configData)
				{
					if(bd.Type.equals(q.data.Type))
					{
						q.data.Singular = bd.Singular;
						q.data.Plural = bd.Plural;
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
	
	public class EatData
	{
		public Material Type;
		public int Id;
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerItemConsume(PlayerItemConsumeEvent event)
	{
		if(event.getPlayer() == null)
			return;
		
		if(!event.getPlayer().hasPermission("quests.quests"))
			return;
		
		Player p = event.getPlayer();
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestEat q = (QuestEat)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		ItemStack eaten = event.getItem();
		//eaten.setAmount(1);
			
		if(q.data.Type.equals(eaten.getType()) && q.data.Id == eaten.getData().getData())
		{
			q.updateProgress(player, 1);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onBlockClick(PlayerInteractEvent event)
	{
		if(event.getPlayer() == null)
			return;
		
		if(!event.getPlayer().hasPermission("quests.quests"))
			return;
		
		Player p = event.getPlayer();
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestEat q = (QuestEat)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		if(q.data.Type.equals(Material.CAKE_BLOCK) && p.getFoodLevel() < 20 && event.getClickedBlock().getType() == Material.CAKE_BLOCK && event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			q.updateProgress(player, 1);
		}
	}
}