package ch.tower.events;

import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.managers.TeamsManager;
import ch.tower.shop.LuckShuffle;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class GameEvents implements StateEvents
{
    private static GameEvents instance = null;
    private GameEvents(){}

    public static synchronized GameEvents getInstance()
    {
        if(instance == null)
        {
            instance = new GameEvents();
        }
        return instance;
    }

    //------------------- START OF THE HARMLESS FEATHER SECTION -------------------//

    @EventHandler
    public void onDamageWithFeather(EntityDamageEvent e)
    {
        if(e.getEntityType() == EntityType.PLAYER && e.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            Player p = (Player) e.getEntity();
            if(isHarmlessFeatherValid(p, EquipmentSlot.HAND) || isHarmlessFeatherValid(p, EquipmentSlot.OFF_HAND))
            {
                //Removes 15% on the FINAL damage if the player got a harmless feather in one of his hand.
                //if the player already have -24% fall damage (FFII enchant) it WONT remove 39% (24%+15%) of the damage. But only 15% of the damage after the 24% reduction.
                //e.g if the damage was 15.00. 39% of 15.00 is 5.85 then the final damage will be 15.00-5.85=9.15
                //but in our case its 24% of 15.00 = 3.6 so the pre-final damage => 15.00 - 3.6 = 11.4
                //THEN we apply the 15% reduction on the pre-final damage: 15% of 11.4 = 1.71 so the FINAL damage is 11.4 - 1.71 = 9.69 (The damage is bigger than the 39% reduction)
                //The difference isn't big here but a big fall damage can be 25-30 and the player can barely survive due to the -24% AND the -15% after that
                double ratio = e.getFinalDamage() * 15.0 /100.0;
                e.setDamage(e.getFinalDamage()-ratio);
            }
        }
    }

    private boolean isHarmlessFeatherValid(Player player, EquipmentSlot slot)
    {
        ItemStack is = player.getInventory().getItem(slot);
        if(is != null && is.getType() == Material.FEATHER)
        {
            NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(is);
            return nbt.hasTag("UUID") && nbt.getString("UUID").equals("harmless_feather");
        }
        return false;
    }

    //------------------- END OF THE HARMLESS FEATHER SECTION -------------------//

    //------------------- START OF THE LUCK POTION SECTION -------------------//

    @EventHandler
    public void onLuckPotionConsume(PlayerItemConsumeEvent e)
    {
        if(e.getItem().getType() == Material.POTION)
        {
            Player p = e.getPlayer();
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->
            {
                if(!p.isOnline())return;
                ItemStack is = p.getInventory().getItem(e.getHand());
                if(is != null && is.getType()==Material.GLASS_BOTTLE)
                {
                    p.getInventory().setItem(e.getHand(), new ItemStack(Material.AIR));
                }
            }, 1L);
            NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(e.getItem());
            if(nbt.hasTag("UUID") && nbt.getString("UUID").equals("7_luck"))
            {
                p.playSound(p, Sound.AMBIENT_CAVE, 1.0f, 1.0f);
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, true, false));
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->
                {
                    if(!p.isOnline())return;
                    LuckShuffle.apply(TowerPlayer.getPlayer(p));
                }, 80L);
            }
        }
    }

    //------------------- END OF THE LUCK POTION SECTION -------------------//

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent e)
    {
        //TODO: tester (bcp de problemes ici)
        Player player = e.getEntity();
        EntityDamageEvent.DamageCause deathCause = player.getLastDamageCause().getCause();
        if (deathCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        {
            if(player.getLastDamageCause().getEntity() instanceof Player attacker)
            {

                TowerPlayer towerPlayer = TowerPlayer.getPlayer(player);
                TowerPlayer towerAttacker = TowerPlayer.getPlayer(attacker);
                towerPlayer.addDeath();
                towerAttacker.addKill();
                String message = TeamsManager.getPlayerTeam(player).getColorCode()
                        + player.getName() + ChatColor.RESET + " has been killed by " + TeamsManager.getPlayerTeam(attacker).getColorCode() + attacker.getName();
                e.setDeathMessage(message);
            }
        }
        if (deathCause == EntityDamageEvent.DamageCause.VOID)
        {
            String message = TeamsManager.getPlayerTeam(player).getColorCode()
                    + player.getName() + ChatColor.RESET + " fell into the void.";
            TowerPlayer towerPlayer = TowerPlayer.getPlayer(player);
            towerPlayer.addDeath();
            e.setDeathMessage(message);
        }
    }

    @EventHandler
    public void onRespawnGiveStuffAndTeleport(PlayerRespawnEvent e)
    {
        TowerPlayer player = TowerPlayer.getPlayer(e.getPlayer());

        if(player != null)
        {
            e.setRespawnLocation(player.getTeam().getSpawn());
            player.giveTools();
            player.giveFood();
        }
    }

    @EventHandler
    public void onChatByPlayer(AsyncPlayerChatEvent e)
    {
        e.setCancelled(true);
        StringBuilder s = new StringBuilder("");
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam t = TeamsManager.getPlayerTeam(p);
        if (t != null)
        {
            s.append(t.getColorCode());
            s.append("[");
            s.append(t.name());
            s.append("] ");
            s.append(p.getDisplayName());
            s.append(ChatColor.RESET);
        } else
        {
            s.append(p.getDisplayName());
        }

        if (e.getMessage().startsWith("!") && e.getMessage().length() != 1 && t != TeamsManager.PlayerTeam.SPECTATOR)
        {
            s.append(" (Global)");
            s.append(" : ");
            s.append(e.getMessage().substring(1));
            Bukkit.broadcast(s.toString(), Server.BROADCAST_CHANNEL_USERS);
        }
        else
        {
            TeamsManager.PlayerTeam team = TeamsManager.getPlayerTeam(e.getPlayer());
            s.append(" : ");
            s.append(e.getMessage());
            for(Player playerOfTeam: team.getInfo().getPlayers())
            {
                playerOfTeam.sendMessage(s.toString());
            }
        }


    }

    //------------------- START OF SHOP/ITEMS SECTION -------------------//

    @EventHandler
    public void onDropRestrictedItem(PlayerDropItemEvent e)
    {
        e.setCancelled(isRestrictedItem(e.getItemDrop().getItemStack()));
    }

    @EventHandler
    public void onInventoryClickRestrictedItem(InventoryClickEvent e)
    {
        if(e.getClickedInventory() != null)
        {
            ItemStack i0 = e.getClickedInventory().getItem(0);
            if(i0 != null && i0.getType() != Material.AIR && SpigotApi.getNBTTagApi().getNBT(i0).hasTag("id-inv"))
            {
                //This is shop menu inventory, we do not need to cancel anything, the shop handler will do that.
                return;
            }
        }
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if(holder != e.getWhoClicked())
        {
            if(e.getClickedInventory() != e.getWhoClicked().getInventory() || e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT)
            {
                e.setCancelled(isRestrictedItem(e.getCurrentItem()) || isRestrictedItem(e.getCursor()));
            }
        }
    }

    //Here because of the InventoryClickEvent glitch;
    //Inventory drag events are called instead of inventory click events when the item is being dragged,
    //and an item being dragged across 2 pixels in the same slot already counts as dragging, which doesn't call the inventory click event.
    @EventHandler
    public void onInventoryDragRestrictedItem(InventoryDragEvent e)
    {
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if(holder != e.getWhoClicked())
        {
            if(e.getInventory() != e.getWhoClicked().getInventory())
            {
                e.setCancelled(isRestrictedItem(e.getOldCursor()));
            }
        }
    }

    @EventHandler
    public void onDeathRemoveRestrictedItems(PlayerDeathEvent e)
    {
        e.getDrops().removeIf(this::isRestrictedItem);
    }

    private boolean isRestrictedItem(ItemStack item)
    {
        if(item == null || item.getType() == Material.AIR)
            return false;
        NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
        return nbt.hasTag("UUID") && nbt.getString("UUID").startsWith("current_");
    }

    //------------------- END OF SHOP/ITEMS SECTION -------------------//

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        //TODO: Tester pour spectateur et team rouge
        Player p = e.getPlayer();
        if(TowerPlayer.getPlayer(p) == null)
        {
            TeamsManager.PlayerTeam.SPECTATOR.addPlayer(p);
            e.setJoinMessage("");
        }
        else
        {
            String message = TeamsManager.getPlayerTeam(p).getColorCode() + p.getName() + ChatColor.RESET + " joined the game.";
            e.setJoinMessage(message);
        }
        p.teleport(TeamsManager.getPlayerTeam(p).getSpawn());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        //TODO: tester
        Player p = e.getPlayer();
        if(TowerPlayer.getPlayer(p) == null)
        {
            e.setQuitMessage("");
        }
        else
        {
            //TODO luca: crash car TeamApi retire le joueur de la team, meme crash lorsqu'il rejoint la partie.
            String message = TeamsManager.getPlayerTeam(p).getColorCode() + p.getName() + ChatColor.RESET + " left the game.";
            e.setQuitMessage(message);
        }
    }

    @Override
    public void onStateBegin()
    {
        TowerPlayer.registerPlayers();
        Bukkit.broadcast("The game begins. GL HF", Server.BROADCAST_CHANNEL_USERS);
        Collection<? extends Player> players = Main.getInstance().getServer().getOnlinePlayers();
        for (Player player : players)
        {
            player.teleport(TeamsManager.getPlayerTeam(player).getSpawn());
            TowerPlayer p = TowerPlayer.getPlayer(player);
            if(p==null) continue;
            Main.getInstance().getManager().getScoreboardManager().updateBoard(player);
            p.giveTools();
            p.giveFood();
        }
        Main.getInstance().getManager().getNpcManager().load();
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryEvent(), Main.getInstance());
    }

    @Override
    public void onStateLeave()
    {

    }
}
