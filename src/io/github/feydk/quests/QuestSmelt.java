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
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestSmelt extends Quest implements Listener
{
	// Quest values.
	private SmeltData data;
	
	// Config data values.
	private List<SmeltData> configData;
	
	public QuestSmelt create(int goal, SmeltData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.SMELT;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		SmeltData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = new Random().nextInt(data.Max) + data.Min;

		return new QuestSmelt().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<SmeltData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.smelt").getList("items"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				SmeltData data = new SmeltData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
				data.Plural = values.get("plural").toString();
				data.Singular = values.get("singular").toString();
				data.Type = Material.valueOf(set.getKey());
				data.Subcat = values.get("subcat").toString();
				
				if(values.containsKey("id"))
					data.Id = Integer.parseInt(values.get("id").toString());
				
				configData.add(data);
			}
		}
	}

	@Override
	String getDescription()
	{
		String str = "";
		
		if(data.Subcat.equals("cooking"))
			str = "Cook ";
		else
			str = "Smelt ";
		
		str += QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + " in a furnace.";
		str += "\n Note: you have to take items out of the furnace manually.";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		if(data.Subcat.equals("cooking"))
			return "Cooking";
		else
			return "Smelting";
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
			
			QuestSmelt q = new QuestSmelt();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new SmeltData();
			q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(map.containsKey("id"))
			{
				q.data.Id = Integer.parseInt(map.get("id").toString());
				
				for(SmeltData bd : configData)
				{
					if(bd.Type.equals(q.data.Type) && bd.Id == q.data.Id)
					{
						q.data.Singular = bd.Singular;
						q.data.Plural = bd.Plural;
						q.data.Subcat = bd.Subcat;
						break;
					}
				}
			}
			else
			{			
				for(SmeltData bd : configData)
				{
					if(bd.Type.equals(q.data.Type))
					{
						q.data.Singular = bd.Singular;
						q.data.Plural = bd.Plural;
						q.data.Subcat = bd.Subcat;
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
	
	public class SmeltData
	{
		public Material Type;
		public int Id;
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
		public String Subcat;
		public int ClickType;
		public int Amount;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onFurnaceExtract(FurnaceExtractEvent event)
	{
		Player p = event.getPlayer();
		
		if(!p.hasPermission("quests.quests"))
			return;
				
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestSmelt q = (QuestSmelt)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		if(event.getItemType().equals(q.data.Type))
		{
			int amount = event.getItemAmount();
			
			if(q.data.ClickType == 3)
			{
				amount = q.data.Amount;
				q.data.Amount = 0;
			}
			
			if(amount == 0)
				return;
							
			q.updateProgress(player, amount);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onInventoryClick(InventoryClickEvent event)
	{
		Player p = (Player)event.getWhoClicked();
		
		if(!p.hasPermission("quests.quests"))
			return;
		
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestSmelt q = (QuestSmelt)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
			
		if(event.getSlotType() != InventoryType.SlotType.RESULT)
			return;

		ItemStack item = event.getCurrentItem();

		if(item != null && item.getType() != Material.AIR)
		{
			if(event.isRightClick())
				q.data.ClickType = 1;
			else if(event.isLeftClick())
				q.data.ClickType = 2;
			else if(event.getHotbarButton() != -1)
				q.data.ClickType = 4;
			
			if(event.isShiftClick())
				q.data.ClickType = 3;
			
			q.data.Amount = item.getAmount();
		}
	}
}