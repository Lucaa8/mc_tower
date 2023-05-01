package ch.tower.events;

import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.managers.TeamsManager;
import ch.tower.utils.NPC.NPCLoader;
import ch.tower.utils.items.NBTTags;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

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

    //Maybe add a delay before giving back a player's bow (level 1 of BOW). To avoid spam camper killing themselves just to get back a bow
    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent e)
    {
        //TODO: tester (bcp de problemes ici)
        Player player = e.getEntity();
        EntityDamageEvent.DamageCause deathCause = player.getLastDamageCause().getCause();
        if (deathCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        {
            if(player.getLastDamageCause().getEntity() instanceof Player)
            {
                Player attacker = (Player) player.getLastDamageCause().getEntity();

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
            if(i0 != null && i0.getType() != Material.AIR && NBTTags.getInstance().getNBT(i0).hasTag("id-inv"))
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
        NBTTags.NBTItem nbt = NBTTags.getInstance().getNBT(item);
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

    @EventHandler void onQuit(PlayerQuitEvent e)
    {
        //TODO: tester
        Player p = e.getPlayer();
        if(TowerPlayer.getPlayer(p) == null)
        {
            e.setQuitMessage("");
        }
        else
        {
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
            Main.getInstance().getManager().getScoreboardManager().setBoard(p.asPlayer(), Main.getInstance().getManager().getState().name());
            p.updateBoard();
            p.giveTools();
            p.giveFood();
        }
        NPCLoader.load();
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryEvent(), Main.getInstance());
    }

    @Override
    public void onStateLeave()
    {

    }
}
