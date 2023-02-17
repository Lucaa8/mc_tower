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
        e.setCancelled(true);
        if(e.getEntity().getType() == EntityType.PLAYER)
        {
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
    public void disableItemDrop(PlayerDropItemEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler
    public void onChatByPlayer(AsyncPlayerChatEvent e)
    {
        StringBuilder s = new StringBuilder("");
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam t = TeamsManager.getPlayerTeam(p);
        if(t != null)
        {
            s.append(t.getColorCode());
            s.append("[");
            s.append(t.name());
            s.append("] ");
            s.append(p.getDisplayName());
            s.append(ChatColor.RESET);
        }
        else
        {
            s.append(p.getDisplayName());
        }
        s.append(" : ");
        s.append(e.getMessage());
        e.setCancelled(true);
        Bukkit.broadcast(s.toString(), Server.BROADCAST_CHANNEL_USERS);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        p.getInventory().clear();
        Location lobby = TeamsManager.PlayerTeam.SPECTATOR.getSpawn();
        p.teleport(lobby);
        ItemStack bw = new ItemStack(Material.BLUE_WOOL,1);
        ItemMeta bwMeta = bw.getItemMeta();
        bwMeta.setDisplayName(ChatColor.BLUE + "Join BLUE Team");
        bw.setItemMeta(bwMeta);
        bw.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);

        ItemStack rw = new ItemStack(Material.RED_WOOL,1);
        ItemMeta rwMeta = rw.getItemMeta();
        rwMeta.setDisplayName(ChatColor.RED + "Join RED Team");
        rw.setItemMeta(rwMeta);
        rw.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);

        p.getInventory().addItem(bw);
        p.getInventory().addItem(rw);
        Bukkit.broadcast(p.getDisplayName() + " joined the game! (" + Main.getInstance().getServer().getOnlinePlayers().size() + "/" + Main.getInstance().getServer().getMaxPlayers() +" players)", Server.BROADCAST_CHANNEL_USERS);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();
        if(TeamsManager.getPlayerTeam(p) != null)
        {
            TeamsManager.getPlayerTeam(p).removePlayer(p);
        }
    }

    @EventHandler
    public void joinTeamByInteract(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();
        ItemStack i = e.getItem();
        TeamsManager.PlayerTeam team;
        switch (i.getType())
        {
            case BLUE_WOOL -> team = TeamsManager.PlayerTeam.BLUE;
            case RED_WOOL -> team = TeamsManager.PlayerTeam.RED;
            default -> team = null;
        }

        if(team != null && team != TeamsManager.getPlayerTeam(p))
        {
            team.addPlayer(p);
            p.sendTitle(team.getColorCode() + "You joined the " + team.name() + " team", "", 10, 20, 10);
            Location lobby = team.getSpawn();
            p.teleport(lobby);
            Bukkit.broadcast(p.getDisplayName() + " joined the " + team.getColorCode() + team.name() + ChatColor.RESET + " team.", Server.BROADCAST_CHANNEL_USERS);
        }
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
            player.sendTitle("The game is about to begin", "In a few seconds", 20 * 1, 20 * 6, 20 * 1);
        }
        try
        {
            Main.getInstance().getServer().wait(8000);
        } catch (Exception e)
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
