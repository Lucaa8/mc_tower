package ch.tower.listeners;

import ch.tower.TowerPlayer;
import ch.tower.managers.TeamsManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GameKillEvent extends Event {

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

    private final TowerPlayer victim;
    private final TowerPlayer attacker;
    private final List<TowerPlayer> assists;
    private final EntityDamageEvent.DamageCause cause;

    public GameKillEvent(@Nonnull TowerPlayer victim, @Nullable TowerPlayer attacker, @Nonnull List<TowerPlayer> assists, @Nonnull EntityDamageEvent.DamageCause cause) {
        this.victim = victim;
        this.attacker = attacker;
        this.assists = assists;
        this.cause = cause;
    }

    @Nonnull
    public TowerPlayer getVictim() {
        return victim;
    }

    @Nonnull
    public String getVictimDisplayName()
    {
        TeamsManager.PlayerTeam team = victim.getTeam();
        String name = victim.asOfflinePlayer().getName();
        if(team == null)
            return "§f"+name;
        return team.getColorCode() + name;
    }

    @Nullable
    public TowerPlayer getAttacker() {
        return attacker;
    }

    @Nonnull
    public List<TowerPlayer> getAssists() {
        return new ArrayList<>(assists);
    }

    @Nonnull
    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

}
