package ch.tower.events;

import ch.luca008.SpigotApi.Item.ItemBuilder;
import ch.tower.Main;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.ScoreboardManager.PlaceholderHelper;
import ch.tower.managers.TeamsManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
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

    private final ItemStack bw = new ItemBuilder()
            .setMaterial(Material.BLUE_WOOL)
            .setName(ChatColor.BLUE + "Join BLUE Team")
            .setGlowing(true)
            .createItem().toItemStack(1);

    private final ItemStack rw = new ItemBuilder()
            .setMaterial(Material.RED_WOOL)
            .setName(ChatColor.RED + "Join RED Team")
            .setGlowing(true)
            .createItem().toItemStack(1);

    private final ItemStack ww = new ItemBuilder()
            .setMaterial(Material.WHITE_WOOL)
            .setName(ChatColor.RED + "Leave your team")
            .createItem().toItemStack(1);

    public void checkAndStartCountdown()
    {
        if (Main.getInstance().getServer().getOnlinePlayers().size() == GameManager.ConfigField.MIN_PLAYERS.get() && countdownTask == null)
        {
            countdownTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::displayCountdownThenStart, 1L, 20L);
            Bukkit.broadcast("Game is starting soon", Server.BROADCAST_CHANNEL_USERS);
            for (Player player : Bukkit.getOnlinePlayers())
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
                ScoreboardManager.BoardField.TIMER.update(p, String.valueOf(countdown));
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
                ScoreboardManager.BoardField.TIMER.update(player, String.valueOf(countdown));
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
            p.kickPlayer("The game is already full.");
            return;
        }

        p.getInventory().clear();

        Location lobby = TeamsManager.PlayerTeam.SPECTATOR.getSpawn();
        p.teleport(lobby);

        p.getInventory().addItem(bw);
        p.getInventory().addItem(rw);
        p.getInventory().setItem(8, ww);

        e.setJoinMessage(p.getDisplayName() + " joined the game! (" + playersCount + "/" + maxPlayersCount +" players, " + GameManager.ConfigField.MIN_PLAYERS.get() + " needed to begin) ");
        checkAndStartCountdown();
        p.setLevel(countdown);
        p.setGameMode(GameMode.ADVENTURE);

        ScoreboardManager.BoardField.TEAM.update(p, PlaceholderHelper.getTeamName(null));
        updatePlayersOnlineBoard();
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
        e.setQuitMessage(p.getDisplayName() + " left the game! (" + (Main.getInstance().getServer().getOnlinePlayers().size()-1) + "/" + maxPlayersCount +" players)");
        updatePlayersOnlineBoard();
    }

    @EventHandler
    public void joinTeamByInteract(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();
        ItemStack i = e.getItem();
        TeamsManager.PlayerTeam team = null;
        if(i == null)
        {
            return;
        }
        switch (i.getType())
        {
            case BLUE_WOOL -> team = TeamsManager.PlayerTeam.BLUE;
            case RED_WOOL -> team = TeamsManager.PlayerTeam.RED;
        }

        TeamsManager.PlayerTeam current = TeamsManager.getPlayerTeam(p);
        if(team == null && current != null)
        {
            current.removePlayer(p);
            Bukkit.broadcast(p.getDisplayName() + " left the " + current.getColorCode() + current.getInfo().apiTeam().getDisplayName() + ChatColor.RESET + " team.", Server.BROADCAST_CHANNEL_USERS);
            p.teleport(TeamsManager.PlayerTeam.SPECTATOR.getSpawn());
        }

        if(team != null && team != current)
        {
            //TODO add check if team is alrady full (basically if team.getCount() >= MAX_PLAYERS/2)
            team.addPlayer(p);
            String name = team.getInfo().apiTeam().getDisplayName();
            p.sendTitle(team.getColorCode() + "You joined the " + name + " team", "", 10, 20, 10);
            p.teleport(team.getSpawn());
            Bukkit.broadcast(p.getDisplayName() + " joined the " + team.getColorCode() + name + ChatColor.RESET + " team.", Server.BROADCAST_CHANNEL_USERS);
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
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setExhaustion(0.0f);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
        }
    }

    private void updatePlayersOnlineBoard()
    {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()-> Bukkit.getOnlinePlayers().forEach(p->ScoreboardManager.BoardField.PLAYER_COUNT.update(p, PlaceholderHelper.getTotalPlayerCount())), 5L);
    }
}
