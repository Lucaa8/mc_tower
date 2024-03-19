package ch.tower.items;

import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class WeaponStatistics {

    public static class ShootListener implements Listener {

        private static final Map<Arrow, ItemStack> arrows = new HashMap<>();
        private int taskId;

        public void start()
        {
            Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
            //Entity#isValid: Returns false if the entity has died, been despawned for some other reason.
            //Will return false when the arrow disappears. Set a maximum of 30sec for arrows lifespan in the spigot.yml (world-settings.default.arrow-despawn-rate)
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), ()->arrows.keySet().removeIf(arrow -> !arrow.isValid()), 20L * 30, 20L * 30);
        }

        public void stop()
        {
            HandlerList.unregisterAll(this);
            Bukkit.getScheduler().cancelTask(taskId);
            arrows.clear();
        }

        @EventHandler
        public void onEntityShoot(EntityShootBowEvent e)
        {
            if(!(e.getEntity() instanceof Player) || !(e.getProjectile() instanceof Arrow arrow))
                return;
            arrows.put(arrow, e.getBow());
        }

        @Nullable
        public static ItemStack getBow(Projectile projectile)
        {
            if(projectile instanceof Arrow arrow && arrows.containsKey(arrow))
            {
                return arrows.remove(arrow);
            }
            return null;
        }

    }

    private final Map<String, Double> damage = new HashMap<>();

    public void addDamageWith(EntityDamageByEntityEvent e)
    {

        ItemStack item = null;
        if(e.getDamager() instanceof Player attacker)
        {
            item = attacker.getInventory().getItemInMainHand();
        }

        if(e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player)
        {
            if(proj.getType() == EntityType.SPLASH_POTION)
            {
                //get the thrown potion
                item = ((ThrownPotion) proj).getItem();
            }

            if(proj.getType() == EntityType.ARROW)
            {
                //get the bow
                item = ShootListener.getBow(proj);
            }
        }

        String id = getWeaponId(item);
        double currentDamage = damage.getOrDefault(id, 0.0d);
        damage.put(getWeaponId(item), currentDamage+e.getFinalDamage());

    }

    private String getWeaponId(ItemStack item)
    {

        if(item == null || item.getType() == Material.AIR)
            return "hand";

        NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
        if(nbt.hasTag("WeaponID")) //current_sword, current_axe, etc... but with their real id (0_sword, 1_sword, ...)
        {
            return nbt.getString("WeaponID");
        }
        if(!nbt.hasTag("UUID"))
        {
            return "other";
        }

        return nbt.getString("UUID");

    }

    public Map<String, Double> getDamage()
    {
        return new HashMap<>(this.damage);
    }

}
