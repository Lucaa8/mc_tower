package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.listeners.GameDamageEvent;
import ch.tower.listeners.GameKillEvent;
import ch.tower.listeners.GamePointEvent;
import org.bukkit.Bukkit;
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
        timeSpentTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::onTimeSpent, (20L*actions.playIntervalSeconds())+40L, 20L*actions.playIntervalSeconds());
        for(TowerPlayer player : TowerPlayer.getPlayers())
        {
            player.giveMoney(actions.startMoney());
            player.displayBarText("§fYou received §b" + actions.startMoney() + "$ §fas starting money.", 80);
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
        e.getAttacker().giveMoney(actions.damageValue()*e.getAmount());
    }

    @EventHandler
    public void onKill(GameKillEvent e)
    {

        String victimName = e.getVictimDisplayName();
        TowerPlayer attacker = e.getAttacker();
        if(attacker != null)
        {
            attacker.giveMoney(actions.killKillValue());
            attacker.displayBarText("§fKilled " + victimName + "§f (§a+"+actions.killKillValue()+"$§f)", 40);
        }

        double assistMoney = actions.killAssistValue();
        for(TowerPlayer assist : e.getAssists())
        {
            assist.giveMoney(assistMoney);
            assist.displayBarText("§fAssist on " + victimName + "§f (§a+"+assistMoney+"$§f)", 40);
        }

        //To test
        TeamsManager.PlayerTeam team = attacker != null ? attacker.getTeam() : null;
        if(team == null)
            return;
        double participationMoney = actions.killParticipationValue();
        for(TowerPlayer player : team.getInfo().getTowerPlayers())
        {
            if(player.equals(attacker) || e.getAssists().contains(player))
                continue;
            player.giveMoney(participationMoney);
            player.displayBarText("§fParticipation on " + victimName + "§f (§a+"+participationMoney+"$§f)", 40);
        }

    }

    @EventHandler
    public void onPoint(GamePointEvent e)
    {

        double pointMoney = actions.pointPointValue();
        double pointPartMoney = actions.pointParticipationValue();

        TowerPlayer scorer = e.getPlayer();
        scorer.giveMoney(pointMoney);
        scorer.displayBarText("§fYou scored! (§a+"+pointMoney+"$§f)", 60);

        //To test
        for(TowerPlayer player : e.getTeam().getInfo().getTowerPlayers())
        {
            if(!player.equals(scorer))
            {
                player.giveMoney(pointPartMoney);
                scorer.displayBarText("§fYour team scored §7("+scorer.asOfflinePlayer().getName()+")§f! (§a+"+pointPartMoney+"$§f)", 60);
            }
        }

    }

    public void onTimeSpent()
    {
        double money = actions.playValue();
        int secondsSpent = actions.playIntervalSeconds();
        String msg = secondsSpent < 60 ? secondsSpent + " second(s)" : secondsSpent == 60 ? "1 minute" : secondsSpent/60 + " minutes";
        for(TowerPlayer player : TowerPlayer.getPlayers())
        {
            player.giveMoney(money);
            player.displayBarText("§fPlayed " + msg + " (§a+"+money+"$§f)", 60);
        }
    }

}
