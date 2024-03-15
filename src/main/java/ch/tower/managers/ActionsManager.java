package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.listeners.GameDamageEvent;
import ch.tower.listeners.GameKillEvent;
import ch.tower.listeners.GamePointEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;

public class ActionsManager implements Listener {

    public static final File ACTIONS_FILE = new File(Main.getInstance().getDataFolder(), "actions.json");

    private int timeSpentTaskId;

    private record Actions(
            double startMoney,
            double playValue,
            int playIntervalSeconds,
            double killKillValue,
            double killAssistValue,
            double killParticipationValue,
            double pointPointValue,
            double pointParticipationValue,
            double damageValue
    ){}

    private final Actions actions;

    public ActionsManager()
    {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(ACTIONS_FILE);
        JSONApi.JSONReader base = r.getJson("BASE");
        JSONApi.JSONReader play = base.getJson("PLAY");
        JSONApi.JSONReader kill = base.getJson("KILL");
        JSONApi.JSONReader point = base.getJson("POINT");
        actions = new Actions(
                r.getDouble("START_MONEY"),
                play.getDouble("PLAY"),
                play.getInt("INTERVAL_SECONDS"),
                kill.getDouble("KILL"),
                kill.getDouble("ASSIST"),
                kill.getDouble("PARTICIPATION"),
                point.getDouble("POINT"),
                point.getDouble("PARTICIPATION"),
                base.getDouble("DAMAGE")
        );
    }

    public void startListening()
    {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        timeSpentTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::onTimeSpent, 40L, 20L*actions.playIntervalSeconds());
        for(TowerPlayer player : TowerPlayer.getPlayers())
        {
            player.giveMoney(actions.startMoney());
            Player p = player.asPlayer();
            if(p!=null&&p.isOnline())
            {
                p.sendMessage(GameManager.getMessage("MSG_GAME_ACTION_START_MONEY", ""+actions.startMoney()));
            }
        }
    }

    public void stopListening()
    {
        Bukkit.getScheduler().cancelTask(timeSpentTaskId);
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onDamage(GameDamageEvent e)
    {
        System.out.println("Old: " + e.getOldDamage() + ", Amount: " + e.getAmount() + ", New: " + e.getNewDamage());
        System.out.println("Damage value: " + actions.damageValue());
        System.out.println();
    }

    @EventHandler
    public void onKill(GameKillEvent e)
    {
        System.out.println("Kill value: " + actions.killKillValue());
        System.out.println("Assist value: " + actions.killAssistValue());
        System.out.println("Kill part. value: " + actions.killParticipationValue());
        System.out.println();
    }

    @EventHandler
    public void onPoint(GamePointEvent e)
    {
        System.out.println("Point value: " + actions.pointPointValue());
        System.out.println("Point part.: " + actions.killParticipationValue());
        System.out.println();
    }

    public void onTimeSpent()
    {
        for(TowerPlayer player : TowerPlayer.getPlayers())
        {

        }
    }

}
