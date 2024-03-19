package ch.tower.listeners;

import ch.tower.TowerPlayer;
import ch.tower.managers.TeamsManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class GamePointEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final TeamsManager.PlayerTeam team;
    private final TowerPlayer player;
    private final int oldPointCount;
    private final int newPointCount;
    private final int pointGoal;

    public GamePointEvent(TeamsManager.PlayerTeam team, TowerPlayer player, int oldPointCount, int newPointCount, int pointGoal)
    {
        this.team = team;
        this.player = player;
        this.oldPointCount = oldPointCount;
        this.newPointCount = newPointCount;
        this.pointGoal = pointGoal;
    }

    public TeamsManager.PlayerTeam getTeam() {
        return team;
    }

    public TowerPlayer getPlayer() {
        return player;
    }

    public int getOldPointCount() {
        return oldPointCount;
    }

    public int getNewPointCount() {
        return newPointCount;
    }

    public int getPointGoal() {
        return pointGoal;
    }

}
