package io.github.feydk.quests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class MySQLDatabase
{
	private Connection connection;
	private boolean log_statements = false;
	private String host;
	private String port;
	private String user;
	private String password;
	private String dbname;
	
	public MySQLDatabase(String host, String port, String user, String password, String dbname)
	{	
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.dbname = dbname;
		
		checkConnection();
	}
	
	public void Close()
	{
		try
		{
			if(connection != null && !connection.isClosed())
				connection.close();
		}
		catch(SQLException e)
		{}
	}
	
	private void checkConnection()
	{
		try
		{
			if(this.connection == null || !this.connection.isValid(5))
			{
				try
				{
					Class.forName("com.mysql.jdbc.Driver");

					this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbname, user, password);
				}
				catch(ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				catch(SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void install()
	{
		String query = "show tables like 'players'";
		
		ResultSet rs = this.select(query, null);
		
		boolean exists = false;
		
		try
		{
			if(rs != null && rs.next())
			{
				exists = true;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		if(!exists)
		{
			query = "CREATE TABLE `players` (`uuid` varchar(50) NOT NULL, `name` varchar(50) DEFAULT NULL, `quest_active` varchar(200) DEFAULT NULL, `quest_choices` varchar(500) DEFAULT NULL, `settings` varchar(200) DEFAULT NULL, `timestamp_begin` datetime DEFAULT '2000-01-01 00:00:00', `timestamp_created` datetime DEFAULT NULL, PRIMARY KEY (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
			this.execute(query, null);
		}
		
		query = "show tables like 'history'";
		
		rs = this.select(query, null);
		
		exists = false;
		
		try
		{
			if(rs != null && rs.next())
			{
				exists = true;
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		if(!exists)
		{
			query = "CREATE TABLE `history` (`id` int(11) NOT NULL AUTO_INCREMENT, `player_uuid` varchar(50) DEFAULT NULL, `quest` varchar(200) DEFAULT NULL, `quest_type` varchar(20) DEFAULT NULL, `timestamp_begin` datetime DEFAULT NULL, `timestamp_end` datetime DEFAULT NULL, `timestamp_created` datetime DEFAULT NULL, `status` int(11) DEFAULT NULL, `reward` double DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;";
			this.execute(query, null);
		}
	}
	
	public ResultSet select(String query, HashMap<Integer, Object> params)
	{
		checkConnection();
		
		PreparedStatement ps;
		
		try
		{
			ps = connection.prepareStatement(query);
			
			if(params != null)
			{
				for(Map.Entry<Integer, Object> param : params.entrySet())
				{
					if(param.getValue().getClass().equals(String.class))
						ps.setString(param.getKey(), param.getValue().toString());
					else if(param.getValue().getClass().equals(Integer.class))
						ps.setInt(param.getKey(), Integer.parseInt(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Double.class))
						ps.setDouble(param.getKey(), Double.parseDouble(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Long.class))
						ps.setLong(param.getKey(), Long.parseLong(param.getValue().toString()));
				}
			}
			
			if(log_statements)
				System.out.println("db.select() called: " + query);

			return ps.executeQuery();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public int getInt(String query, HashMap<Integer, Object> params)
	{
		checkConnection();
		
		ResultSet rs = select(query, params);
		
		try
		{
			if(rs != null && rs.next())
			{
				return rs.getInt(1);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public double getDouble(String query, HashMap<Integer, Object> params)
	{
		checkConnection();
		
		ResultSet rs = select(query, params);
		
		try
		{
			if(rs != null && rs.next())
			{
				return rs.getDouble(1);
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public int insert(String query, HashMap<Integer, Object> params)
	{
		checkConnection();
		
		PreparedStatement ps;
		
		try
		{
			ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			
			if(params != null)
			{
				for(Map.Entry<Integer, Object> param : params.entrySet())
				{
					if(param.getValue().getClass().equals(String.class))
						ps.setString(param.getKey(), param.getValue().toString());
					else if(param.getValue().getClass().equals(Integer.class))
						ps.setInt(param.getKey(), Integer.parseInt(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Double.class))
						ps.setDouble(param.getKey(), Double.parseDouble(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Long.class))
						ps.setLong(param.getKey(), Long.parseLong(param.getValue().toString()));
				}
			}
			
			if(log_statements)
				System.out.println("db.insert() called");
			
			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			
		    if(rs.next())
		    {
		    	int last_inserted_id = rs.getInt(1);
		    	
		    	return last_inserted_id;
		    }
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public boolean update(String query, HashMap<Integer, Object> params)
	{
		checkConnection();
		
		PreparedStatement ps;
		
		try
		{
			ps = connection.prepareStatement(query);
			
			if(params != null)
			{
				for(Map.Entry<Integer, Object> param : params.entrySet())
				{
					if(param.getValue().getClass().equals(String.class))
						ps.setString(param.getKey(), param.getValue().toString());
					else if(param.getValue().getClass().equals(Integer.class))
						ps.setInt(param.getKey(), Integer.parseInt(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Double.class))
						ps.setDouble(param.getKey(), Double.parseDouble(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Long.class))
						ps.setLong(param.getKey(), Long.parseLong(param.getValue().toString()));
				}
			}
			
			if(log_statements)
				System.out.println("db.update() called");
			
			ps.executeUpdate();
			
			return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean execute(String query, HashMap<Integer, Object> params)
	{
		checkConnection();
		
		PreparedStatement ps;
		
		try
		{
			ps = connection.prepareStatement(query);
			
			if(params != null)
			{
				for(Map.Entry<Integer, Object> param : params.entrySet())
				{
					if(param.getValue().getClass().equals(String.class))
						ps.setString(param.getKey(), param.getValue().toString());
					else if(param.getValue().getClass().equals(Integer.class))
						ps.setInt(param.getKey(), Integer.parseInt(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Double.class))
						ps.setDouble(param.getKey(), Double.parseDouble(param.getValue().toString()));
					else if(param.getValue().getClass().equals(Long.class))
						ps.setLong(param.getKey(), Long.parseLong(param.getValue().toString()));
				}
			}
			
			if(log_statements)
				System.out.println("db.execute() called");
			
			return ps.execute();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}