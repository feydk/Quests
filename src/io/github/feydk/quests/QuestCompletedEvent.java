package io.github.feydk.quests;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QuestCompletedEvent extends Event implements Cancellable
{
	private boolean cancelled;
	private static HandlerList handlers = new HandlerList();
	
	public QuestCompletedEvent(Player player, int index)
	{
		this.player = player;
		this.index = index;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	 public static HandlerList getHandlerList()
	 {
		 return handlers;
	 }
	 
	 final Player player;
	 final int index;
	 
	 public static boolean call(Player player, int index)
	 {
		 QuestCompletedEvent event = new QuestCompletedEvent(player, index);
		 Bukkit.getServer().getPluginManager().callEvent(event);
		 
		 return (!event.isCancelled());
	 }

         public Player getPlayer() { return player; }
         public int getIndex() { return index; }
}
