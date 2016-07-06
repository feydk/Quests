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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestCraft extends Quest implements Listener
{
	// Quest values.
	private CraftData data;
	
	// Config data values.
	private List<CraftData> configData;
	
	public QuestCraft create(int goal, CraftData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.CRAFT;
	}

	@Override
	Quest generate()
	{
		Collections.shuffle(configData);
		CraftData data = configData.get(0);
		
		int amount = new Random().nextInt(data.Max) + data.Min;

		return new QuestCraft().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<CraftData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.craft").getList("items"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				CraftData data = new CraftData();
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
		String str = "Craft " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Crafting";
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
			
			QuestCraft q = new QuestCraft();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new CraftData();
			q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(map.containsKey("id"))
			{
				q.data.Id = Integer.parseInt(map.get("id").toString());
				
				for(CraftData bd : configData)
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
				for(CraftData bd : configData)
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
	
	public class CraftData
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
	private void onCraftItem(CraftItemEvent event)
	{
		if(!event.getWhoClicked().hasPermission("quests.quests"))
			return;
		
		Player p = (Player)event.getWhoClicked();
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestCraft q = (QuestCraft)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		if(!event.getRecipe().getResult().getType().equals(q.data.Type))
			return;

		if(p.getItemOnCursor().getType() != Material.AIR)
			return;
		
		int amount = 0;
		
		if(event.isShiftClick())
		{
			int low = 999;
			
			for(ItemStack is : event.getInventory().getMatrix())
			{
				if(is.getAmount() < low && is.getAmount() > 0)
					low = is.getAmount();
			}
			
			amount = event.getRecipe().getResult().getAmount() * low;
		}
		else
		{
			amount = event.getRecipe().getResult().getAmount();
		}
		
		q.updateProgress(player, amount);
	}
}