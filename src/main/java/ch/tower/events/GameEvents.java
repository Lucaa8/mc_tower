package ch.tower.events;

import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.managers.TeamsManager;
import ch.tower.utils.NPC.NPCLoader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;

public class GameEvents implements StateEvents
{
    private static GameEvents instance = null;
    private GameEvents(){}

    public static synchronized GameEvents getInstance()
    {
        if(instance == null)
        {
            instance = new GameEvents();
        }
        return instance;
    }
    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent e)
    {
        //TODO: tester
        Player player = e.getEntity();
        EntityDamageEvent.DamageCause deathCause = player.getLastDamageCause().getCause();
        if (deathCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        {
            if(player.getLastDamageCause().getEntity() instanceof Player)
            {
                Player attacker = (Player) player.getLastDamageCause().getEntity();

                TowerPlayer towerPlayer = TowerPlayer.getPlayer(player);
                TowerPlayer towerAttacker = TowerPlayer.getPlayer(attacker);
                towerPlayer.addDeath();
                towerAttacker.addKill();
                String message = TeamsManager.getPlayerTeam(player).getColorCode()
                        + player.getName() + ChatColor.RESET + " has been killed by " + TeamsManager.getPlayerTeam(attacker).getColorCode() + player.getName();
                e.setDeathMessage(message);
            }
        }
        if (deathCause == EntityDamageEvent.DamageCause.VOID)
        {
            String message = TeamsManager.getPlayerTeam(player).getColorCode()
                    + player.getName() + ChatColor.RESET + " fell into the void.";
            TowerPlayer towerPlayer = TowerPlayer.getPlayer(player);
            towerPlayer.addDeath();
            e.setDeathMessage(message);
        }
    }
    @EventHandler
    public void onChatByPlayer(AsyncPlayerChatEvent e)
    {
        e.setCancelled(true);
        StringBuilder s = new StringBuilder("");
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam t = TeamsManager.getPlayerTeam(p);
        if (t != null)
        {
            s.append(t.getColorCode());
            s.append("[");
            s.append(t.name());
            s.append("] ");
            s.append(p.getDisplayName());
            s.append(ChatColor.RESET);
        } else
        {
            s.append(p.getDisplayName());
        }

        if (e.getMessage().startsWith("!") && e.getMessage().length() != 1 && t != TeamsManager.PlayerTeam.SPECTATOR)
        {
            s.append(" (Global)");
            s.append(" : ");
            s.append(e.getMessage().substring(1));
            Bukkit.broadcast(s.toString(), Server.BROADCAST_CHANNEL_USERS);
        }
        else
        {
            TeamsManager.PlayerTeam team = TeamsManager.getPlayerTeam(e.getPlayer());
            s.append(" : ");
            s.append(e.getMessage());
            for(Player playerOfTeam: team.getInfo().getPlayers())
            {
                playerOfTeam.sendMessage(s.toString());
            }
        }


    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        //TODO: Tester pour spectateur et team rouge
        Player p = e.getPlayer();
        if(TowerPlayer.getPlayer(p) == null)
        {
            TeamsManager.PlayerTeam.SPECTATOR.addPlayer(p);
            e.setJoinMessage("");
        }
        else
        {
            String message = TeamsManager.getPlayerTeam(p).getColorCode() + p.getName() + ChatColor.RESET + " joined the game.";
            e.setJoinMessage(message);
        }
        p.teleport(TeamsManager.getPlayerTeam(p).getSpawn());
    }

    @EventHandler void onQuit(PlayerQuitEvent e)
    {
        //TODO: tester
        Player p = e.getPlayer();
        if(TowerPlayer.getPlayer(p) == null)
        {
            e.setQuitMessage("");
        }
        else
        {
            String message = TeamsManager.getPlayerTeam(p).getColorCode() + p.getName() + ChatColor.RESET + " leaved the game.";
            e.setQuitMessage(message);
        }
    }

    @Override
    public void onStateBegin()
    {
        TowerPlayer.registerPlayers();
        Bukkit.broadcast("The game begin. GL HF", Server.BROADCAST_CHANNEL_USERS);
        //TODO: tester
        Collection<? extends Player> players = Main.getInstance().getServer().getOnlinePlayers();
        for (Player player : players)
        {
            player.teleport(TeamsManager.getPlayerTeam(player).getSpawn());
        }
        NPCLoader.load();
    }

    @Override
    public void onStateLeave()
    {

    }
}
