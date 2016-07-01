package io.github.feydk.quests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlayerModel
{
	public UUID uuid;
	public String name;
	public String quest_active;
	public String quest_choices;
	public String settings;
	public long timestamp_begin;
	public long timestamp_created;
	
	private boolean populate(ResultSet rs)
	{
		try
		{
			uuid = java.util.UUID.fromString(rs.getString("uuid"));
			name = rs.getString("name");
			quest_active = rs.getString("quest_active");
			quest_choices = rs.getString("quest_choices");
			settings = rs.getString("settings");
			timestamp_created = rs.getTimestamp("timestamp_created").getTime();

			if(rs.getTimestamp("timestamp_begin") != null)
				timestamp_begin = rs.getTimestamp("timestamp_begin").getTime();
			
			return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	static int getIndex(UUID uuid)
	{
		String query = "select count(id) from history where player_uuid = ? and date(timestamp_created) = date(now())";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		
		return QuestsPlugin.getInstance().db.getInt(query, params) + 1;
	}
	
	static void create(UUID uuid, String name, String settings)
	{
		String query = "insert into players (uuid, name, quest_active, quest_choices, settings, timestamp_begin, timestamp_created) values(?, ?, '', '', ?, NULL, now())";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		params.put(2, name);
		params.put(3, settings);
		
		QuestsPlugin.getInstance().db.insert(query, params);
	}
	
	public static PlayerModel loadByUuid(UUID uuid)
	{
		String query = "select * from players where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		try
		{
			if(rs != null && rs.next())
			{
				PlayerModel obj = new PlayerModel();
				obj.populate(rs);
				
				return obj;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*public static PlayerSettings getSettings(UUID uuid)
	{
		String query = "select settings from players where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		try
		{
			if(rs != null && rs.next())
			{
				String json = rs.getString("settings");
				
				return PlayerSettings.fromJson(json);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}*/
	
	static void updateName(UUID uuid, String name)
	{
		String query = "update players set name = ? where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, name);
		params.put(2, uuid.toString());
				
		QuestsPlugin.getInstance().db.update(query, params);
	}
	
	static void updateSettings(UUID uuid, PlayerSettings settings)
	{
		String query = "update players set settings = ? where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, settings.toJson());
		params.put(2, uuid.toString());
				
		QuestsPlugin.getInstance().db.update(query, params);
	}
	
	static void updateQuest(UUID uuid, Quest quest, long timestamp, long created)
	{
		String ts = Long.toString(timestamp);
		
		if(ts.length() > 10)
			ts = ts.substring(0, 10);
		
		String ts2 = Long.toString(created);
		
		if(ts2.length() > 10)
			ts2 = ts2.substring(0, 10);
		
		String query = "update players set quest_active = ?, timestamp_begin = FROM_UNIXTIME(" + ts + "), timestamp_created = FROM_UNIXTIME(" + ts2 + ") where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, quest.toJson());
		params.put(2, uuid.toString());
		
		QuestsPlugin.getInstance().db.update(query, params);
	}
	
	static void updateChoices(UUID uuid, Quest quest1, Quest quest2, Quest quest3)
	{
		String json = "[" + quest1.toJson() + ", ";
		json += quest2.toJson() + ", ";
		json += quest3.toJson() + "]";
		
		String query = "update players set quest_choices = ? where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, json);
		params.put(2, uuid.toString());
		
		QuestsPlugin.getInstance().db.update(query, params);
	}
	
	static List<Quest> getChoices(UUID uuid)
	{
		String query = "select quest_choices from players where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		String json = "";
		
		List<Quest> list = new ArrayList<Quest>();
		
		try
		{
			if(rs != null && rs.next())
			{
				json = rs.getString("quest_choices");
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		if(!json.isEmpty())
		{
			try
			{
				Object obj = new JSONParser().parse(json);
				JSONArray arr = (JSONArray)obj;
				
				list.add(Quests.getInstance().fromJson(arr.get(0).toString()));
				list.add(Quests.getInstance().fromJson(arr.get(1).toString()));
				list.add(Quests.getInstance().fromJson(arr.get(2).toString()));
			}
			catch(ParseException e)
			{
				e.printStackTrace();
			}
		}
		
		return list;
	}
	
	static Quest getChoice(UUID uuid, int idx)
	{
		String query = "select quest_choices from players where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		String json = "";
		
		try
		{
			if(rs != null && rs.next())
			{
				json = rs.getString("quest_choices");
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		if(!json.isEmpty())
		{
			try
			{
				Object obj = new JSONParser().parse(json);
				JSONArray arr = (JSONArray)obj;
				
				return Quests.getInstance().fromJson(arr.get(idx - 1).toString());
			}
			catch(ParseException e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	static void updateTimestamp(UUID uuid, long timestamp)
	{
		String ts = Long.toString(timestamp);
		
		if(ts.length() > 10)
			ts = ts.substring(0, 10);
		
		String query = "update players set timestamp_begin = FROM_UNIXTIME(" + ts + ") where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, uuid.toString());
		
		QuestsPlugin.getInstance().db.update(query, params);
	}
	
	static void updateProgress(UUID uuid, Quest quest)
	{
		String query = "update players set quest_active = ? where uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, quest.toJson());
		params.put(2, uuid.toString());
				
		QuestsPlugin.getInstance().db.update(query, params);
	}
}