package ch.tower.listeners;

import ch.tower.TowerPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class GameDamageEvent extends Event {

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

    public enum DamageType {
        SWORD, PICKAXE, AXE, BOW, POTION, ROD, FEATHER
    }

    private final TowerPlayer attacker;
    private final TowerPlayer victim;
    private final DamageType type;
    private final double amount;

    public GameDamageEvent(TowerPlayer attacker, TowerPlayer victim, DamageType type, double amount) {
        this.attacker = attacker;
        this.victim = victim;
        this.type = type;
        this.amount = amount;
    }

    public TowerPlayer getAttacker() {
        return attacker;
    }

    public TowerPlayer getVictim() {
        return victim;
    }

    public DamageType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

}
