package io.github.feydk.quests;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

public class QuestsRewards
{
	private double max;
	private double min;
	private double decrease;
	private Map<Integer, RewardEntry> triggers;
	
	@SuppressWarnings("unchecked")
	public QuestsRewards()
	{
		FileConfiguration config = Quests.getInstance().getPlugin().getConfig();
		
		min = config.getDouble("rewards.min");
		max = config.getDouble("rewards.max");
		decrease = config.getDouble("rewards.decrease");
		triggers = new HashMap<Integer, RewardEntry>();
		
		for(Object entry : config.getConfigurationSection("rewards").getList("triggers"))
		{
			LinkedHashMap<Integer, Object> map = (LinkedHashMap<Integer, Object>)entry;
						
			for(Entry<Integer, Object> set : map.entrySet())
			{
				LinkedHashMap<String, Object> values = (LinkedHashMap<String, Object>)set.getValue();
			
				RewardEntry re = new RewardEntry();
				re.Command = values.get("command").toString();
				re.Text = values.get("text").toString();

				triggers.put(set.getKey(), re);
			}
		}
	}
	
	public class RewardEntry
	{
		public String Command;
		public String Text;
	}
	
	public double getReward(int index)
	{
		double one = max / 100;
		double reward = max - ((index - 1) * (one * decrease));
		
		if(reward < min)
			reward = min;
		
		return reward;
	}
	
	public RewardEntry getTriggerReward(int index)
	{
		if(triggers.containsKey(index))
			return triggers.get(index);
		
		return null;
	}
	
	public boolean minimumMet(double amount)
	{
		return amount <= min;
	}
}