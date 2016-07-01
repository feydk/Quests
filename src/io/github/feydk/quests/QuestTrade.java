package io.github.feydk.quests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestTrade extends Quest implements Listener
{
	// Quest values.
	private TradeData data;
	
	// Config data values.
	private List<TradeData> configData;
	
	public QuestTrade create(int goal, TradeData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.TRADE;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		TradeData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = new Random().nextInt(data.Max) + data.Min;
		
		return new QuestTrade().create(amount, data);		
	}

	@Override
	void configure()
	{
		configData = new ArrayList<TradeData>();
		
		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		ConfigurationSection map = config.getConfigurationSection("quests.trade.general");
		
		TradeData data = new TradeData();
		data.Max = map.getInt("max");
		data.Min = map.getInt("min");
		data.Plural = map.getString("plural");
		data.Singular = map.getString("singular");
		
		configData.add(data);
	}

	@Override
	String getDescription()
	{
		String str = "Do " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + " with a villager.";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Trading";
	}	
	
	@Override
	String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("quest", getQuestType().toString());
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
			
			QuestTrade q = new QuestTrade();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new TradeData();
			q.data.Singular = configData.get(0).Singular;
			q.data.Plural = configData.get(0).Plural;
			
			return q;
		}
		catch(ParseException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
			
	public class TradeData
	{
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
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
		
		QuestTrade q = (QuestTrade)player.activeQuest;
						
		if(!q.checkForValidModifiers(p))
			return;
		
		if(event.getInventory().getType() == InventoryType.MERCHANT && event.getRawSlot() == 2 && event.getCurrentItem().getType() != Material.AIR)
		{
			// If player inventory is full the trade can't be completed, so don't count progress.
			if(p.getInventory().firstEmpty() == -1)
				return;
			
			q.updateProgress(player, 1);
		}
	}
}