package ch.tower.events;

import ch.tower.Main;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.TeamsManager;
import ch.tower.utils.Scoreboard.PlayerBoard;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WaitEvents implements StateEvents
{
    private static WaitEvents instance = null;

    private int countdown = GameManager.ConfigField.TIMER_DURATION_WAIT.get();

    private BukkitTask countdownTask;

    private final int maxPlayersCount = GameManager.ConfigField.MAX_PLAYERS.get();

    private WaitEvents(){}

    public static synchronized WaitEvents getInstance()
    {
        if (instance == null)
        {
            instance = new WaitEvents();
        }
        return instance;
    }

    public void checkAndStartCountdown()
    {
        if (Main.getInstance().getServer().getOnlinePlayers().size() == GameManager.ConfigField.MIN_PLAYERS.get() && countdownTask == null)
        {
            countdownTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::displayCountdownThenStart, 1L, 20L);
            Bukkit.broadcast("Game is starting soon", Server.BROADCAST_CHANNEL_USERS);
            Collection<? extends Player> players = Main.getInstance().getServer().getOnlinePlayers();
            for (Player player : players)
            {
                player.sendTitle("Starting in " + countdown + " seconds", "", 5, 20, 5);
            }
        }
    }

    public void checkAndStopCountdown()
    {
        if (Main.getInstance().getServer().getOnlinePlayers().size() - 1 < GameManager.ConfigField.MIN_PLAYERS.get() && countdownTask != null)
        {
            countdownTask.cancel();
            countdownTask = null;
            countdown = GameManager.ConfigField.TIMER_DURATION_WAIT.get();
            Bukkit.getOnlinePlayers().forEach(p->
            {
                p.setLevel(countdown);
                ScoreboardManager.BoardField.TIMER.update(p, countdown);
            }); //Resets the player bar xp and scoreboard with the max count down
            Bukkit.broadcast("Game start is cancelled, a player left.", Server.BROADCAST_CHANNEL_USERS);
        }
    }

    public void displayCountdownThenStart()
    {
        Collection<? extends Player> players = Main.getInstance().getServer().getOnlinePlayers();
        if (countdown > 0)
        {
            for (Player player : players)
            {
                if(countdown < 4)
                {
                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1,1);
                }
                player.setLevel(countdown);
                ScoreboardManager.BoardField.TIMER.update(player, countdown);
            }
            countdown--;
        }
        else
        {
            countdownTask.cancel();
            Main.getInstance().getManager().setState(GameManager.GameState.GAME);
        }
    }

    public void putPlayersInTeams()
    {
        //TODO: Test with multiple players
        Collection<? extends Player> listPlayers = Main.getInstance().getServer().getOnlinePlayers();
        int nbRed = 0;
        int nbBlue = 0;
        List<Player> playersWithoutTeam = new ArrayList<Player>(listPlayers);
        Iterator<Player> it = playersWithoutTeam.iterator();
        while (it.hasNext())
        {
            Player player = it.next();
            TeamsManager.PlayerTeam team = TeamsManager.getPlayerTeam(player);
            if(TeamsManager.getPlayerTeam(player) != null)
            {
                it.remove();
                if(team == TeamsManager.PlayerTeam.RED) nbRed++;
                if(team == TeamsManager.PlayerTeam.BLUE) nbBlue++;
            }
        }
        Random rand = new Random();
        while(playersWithoutTeam.size() > 0)
        {
            Player player = playersWithoutTeam.get(rand.nextInt(playersWithoutTeam.size()));
            playersWithoutTeam.remove(player);
            if(nbBlue - nbRed > 0)
            {
                TeamsManager.PlayerTeam.RED.addPlayer(player);
                nbRed++;
            }
            else
            {
                TeamsManager.PlayerTeam.BLUE.addPlayer(player);
                nbBlue++;
            }
        }
    }

    @EventHandler
    public void disableDamageOnPlayers(EntityDamageEvent e)
    {
        e.setCancelled(true);
        if (e.getEntity().getType() == EntityType.PLAYER)
        {
            Player player = (Player) e.getEntity();
            player.setHealth(20);
        }
    }

    @EventHandler
    public void disableFoodLoss(FoodLevelChangeEvent e)
    {
        if (e.getEntity().getType() == EntityType.PLAYER)
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
    public void disableExp(PlayerExpChangeEvent e)
    {
        e.setAmount(0);
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
        int playersCount = Main.getInstance().getServer().getOnlinePlayers().size();
        Player p = e.getPlayer();
        if(playersCount >= maxPlayersCount)
        {
            p.kickPlayer("The limit of player is already passed, sorry.");
            return;
        }
        p.getInventory().clear();
        Location lobby = TeamsManager.PlayerTeam.SPECTATOR.getSpawn();
        p.teleport(lobby);
        ItemStack bw = new ItemStack(Material.BLUE_WOOL,1);
        ItemMeta bwMeta = bw.getItemMeta();
        bwMeta.setDisplayName(ChatColor.BLUE + "Join BLUE Team");
        bwMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        bw.setItemMeta(bwMeta);
        bw.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);

        ItemStack rw = new ItemStack(Material.RED_WOOL,1);
        ItemMeta rwMeta = rw.getItemMeta();
        rwMeta.setDisplayName(ChatColor.RED + "Join RED Team");
        rwMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        rw.setItemMeta(rwMeta);
        rw.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);

        p.getInventory().addItem(bw);
        p.getInventory().addItem(rw);
        e.setJoinMessage(p.getDisplayName() + " joined the game! (" + playersCount + "/" + maxPlayersCount +" players, " + GameManager.ConfigField.MIN_PLAYERS.get() + " needed to begin) ");
        checkAndStartCountdown();
        p.setLevel(countdown);
        p.setGameMode(GameMode.ADVENTURE);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->
        {
            for(Player player : Bukkit.getOnlinePlayers())
            {
                if(player == p)
                {
                    ScoreboardManager.BoardField.TEAM.update(player, "§aNone");
                    ScoreboardManager.BoardField.TIMER.update(player, countdown);
                }
                updatePlayersOnlineBoard(playersCount);
            }
        }, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam pt = TeamsManager.getPlayerTeam(p);
        if(pt != null)
        {
            pt.removePlayer(p);
        }
        checkAndStopCountdown();
        int playersCount = Main.getInstance().getServer().getOnlinePlayers().size() - 1;
        e.setQuitMessage(p.getDisplayName() + " left the game! (" + playersCount + "/" + maxPlayersCount +" players)");
        updatePlayersOnlineBoard(playersCount);
    }

    @EventHandler
    public void joinTeamByInteract(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();
        ItemStack i = e.getItem();
        TeamsManager.PlayerTeam team;
        if(i == null)
        {
            return;
        }
        switch (i.getType())
        {
            case BLUE_WOOL -> team = TeamsManager.PlayerTeam.BLUE;
            case RED_WOOL -> team = TeamsManager.PlayerTeam.RED;
            default -> team = null;
        }

        if(team != null && team != TeamsManager.getPlayerTeam(p))
        {
            team.addPlayer(p);
            String name = team.getInfo().getName();
            p.sendTitle(team.getColorCode() + "You joined the " + name + " team", "", 10, 20, 10);
            Location lobby = team.getSpawn();
            p.teleport(lobby);
            Bukkit.broadcast(p.getDisplayName() + " joined the " + team.getColorCode() + name + ChatColor.RESET + " team.", Server.BROADCAST_CHANNEL_USERS);
            ScoreboardManager.BoardField.TEAM.update(p, team.getColorCode() + name);
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
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Collection<? extends Player> players = Main.getInstance().getServer().getOnlinePlayers();
        if(TeamsManager.PlayerTeam.BLUE.getInfo().getPlayers().size() == 0 || TeamsManager.PlayerTeam.RED.getInfo().getPlayers().size() == 0)
        {
            for (Player player : players)
            {
                TeamsManager.PlayerTeam playerTeam = TeamsManager.getPlayerTeam(player);
                if(playerTeam != null) playerTeam.removePlayer(player);
            }
            Bukkit.broadcast("Since everyone was on the same team, the teams were modified", Server.BROADCAST_CHANNEL_USERS);
        }
        putPlayersInTeams();
        for (Player player : players)
        {
            player.setLevel(0);
            player.setExp(0);
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    private void updatePlayersOnlineBoard(int playersCount)
    {
        for(Player player : Bukkit.getOnlinePlayers())
        {
            PlayerBoard pb = Main.getInstance().getManager().getScoreboardManager().getBoard(player);
            if(pb != null)
            {
                pb.updateLines(Map.of
                        (
                                ScoreboardManager.BoardField.MAX_PLAYER_COUNT.toFormat(), maxPlayersCount,
                                ScoreboardManager.BoardField.PLAYER_COUNT.toFormat(), playersCount
                        ));
            }
        }
    }
}
