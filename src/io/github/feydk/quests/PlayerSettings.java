package io.github.feydk.quests;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlayerSettings
{
	public boolean showProgressbar;
	public boolean useSounds;
	public boolean showOthersCompletions;
	public boolean useReminders;
	
	public static PlayerSettings getDefault()
	{
		PlayerSettings obj = new PlayerSettings();
		obj.showProgressbar = true;
		obj.useSounds = true;
		obj.showOthersCompletions = true;
		obj.useReminders = true;
		
		return obj;
	}
	
	public static String getDefaultJson()
	{
		PlayerSettings obj = PlayerSettings.getDefault();
		
		return obj.toJson();
	}
	
	public String toJson()
	{
		Map<String, Object> arr = new HashMap<String, Object>();
		arr.put("progressbar", showProgressbar);
		arr.put("sounds", useSounds);
		arr.put("completions", showOthersCompletions);
		arr.put("reminders", useReminders);
		
		return JSONValue.toJSONString(arr);
	}
	
	@SuppressWarnings("unchecked")
	public static PlayerSettings fromJson(String json)
	{
		try
		{
			Object obj = new JSONParser().parse(json);
			Map<String, Object> map = (Map<String, Object>)obj;
			
			PlayerSettings settings = new PlayerSettings();
			settings.showProgressbar = Boolean.parseBoolean(map.get("progressbar").toString());
			settings.useSounds = Boolean.parseBoolean(map.get("sounds").toString());
			settings.showOthersCompletions = Boolean.parseBoolean(map.get("completions").toString());
			settings.useReminders = Boolean.parseBoolean(map.get("reminders").toString());
			
			return settings;
			
		}
		catch(ParseException e)
		{
			e.printStackTrace();
		}
		
		return PlayerSettings.getDefault();
	}
}