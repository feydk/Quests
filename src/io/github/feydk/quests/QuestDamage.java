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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestDamage extends Quest implements Listener
{
	// Quest values.
	private DamageData data;
	
	// Config data values.
	private List<DamageData> configData;
	
	public QuestDamage create(int goal, DamageData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.TAKE_DAMAGE;
	}

	@Override
	Quest generate()
	{
		Collections.shuffle(configData);
		DamageData data = configData.get(0);
		
		int amount = new Random().nextInt(data.Max) + data.Min;
		
		return new QuestDamage().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<DamageData>();
		
		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		int min = config.getInt("quests.damage.min");
		int max = config.getInt("quests.damage.max");
		
		for(Object entry : config.getConfigurationSection("quests.damage").getList("from"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
			
				DamageData data = new DamageData();
				data.Max = max;
				data.Min = min;
				data.Name = values.get("name").toString();
				data.Type = EntityType.valueOf(set.getKey());
				
				configData.add(data);
			}
		}
	}

	@Override
	String getDescription()
	{
		String str = "Take " + QuestUI.formatDouble(goal) + " heart" + (goal == 1 ? "" : "s") + " of damage";
		
		if(data.Name.equals("any"))
			str += ".";
		else
			str += " from " + data.Name + ".";
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Take Damage";
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
			
			QuestDamage q = new QuestDamage();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new DamageData();
			q.data.Type = EntityType.valueOf(map.get("type").toString());
			
			for(DamageData bd : configData)
			{
				if(bd.Type.equals(q.data.Type))
				{
					q.data.Name = bd.Name;
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
	
	public class DamageData
	{
		public EntityType Type;
		public int Min;
		public int Max;
		public String Name;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onEntityDamage(EntityDamageEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		
		Player p = (Player)event.getEntity();
		
		if(!p.hasPermission("quests.quests"))
			return;
		
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestDamage q = (QuestDamage)player.activeQuest;
						
		if(!q.checkForValidModifiers(p))
			return;
		
		if(q.data.Type.equals(EntityType.UNKNOWN))
		{
			q.updateProgress(player, event.getFinalDamage() / 2);
		}
		else
		{
			if(event instanceof EntityDamageByEntityEvent)
			{
				Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
				
				if(damager instanceof Arrow)
				{
					ProjectileSource shooter = ((Arrow)damager).getShooter();
					
					if(shooter instanceof Entity)
						damager = (Entity)shooter;
				}
				
				if(damager.getType().equals(q.data.Type))
				{
					q.updateProgress(player, event.getFinalDamage() / 2);
				}
			}
		}
	}
}