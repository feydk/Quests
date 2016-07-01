package io.github.feydk.quests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HistoryModel
{
	public int id;
	public UUID player_uuid;
	public String quest;
	public String quest_type;
	public long timestamp_begin;
	public long timestamp_end;
	public long timestamp_created;
	public int status;
	public double reward;
	
	private boolean populate(ResultSet rs)
	{
		try
		{
			id = rs.getInt("id");
			player_uuid = java.util.UUID.fromString(rs.getString("player_uuid"));
			quest = rs.getString("quest");
			quest_type = rs.getString("quest_type");
			timestamp_begin = rs.getTimestamp("timestamp_begin").getTime();
			timestamp_end = rs.getTimestamp("timestamp_end").getTime();
			timestamp_created = rs.getTimestamp("timestamp_created").getTime();
			status = rs.getInt("status");
			reward = rs.getDouble("reward");
			
			return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static int getCount(UUID player_uuid, int status)
	{
		String query = "select count(id) from history where player_uuid = ? and status = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, player_uuid.toString());
		params.put(2, status);
		
		return QuestsPlugin.getInstance().db.getInt(query, params);
	}
	
	public static double getRewards(UUID player_uuid)
	{
		String query = "select sum(reward) from history where player_uuid = ? and status = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, player_uuid.toString());
		params.put(2, QuestStatus.COMPLETED.ordinal());
		
		return QuestsPlugin.getInstance().db.getDouble(query, params);
	}
	
	public static Map<String, Integer> getDistribution(UUID player_uuid)
	{
		String query = "select count(id) as amount, quest_type from history where player_uuid = ? and status = ? group by quest_type order by amount desc";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, player_uuid.toString());
		params.put(2, QuestStatus.COMPLETED.ordinal());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		Map<String, Integer> list = new LinkedHashMap<String, Integer>();
		
		try
		{
			if(rs != null)
			{
				while(rs.next())
				{
					list.put(rs.getString("quest_type"), rs.getInt("amount"));
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return list;
	}
	
	public static Map<String, Integer> getHighscore()
	{
		String query = "select count(id) as completed, (select name from players where uuid = history.player_uuid) as name from history where status = ? group by name order by completed desc limit 0, 10";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, QuestStatus.COMPLETED.ordinal());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		Map<String, Integer> list = new LinkedHashMap<String, Integer>();
		
		try
		{
			if(rs != null)
			{
				while(rs.next())
				{
					list.put(rs.getString("name"), rs.getInt("completed"));
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return list;
	}
	
	public static HistoryModel create(UUID player_uuid, String quest, int status, double reward, String quest_type, long timestamp_begin, long timestamp_created)
	{
		String ts = Long.toString(timestamp_begin);
		
		if(ts.length() > 10)
			ts = ts.substring(0, 10);
		
		String ts2 = Long.toString(timestamp_created);
		
		if(ts2.length() > 10)
			ts2 = ts2.substring(0, 10);
		
		String query = "insert into history (player_uuid, quest, quest_type, status, reward, timestamp_begin, timestamp_end, timestamp_created) values(?, ?, ?, ?, ?, FROM_UNIXTIME(" + ts + "), now(), FROM_UNIXTIME(" + ts2 + "))";

		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, player_uuid.toString());
		params.put(2, quest);
		params.put(3, quest_type);
		params.put(4, status);
		params.put(5, reward);
		
		int id = QuestsPlugin.getInstance().db.insert(query, params);
		
		return HistoryModel.loadById(id);
	}
	
	public static HistoryModel loadById(int id)
	{
		String query = "select * from history where id = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, id);
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		try
		{
			if(rs != null && rs.next())
			{
				HistoryModel obj = new HistoryModel();
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
	
	public static List<HistoryModel> loadByPlayerUuid(UUID player_uuid)
	{
		String query = "select * from history where player_uuid = ?";
		
		HashMap<Integer, Object> params = new HashMap<Integer, Object>();
		params.put(1, player_uuid.toString());
		
		ResultSet rs = QuestsPlugin.getInstance().db.select(query, params);
		
		List<HistoryModel> list = new ArrayList<HistoryModel>();
		
		try
		{
			if(rs != null)
			{
				while(rs.next())
				{
					HistoryModel obj = new HistoryModel();
					obj.populate(rs);
				
					list.add(obj);
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return list;
	}
}