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
import java.util.stream.Collectors;

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
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_GAME_STARTING"));
            String title = GameManager.getMessage("MSG_WAIT_TITLE_STARTING", String.valueOf(countdown));
            for (Player player : Bukkit.getOnlinePlayers())
            {
                player.sendTitle(title, "", 5, 20, 5);
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
                //Resets the player bar xp and scoreboard with the max count down
                p.setLevel(countdown);
                ScoreboardManager.BoardField.TIMER.update(p, String.valueOf(countdown));
            });
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_GAME_ABORT"));
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

    private void balanceTeams(List<Player> redPlayers, List<Player> bluePlayers, List<Player> noTeamPlayers) {
        int maxPerTeam = maxPlayersCount / 2;

        String blue = TeamsManager.PlayerTeam.BLUE.getColorCode() + TeamsManager.PlayerTeam.BLUE.getInfo().apiTeam().getDisplayName();
        String red = TeamsManager.PlayerTeam.RED.getColorCode() + TeamsManager.PlayerTeam.RED.getInfo().apiTeam().getDisplayName();

        while (Math.abs(redPlayers.size() - bluePlayers.size()) > 1) {
            if (redPlayers.size() > bluePlayers.size() && bluePlayers.size() < maxPerTeam) {
                Player p = redPlayers.remove(redPlayers.size() - 1);
                bluePlayers.add(p); //just to keep track of actual team sizes
                TeamsManager.PlayerTeam.BLUE.addPlayer(p); //but this is this adding that makes the difference
                Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_TEAM_CHANGE", p.getName(), red, blue));
            } else if (bluePlayers.size() > redPlayers.size() && redPlayers.size() < maxPerTeam) {
                Player p = bluePlayers.remove(bluePlayers.size() - 1);
                redPlayers.add(p); //same as blue
                TeamsManager.PlayerTeam.RED.addPlayer(p);
                Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_TEAM_CHANGE", p.getName(), blue, red));
            } else {
                break; //security but cannot be reached in normal cases
            }
        }

        // add remaining no-team-players to teams
        for (Player player : noTeamPlayers) {
            if (redPlayers.size() < maxPerTeam || bluePlayers.size() < maxPerTeam) {
                if (redPlayers.size() <= bluePlayers.size()) {
                    redPlayers.add(player);
                    TeamsManager.PlayerTeam.RED.addPlayer(player);
                    Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_TEAM_FORCE_JOIN", player.getName(), red));
                } else {
                    bluePlayers.add(player);
                    TeamsManager.PlayerTeam.BLUE.addPlayer(player);
                    Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_TEAM_FORCE_JOIN", player.getName(), blue));
                }
            } else {
                break; // the two teams are full, not gonna append in normal cases because the server wont accept a player count greater than maxPlayersCount
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
        e.setCancelled(true);
        String message = "";
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam t = TeamsManager.getPlayerTeam(p);
        if(t != null)
        {
            message += t.getColorCode() + "[" + t.getInfo().apiTeam().getDisplayName() + "] " + p.getName() + "§r";
        }
        else
        {
            message += p.getName();
        }
        message += " : " + e.getMessage();
        Bukkit.broadcastMessage(message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        int playersCount = Main.getInstance().getServer().getOnlinePlayers().size();
        Player p = e.getPlayer();
        if(playersCount >= maxPlayersCount)
        {
            p.kickPlayer(GameManager.getMessage("MSG_WAIT_GAME_FULL"));
            return;
        }

        p.getInventory().clear();

        Location lobby = TeamsManager.PlayerTeam.SPECTATOR.getSpawn();
        p.teleport(lobby);

        p.getInventory().addItem(bw);
        p.getInventory().addItem(rw);
        p.getInventory().setItem(8, ww);

        e.setJoinMessage(GameManager.getMessage("MSG_WAIT_JOIN", p.getName(), String.valueOf(playersCount), String.valueOf(maxPlayersCount), String.valueOf(GameManager.ConfigField.MIN_PLAYERS.get())));
        checkAndStartCountdown();
        p.setLevel(countdown);
        p.setGameMode(GameMode.ADVENTURE);
        p.getActivePotionEffects().forEach(effect->p.removePotionEffect(effect.getType()));
        p.setHealth(20);
        p.setFoodLevel(20);

        ScoreboardManager.BoardField.TEAM.update(p, PlaceholderHelper.getTeamName(null));
        updatePlayersOnlineBoard();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam pt = TeamsManager.getPlayerTeam(p);
        String color = "§f";
        if(pt != null)
        {
            pt.removePlayer(p);
            color = pt.getColorCode();
        }
        checkAndStopCountdown();
        e.setQuitMessage(GameManager.getMessage("MSG_WAIT_QUIT", color+p.getName(), String.valueOf((Main.getInstance().getServer().getOnlinePlayers().size()-1)), String.valueOf(maxPlayersCount)));
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
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_TEAM_LEFT", p.getName(), current.getColorCode() + current.getInfo().apiTeam().getDisplayName()));
            p.teleport(TeamsManager.PlayerTeam.SPECTATOR.getSpawn());
        }

        if(team != null && team != current)
        {
            if(team.getInfo().getPlayers().size() >= maxPlayersCount/2)
            {
                p.sendMessage(GameManager.getMessage("MSG_WAIT_TEAM_FULL"));
                return;
            }
            team.addPlayer(p);
            String name = team.getInfo().apiTeam().getDisplayName();
            p.teleport(team.getSpawn());
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_WAIT_TEAM_JOIN", p.getName(), team.getColorCode() + name));
        }
    }

    @Override
    public void onStateBegin()
    {
        Bukkit.getConsoleSender().sendMessage("§aThe server is prepared to host players.");
    }

    @Override
    public void onStateLeave()
    {
        List<Player> redPlayers = new ArrayList<>(TeamsManager.PlayerTeam.RED.getInfo().getPlayers());
        List<Player> bluePlayers = new ArrayList<>(TeamsManager.PlayerTeam.BLUE.getInfo().getPlayers());
        List<Player> noTeamPlayers = Bukkit.getOnlinePlayers().stream()
                .filter(p->!(redPlayers.contains(p)||bluePlayers.contains(p)))
                .collect(Collectors.toCollection(ArrayList::new));

        if(redPlayers.size() != bluePlayers.size() || noTeamPlayers.size() > 0) //on the case of 3 red and 4 blues, we'll enter this condition but the while loop of balanceTeams wont be true so we'll quit the function
        {
            balanceTeams(redPlayers, bluePlayers, noTeamPlayers);
        }

        for (Player player : Bukkit.getOnlinePlayers())
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
