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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuestThrow extends Quest implements Listener
{
	// Quest values.
	private ThrowData data;
	
	// Config data values.
	private List<ThrowData> configData;
	
	public QuestThrow create(int goal, ThrowData data)
	{
		this.goal = goal;
		this.data = data;
		
		return this;
	}
	
	@Override
	QuestType getQuestType()
	{
		return QuestType.THROW;
	}

	@Override
	Quest generate()
	{
		// Pick a random entity type to breed.
		Collections.shuffle(configData);
		ThrowData data = configData.get(0);
		
		// Pick a random amount within the allowed range.
		int amount = new Random().nextInt(data.Max) + data.Min;

		return new QuestThrow().create(amount, data);		
	}

	@SuppressWarnings("unchecked")
	@Override
	void configure()
	{
		configData = new ArrayList<ThrowData>();

		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();

		for(Object entry : config.getConfigurationSection("quests.throw").getList("items"))
		{
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)entry;
						
			for(Entry<String, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
				
				ThrowData data = new ThrowData();
				data.Max = Integer.parseInt(values.get("max").toString());
				data.Min = Integer.parseInt(values.get("min").toString());
				data.Plural = values.get("plural").toString();
				data.Singular = values.get("singular").toString();
				data.Type = Material.valueOf(set.getKey());
				
				if(values.containsKey("potion"))
					data.Potion = PotionType.valueOf(values.get("potion").toString());
				
				if(values.containsKey("hatch"))
					data.Hatch = Boolean.parseBoolean(values.get("hatch").toString());
				
				configData.add(data);
			}
		}
	}

	@Override
	String getDescription()
	{
		String str = "";
		
		if(data.Potion != null)
		{
			str = "Throw " + QuestUI.formatDouble(goal) + " splash potion of " + (goal == 1 ? data.Singular : data.Plural) + ".";
		}
		else
		{
			if(data.Hatch)
			{
				str = "Hatch " + QuestUI.formatDouble(goal) + " chicken" + (goal == 1 ? "" : "s") + " by throwing eggs.";
			}
			else
			{
				str = "Throw " + QuestUI.formatDouble(goal) + " " + (goal == 1 ? data.Singular : data.Plural) + ".";
			}
		}
		
		return str;
	}
	
	@Override
	String getTypeName()
	{
		return "Throwing";
	}
	
	@Override
	String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("quest", getQuestType().toString());
		arr.put("type", data.Type.toString());
		arr.put("goal", goal);
		arr.put("progress", progress);
		
		if(data.Potion != null)
			arr.put("potion", data.Potion.toString());
		
		if(data.Type.equals(Material.EGG))
			arr.put("hatch", data.Hatch);
		
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
			
			QuestThrow q = new QuestThrow();
			q.goal = Double.parseDouble(map.get("goal").toString());
			q.progress = Double.parseDouble(map.get("progress").toString());
			q.data = new ThrowData();
			q.data.Type = Material.valueOf(map.get("type").toString());
			
			if(q.data.Type.equals(Material.EGG))
				q.data.Hatch = Boolean.parseBoolean(map.get("hatch").toString());
			
			if(map.containsKey("potion"))
			{
				q.data.Potion = PotionType.valueOf(map.get("potion").toString());
				
				for(ThrowData bd : configData)
				{
					if(bd.Type.equals(q.data.Type) && bd.Potion == q.data.Potion)
					{
						q.data.Singular = bd.Singular;
						q.data.Plural = bd.Plural;
						break;
					}
				}
			}
			else
			{
				for(ThrowData bd : configData)
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
	
	public class ThrowData
	{
		public Material Type;
		public PotionType Potion;
		public boolean Hatch;
		public int Min;
		public int Max;
		public String Singular;
		public String Plural;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPotionThrow(PotionSplashEvent event)
	{
		ThrownPotion potion = event.getPotion();
        ProjectileSource source = potion.getShooter();
		
        if(!(source instanceof Player))
			return;
        
        Player p = (Player)source;
		
		if(!p.hasPermission("quests.quests"))
			return;
		
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestThrow q = (QuestThrow)player.activeQuest;
		
		if(!q.checkForValidModifiers(p))
			return;
		
		PotionMeta meta = (PotionMeta)potion.getItem().getItemMeta();
		
		if(meta.getBasePotionData().getType().equals(q.data.Potion))
		{
			q.updateProgress(player, 1);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerEggThrow(PlayerEggThrowEvent event)
	{
		if(!event.getPlayer().hasPermission("quests.quests"))
			return;
		
		Player p = (Player)event.getPlayer();
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestThrow q = (QuestThrow)player.activeQuest;
		
		if(!(q.data.Type.equals(Material.EGG)))
			return;
								
		if(!q.checkForValidModifiers(p))
			return;
		
		if(q.data.Hatch && event.getHatchingType() == EntityType.CHICKEN && event.getNumHatches() > 0)
		{
			q.updateProgress(player, event.getNumHatches());
		}
		else if(!q.data.Hatch)
		{
			q.updateProgress(player, 1);
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if(!(event.getCause().equals(TeleportCause.ENDER_PEARL)))
			return;
		
		if(!event.getPlayer().hasPermission("quests.quests"))
			return;
		
		Player p = (Player)event.getPlayer();
		BukkitPlayer player = Quests.getInstance().getBukkitPlayer(p);
		
		if(player.activeQuest.getQuestType() != this.getQuestType())
			return;
		
		QuestThrow q = (QuestThrow)player.activeQuest;
		
		if(!(q.data.Type.equals(Material.ENDER_PEARL)))
			return;
								
		if(!q.checkForValidModifiers(p))
			return;
		
		q.updateProgress(player, 1);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onProjectileThrownEvent(ProjectileLaunchEvent event)
	{
		if(event.getEntity() instanceof Snowball)
		{
			Snowball snowball = (Snowball)event.getEntity();
			if(snowball.getShooter() instanceof Player)
			{
				Player playerThrower = (Player)snowball.getShooter();
				
				if(!playerThrower.hasPermission("quests.quests"))
					return;
				
				BukkitPlayer player = Quests.getInstance().getBukkitPlayer(playerThrower);
				
				if(player.activeQuest.getQuestType() != this.getQuestType())
					return;
				
				QuestThrow q = (QuestThrow)player.activeQuest;
				
				if(!(q.data.Type.equals(Material.SNOW_BALL)))
					return;
										
				if(!q.checkForValidModifiers(playerThrower))
					return;
				
				q.updateProgress(player, 1);
			}
		}
	}
}