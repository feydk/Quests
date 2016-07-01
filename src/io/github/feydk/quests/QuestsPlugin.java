package io.github.feydk.quests;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestsPlugin extends JavaPlugin implements Listener
{
	private static QuestsPlugin instance;
	Economy economy;
	MySQLDatabase db;
	final Quests quests = new Quests();
	final QuestCommand questCommand = new QuestCommand();
	final QuestAdminCommand questAdminCommand = new QuestAdminCommand();
	
	public QuestsPlugin()
	{
		instance = this;
	}
	
	public static QuestsPlugin getInstance()
	{
		return instance;
	}
	
	@Override
    public void onEnable()
    {
        reloadConfig();
        
        // Economy
        if(!setupEconomy())
        {
        	getLogger().warning("Economy setup failed. Disabling Quests.");
        	getServer().getPluginManager().disablePlugin(this);
        	return;
        }
        
        // Database
        if(!setupDatabase())
        {
        	getLogger().warning("Database setup failed. Disabling Quests.");
        	getServer().getPluginManager().disablePlugin(this);
        	return;
        }
        
        quests.configure();
        
        for(Quest quest : quests.getQuests())
        {
            if(quest instanceof Listener)
            {
                getServer().getPluginManager().registerEvents((Listener)quest, this);
            }
        }
        
        // Commands
        getCommand("questsadmin").setExecutor(questAdminCommand);
        getCommand("quest").setExecutor(questCommand);
        
        // Events
        getServer().getPluginManager().registerEvents(this, this);
        
        for(Player player : getServer().getOnlinePlayers())
        {
        	BukkitPlayer p = new BukkitPlayer(player.getUniqueId());
    		p.init();
    		
    		quests.players.put(p.uuid, p);
        }
    }
	
	@Override
    public void onDisable()
    {
		for(Quest quest : quests.getQuests())
        {
            quest.onDisable();
        }
		
		for(BukkitPlayer p : quests.players.values())
			p.bossbar.removePlayer(p.getPlayer());
    }
	
	@EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        quests.players.remove(event.getPlayer().getUniqueId());
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		final BukkitPlayer player = new BukkitPlayer(event.getPlayer().getUniqueId());
		player.init();
		
		quests.players.put(player.uuid, player);
		
		if(player.getSettings().useReminders)
		{
			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
			{
				public void run()
				{
					QuestUI.displayReminder(player);
				}
			}, 160);
		}
	}
	
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        
        if(economyProvider != null)
        	economy = economyProvider.getProvider();
        
        return (economy != null);
    }
	
	private boolean setupDatabase()
    {
		FileConfiguration config = getConfig();
		
		db = new MySQLDatabase(config.getString("mysql.host"), config.getString("mysql.port"), config.getString("mysql.user"), config.getString("mysql.password"), config.getString("mysql.database"));
		
		db.install();
		
        return true;
    }
}