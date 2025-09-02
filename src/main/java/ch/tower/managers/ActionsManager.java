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
    private double multiplier = 1.0;

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
        multiplier = r.getDouble("MULTIPLIER");
    }

    public double getMultiplier()
    {
        return this.multiplier;
    }

    public void setMultiplier(double multiplier)
    {
        this.multiplier = multiplier;
    }

    public void startListening()
    {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        timeSpentTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::onTimeSpent, (20L*actions.playIntervalSeconds())+40L, 20L*actions.playIntervalSeconds());
        double startMoney = actions.startMoney()*multiplier;
        String startMsg = GameManager.getMessage("MSG_GAME_ACTION_START", startMoney+"");
        for(TowerPlayer player : TowerPlayer.getPlayers())
        {
            player.giveMoney(startMoney);
            player.displayBarText(startMsg, 80);
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
        double damageValue = actions.damageValue()*multiplier;
        e.getAttacker().giveMoney(damageValue*e.getAmount());
    }

    @EventHandler
    public void onKill(GameKillEvent e)
    {

        String victimName = e.getVictimDisplayName();
        TowerPlayer attacker = e.getAttacker();
        if(attacker != null)
        {
            double killValue = actions.killKillValue()*multiplier;
            attacker.giveMoney(killValue);
            attacker.displayBarText(GameManager.getMessage("MSG_GAME_ACTION_KILL", victimName, killValue+""), 40);
        }

        double assistMoney = actions.killAssistValue()*multiplier;
        String assistMsg = GameManager.getMessage("MSG_GAME_ACTION_ASSIST", victimName, assistMoney+"");
        for(TowerPlayer assist : e.getAssists())
        {
            assist.giveMoney(assistMoney);
            assist.displayBarText(assistMsg, 40);
        }

        TeamsManager.PlayerTeam team = attacker != null ? attacker.getTeam() : null;
        if(team == null)
            return;
        double participationMoney = actions.killParticipationValue()*multiplier;
        String participationMsg = GameManager.getMessage("MSG_GAME_ACTION_KILL_PART", victimName, participationMoney+"");
        for(TowerPlayer player : TowerPlayer.getPlayersInTeam(team))
        {
            if(player.equals(attacker) || e.getAssists().contains(player))
                continue;
            player.giveMoney(participationMoney);
            player.displayBarText(participationMsg, 40);
        }

    }

    @EventHandler
    public void onPoint(GamePointEvent e)
    {

        double pointMoney = actions.pointPointValue()*multiplier;
        double pointPartMoney = actions.pointParticipationValue()*multiplier;

        TowerPlayer scorer = e.getPlayer();
        scorer.giveMoney(pointMoney);
        scorer.displayBarText(GameManager.getMessage("MSG_GAME_ACTION_POINT", pointMoney+""), 60);

        String participationMsg = GameManager.getMessage("MSG_GAME_ACTION_POINT_PART", pointPartMoney+"");
        for(TowerPlayer player : TowerPlayer.getPlayersInTeam(scorer.getTeam()))
        {
            if(!player.equals(scorer))
            {
                player.giveMoney(pointPartMoney);
                player.displayBarText(participationMsg, 60);
            }
        }

    }

    public void onTimeSpent()
    {
        double money = actions.playValue()*multiplier;
        int secondsSpent = actions.playIntervalSeconds();
        String msg = secondsSpent < 60 ? secondsSpent + " second(s)" : secondsSpent == 60 ? "1 minute" : secondsSpent/60 + " minutes";
        msg = GameManager.getMessage("MSG_GAME_ACTION_PLAYED", msg, money+"");
        for(TowerPlayer player : TowerPlayer.getPlayers())
        {
            player.giveMoney(money);
            player.displayBarText(msg, 60);
        }
    }

}
