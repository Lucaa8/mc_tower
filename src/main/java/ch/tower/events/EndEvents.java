package ch.tower.events;

import ch.tower.Main;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.TeamsManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EndEvents implements StateEvents
{
    private static EndEvents instance = null;
    private EndEvents(){}

    public static synchronized EndEvents getInstance()
    {
        if(instance == null)
        {
            instance = new EndEvents();
        }
        return instance;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent e)
    {
        e.setKickMessage(GameManager.getMessage("MSG_END_LOGIN_DISALLOWED"));
        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        e.setQuitMessage(null);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler
    public void onMoveBelowZero(PlayerMoveEvent e)
    {
        if(e.getTo() == null || e.getTo().getY() > 0)
            return;
        //if a spectator player trigger this line, his gamemode will become creative idk why... but it is not a problem i guess
        e.setTo(TeamsManager.PlayerTeam.SPECTATOR.getSpawn());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e)
    {
        e.setCancelled(true);
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam team = TeamsManager.getPlayerTeam(p);
        Bukkit.broadcastMessage((team == null ? "§f" : team.getColorCode()) + p.getName() + "§f: " + e.getMessage());
    }

    @Override
    public void onStateBegin()
    {
        ScoreboardManager sb = Main.getInstance().getManager().getScoreboardManager();
        for(Player p : Bukkit.getOnlinePlayers())
        {
            sb.updateBoard(p);
            p.setGameMode(GameMode.CREATIVE);
        }
        int timer = GameManager.ConfigField.TIMER_DURATION_END.get();
        Bukkit.broadcastMessage(GameManager.getMessage("MSG_END_RESTART", String.valueOf(timer)));
        long started = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), ()->{
            int elapsedSec = (int)(System.currentTimeMillis() - started)/1000;
            Bukkit.getOnlinePlayers().forEach(p->ScoreboardManager.BoardField.TIMER.update(p, String.valueOf(timer-elapsedSec)));
            if(elapsedSec >= timer)
            {
                Main.getInstance().getManager().stop();
            }
        }, 20L, 20L);
        Main.getInstance().getManager().getNpcManager().unregisterAll();
        //maybe save scores into database, etc.. async
    }

    @Override
    public void onStateLeave() {}
}
