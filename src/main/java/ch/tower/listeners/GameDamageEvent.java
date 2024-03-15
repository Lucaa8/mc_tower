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
    private final double amount;
    private final double oldDamage;
    private final double newDamage;

    public GameDamageEvent(TowerPlayer attacker, TowerPlayer victim, double amount, double oldDamage, double newDamage) {
        this.attacker = attacker;
        this.victim = victim;
        this.amount = amount;
        this.oldDamage = oldDamage;
        this.newDamage = newDamage;
    }

    public TowerPlayer getAttacker() {
        return attacker;
    }

    public TowerPlayer getVictim() {
        return victim;
    }

    public double getAmount() {
        return amount;
    }

    public double getOldDamage()
    {
        return oldDamage;
    }

    public double getNewDamage()
    {
        return newDamage;
    }
}
