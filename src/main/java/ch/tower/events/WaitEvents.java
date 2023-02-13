package ch.tower.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WaitEvents implements Listener, StateEvents
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
            Player player = (Player) e.getEntity();
            player.setHealth(20);
        }
    }

    @EventHandler
    public void disableFoodLoss(FoodLevelChangeEvent e)
    {
        if(e.getEntity().getType() == EntityType.PLAYER)
        {
            e.setCancelled(true);
            Player player = (Player) e.getEntity();
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void disableBreakingBlock(BlockBreakEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler
    public void disablePlacingBlock(BlockPlaceEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler
    public void playerQuitGame(PlayerQuitEvent e)
    {
        e.getPlayer().getInventory().clear();
    }

    @Override
    public void onStateBegin()
    {

    }

    @Override
    public void onStateLeave()
    {

    }
}
