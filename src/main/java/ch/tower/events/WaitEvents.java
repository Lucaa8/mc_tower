package ch.tower.events;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WaitEvents implements Listener
{
    private static WaitEvents instance = null;
    private WaitEvents(){}

    public static synchronized WaitEvents getInstance()
    {
        if(instance == null)
        {
            instance = new WaitEvents();
        }
        return instance;
    }
    @EventHandler
    public void disableDamageOnPlayers(EntityDamageEvent e)
    {
        if(e.getEntity().getType() == EntityType.PLAYER)
        {
            e.setCancelled(true);
        }
    }
}
