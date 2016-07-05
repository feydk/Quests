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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestFish extends Quest implements Listener
{
	// Quest values.
	private FishData data;
	
	// Config data values.
	private List<FishData> configData;
	
	public QuestFish create(int goal, FishData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.FISH;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		FishData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = new Random().nextInt(data.Max) + data.Min;

		return new QuestFish().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<FishData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.fish").getList("items"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				FishData data = new FishData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
				data.Plural = values.get("plural").toString();
				data.Singular = values.get("singular").toString();
				
				if(set.getKey().equals("TREASURE"))
					data.Subcat = "treasure";
				else if(set.getKey().equals("JUNK"))
					data.Subcat = "junk";
				else
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
		String str = "Catch " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Fishing";
	}
	
	@Override
	String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("quest", getQuestType().toString());
		
		if(data.Type != null)
			arr.put("type", data.Type.toString());
		else
			arr.put("type", data.Subcat.toUpperCase());
		
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
			
			QuestFish q = new QuestFish();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new FishData();


			if(map.get("type").toString().equals("TREASURE"))
				q.data.Subcat = "treasure";
			else if(map.get("type").toString().equals("JUNK"))
				q.data.Subcat = "junk";
			else
				q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(map.containsKey("id") && q.data.Subcat == null && q.data.Type != null)
			{
				q.data.Id = Integer.parseInt(map.get("id").toString());
				
				for(FishData bd : configData)
				{
					if(bd.Type != null && bd.Type.equals(q.data.Type) && bd.Id == q.data.Id)
					{
						q.data.Singular = bd.Singular;
						q.data.Plural = bd.Plural;
						break;
					}
				}
			}
			else
			{			
				for(FishData bd : configData)
				{
					if(q.data.Type != null)
					{
						if(bd.Type != null && bd.Type.equals(q.data.Type))
						{
							q.data.Singular = bd.Singular;
							q.data.Plural = bd.Plural;
							break;
						}
					}
					else
					{
						if(bd.Subcat != null && bd.Subcat.equals(q.data.Subcat))
						{
							q.data.Singular = bd.Singular;
							q.data.Plural = bd.Plural;
							break;
						}
					}
				}
			}
			
			return q;
		}
		catch(ParseException e)
		{
			e.printStackTrace();
			System.out.println("Quest JSON couldn't load: " + json);
		}
		
		return null;
	}
	
	public class FishData
	{
		public Material Type;
		public int Id;
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
		public String Subcat;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerFish(PlayerFishEvent event)
	{
		if(event.getCaught() == null)
			return;

		if(!(event.getCaught() instanceof Item))
			return;
		
		Player p = event.getPlayer();
		
		if(!p.hasPermission("quests.quests"))
			return;
		
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestFish q = (QuestFish)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		ItemStack fishie = ((Item)event.getCaught()).getItemStack();
		
		if(q.data.Type != null)
		{
			if(fishie.getType().equals(q.data.Type) && fishie.getData().getData() == q.data.Id)
				q.updateProgress(player, 1);
		}
		else
		{
			if(q.data.Subcat.equals("treasure"))
			{
				// Check for treasure items from http://minecraft.gamepedia.com/Fishing#Junk_and_treasures
				
				Material[] treasure = new Material[] { Material.BOW, Material.ENCHANTED_BOOK, Material.NAME_TAG, Material.SADDLE, Material.WATER_LILY, Material.FISHING_ROD };
				
				for(Material mat : treasure)
				{
					if(fishie.getType().equals(mat))
					{
						if(mat.equals(Material.FISHING_ROD))
						{
							// Must be enchanted.
							if(fishie.getEnchantments().size() > 0)
								q.updateProgress(player, 1);
						}
						else
						{
							q.updateProgress(player, 1);
						}
					}
				}
			}
			else if(q.data.Subcat.equals("junk"))
			{
				Material[] junk = new Material[] { Material.BOWL, Material.FISHING_ROD, Material.LEATHER, Material.LEATHER_BOOTS, Material.ROTTEN_FLESH, Material.STICK, Material.STRING, Material.POTION, Material.BONE, Material.INK_SACK, Material.TRIPWIRE_HOOK };
				
				for(Material mat : junk)
				{
					if(fishie.getType().equals(mat))
					{
						if(mat.equals(Material.FISHING_ROD))
						{
							// Must NOT be enchanted.
							if(fishie.getEnchantments().size() == 0)
								q.updateProgress(player, 1);
						}
						else
						{
							q.updateProgress(player, 1);
						}
					}
				}
			}
		}
	}
}