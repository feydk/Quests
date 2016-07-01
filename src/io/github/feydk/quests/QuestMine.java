package io.github.feydk.quests;

import java.util.ArrayList;
import java.util.Collection;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.winthier.exploits.bukkit.BukkitExploits;

public class QuestMine extends Quest implements Listener
{
	// Quest values.
	private MineData data;
	
	// Config data values.
	private List<MineData> configData;
		
	public QuestMine create(int goal, MineData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.MINE;
	}

	@Override
	Quest generate()
	{
		Collections.shuffle(configData);
		MineData data = configData.get(0);
		
		int amount = new Random().nextInt(data.Max) + data.Min;
		
		return new QuestMine().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<MineData>();
		
		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		for(Object entry : config.getConfigurationSection("quests.mine").getList("blocks"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				MineData data = new MineData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
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
		String str = "Mine " + QuestUI.formatDouble(goal) + " block" + (goal == 1 ? "" : "s") + " of " + data.Name + ".";

		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Mining";
	}
	
	@Override
	String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("quest", getQuestType().toString());
		arr.put("type", data.Type.toString());
		arr.put("goal", goal);
		arr.put("progress", progress);
		arr.put("id", data.Id);
		
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
			
			QuestMine q = new QuestMine();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new MineData();
			q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(map.containsKey("id"))
			{
				q.data.Id = Integer.parseInt(map.get("id").toString());
				
				for(MineData bd : configData)
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
				for(MineData bd : configData)
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
	
	public class MineData
	{
		public Material Type;
		public int Min;
		public int Max;
		public String Name;
		public int Id;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onBlockBreak(BlockBreakEvent event)
	{
		if(!event.getPlayer().hasPermission("quests.quests"))
			return;
		
		Player p = event.getPlayer();
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestMine q = (QuestMine)player.activeQuest;
		
		// Ignore if the block was placed by a player.
		if(BukkitExploits.getInstance().isPlayerPlaced(event.getBlock()))
			return;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		Collection<ItemStack> dropped_items = event.getBlock().getDrops();
		ItemStack mined_item = new ItemStack(event.getBlock().getType());
		
		// Fix for redstone ore.
		if(event.getBlock().getType() == Material.GLOWING_REDSTONE_ORE)
			mined_item = new ItemStack(Material.REDSTONE_ORE);
		
		ItemStack dropped_item = (ItemStack)(dropped_items.size() > 0 ? dropped_items.toArray()[0] : null);
		
		if((dropped_item != null && dropped_item.getType().equals(q.data.Type) && dropped_item.getData().getData() == q.data.Id) || (mined_item.getType().equals(q.data.Type) && mined_item.getData().getData() == q.data.Id))
		{
			q.updateProgress(player, 1);
		}
	}
}