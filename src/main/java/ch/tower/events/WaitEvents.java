package ch.tower.events;

import ch.luca008.SpigotApi.Api.TeamAPI;
import ch.tower.Main;
import ch.tower.managers.TeamsManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public class WaitEvents implements StateEvents
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

    @Override
    public void onStateBegin()
    {
        Bukkit.broadcast("Waiting for the beginning of the game", Server.BROADCAST_CHANNEL_USERS);
    }

    @Override
    public void onStateLeave()
    {
        Collection<? extends Player> players = Main.getInstance().getServer().getOnlinePlayers();
        for (Player player : players)
        {
            player.sendTitle("The game is about to begin", "In a few seconds", 20*1, 20*6, 20*1);
        }
        try
        {
            Main.getInstance().getServer().wait(8000);
        }
        catch (Exception e)
        {
            Bukkit.broadcast("Problem with waiting", Server.BROADCAST_CHANNEL_USERS);
        }

        players = Main.getInstance().getServer().getOnlinePlayers();
        for (Player player : players)
        {
            player.getInventory().clear();
        }
    }
}
