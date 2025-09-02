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
import java.util.Objects;
import java.util.function.Function;

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
        if(GameManager.ConfigField.FRIENDLY_FIRE.getBool() && !GameManager.ConfigField.FRIENDLY_FIRE_MONEY.getBool() &&
                e.getAttacker().getTeam() == e.getVictim().getTeam())
        {
            return;
        }
        double damageValue = actions.damageValue()*multiplier;
        e.getAttacker().giveMoney(damageValue*e.getAmount());
    }

    //While money earnings can be disabled between players of the same team, if an enemy player had an assistance on a teamkill
    //they will still receive the money for assistance. The system doesnt cancel everything if victim and attacker are in the same team.

    @EventHandler
    public void onKill(GameKillEvent e)
    {

        String victimName = e.getVictimDisplayName();

        //"player" arg here could be any player but the victim.
        //e.g. The killer/attacker of the victim, a player who assisted in the kill of the victim or even every other player of the team (participation)
        Function<TowerPlayer, Boolean> giveMoney = (player) -> {
            //Friendly Fire is disabled so attacker/assist,etc. is necessarily in the opposite team, so they receive money
            if(!GameManager.ConfigField.FRIENDLY_FIRE.getBool()) return true;
            //Friendly Fire is enabled but the rule allows ff actions to reward money
            if(GameManager.ConfigField.FRIENDLY_FIRE_MONEY.getBool()) return true;
            //FF is enabled but the rule disallow ff action to reward money, so we need to check if the player and victim are on the same team
            TeamsManager.PlayerTeam victimTeam = e.getVictim().getTeam();
            TeamsManager.PlayerTeam playerTeam = player.getTeam();
            if(victimTeam == null || playerTeam == null)
                //shouldn't happen, even if the player left the game, as their team is still stored as "abandoningTeam"
                return false;
            return !victimTeam.equals(playerTeam);
        };

        TowerPlayer attacker = e.getAttacker();

        if(attacker != null && giveMoney.apply(attacker))
        {
            double killValue = actions.killKillValue()*multiplier;
            attacker.giveMoney(killValue);
            attacker.displayBarText(GameManager.getMessage("MSG_GAME_ACTION_KILL", victimName, killValue+""), 40);
        }

        double assistMoney = actions.killAssistValue()*multiplier;
        String assistMsg = GameManager.getMessage("MSG_GAME_ACTION_ASSIST", victimName, assistMoney+"");
        for(TowerPlayer assist : e.getAssists())
        {
            if(!giveMoney.apply(assist))
                continue;
            assist.giveMoney(assistMoney);
            assist.displayBarText(assistMsg, 40);
        }

        TeamsManager.PlayerTeam team = attacker != null ? attacker.getTeam() : null;
        if(team == null || (team == e.getVictim().getTeam() && !GameManager.ConfigField.FRIENDLY_FIRE_MONEY.getBool()))
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
