package ch.tower.events;

import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.items.WeaponStatistics;
import ch.tower.listeners.GameDamageEvent;
import ch.tower.listeners.GameKillEvent;
import ch.tower.listeners.GamePointEvent;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.TeamsManager;
import ch.tower.managers.WorldManager.WorldZone;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

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

    private BukkitTask timerTask;
    private long startedAt;
    private int maxTimerSeconds;

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
                //Df=Di×(1−0.24)×(1−0.15)
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

    //------------------- START OF THE POTION SECTION -------------------//

    //Removes the glass bottle after the player consumed the potion
    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent e)
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
        }
    }

    //------------------- END OF THE POTION SECTION -------------------//

    //------------------- START OF THE KILLS SECTION -------------------//

    private final double immuneOnDeath = GameManager.ConfigField.TIMER_IMMUNE_ON_DEATH.getDecimal();

    @EventHandler
    public void onRespawnSetImmune(PlayerRespawnEvent e)
    {
        TowerPlayer player = TowerPlayer.getPlayer(e.getPlayer());
        if(player != null)
        {
            //this method set invulnerable the player and waits immuneOnDeath*20 ticks before removing the invulnerability
            player.setImmune(immuneOnDeath);
        }
    }

    private String getKillMessage(DamageCause cause, String victim, @Nullable String attacker)
    {
        String key = "MSG_DEATH_"+cause.name()+(cause==DamageCause.FIRE?"_TICK":"")+"_"+(attacker==null?"1":"2");
        String message = attacker == null ? GameManager.getMessage(key, victim) : GameManager.getMessage(key, victim, attacker);
        if(message.equals("Unknown message"))
        {
            return GameManager.getMessage("MSG_DEATH_DEFAULT", victim);
        }
        return message;
    }

    private String addKill(TowerPlayer attacker, String victimName)
    {
        TeamsManager.PlayerTeam team = attacker.getTeam();
        if(team == null)
        {
            team = attacker.getAbandoningTeam();
        }
        attacker.addKill();
        attacker.displayBarText("§fKilled " + victimName, 40);
        Player pAttacker = attacker.asPlayer();
        if(pAttacker != null && pAttacker.isOnline())
        {
            pAttacker.playSound(pAttacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1f);
        }
        //attackerName, formatted the same for both FIRE_TICK and others
        return (team == null ? "§f" : team.getColorCode()) + attacker.asOfflinePlayer().getName();
    }

    private void addAssist(List<TowerPlayer> players, String victimName)
    {
        for(TowerPlayer assist : players)
        {
            assist.addAssist();
            assist.displayBarText("§fAssist on " + victimName, 40);
            Player pAssist = assist.asPlayer();
            if(pAssist != null && pAssist.isOnline())
            {
                pAssist.playSound(pAssist.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.6f, 1f); //chose better sound
            }
        }
    }

    @EventHandler
    public void onDeathOfPlayer(PlayerDeathEvent e)
    {
        Player player = e.getEntity();
        TowerPlayer towerPlayer = TowerPlayer.getPlayer(player);

        if(towerPlayer==null||towerPlayer.getTeam()==null)
        {
            e.setDeathMessage(null);
            return;
        }

        towerPlayer.addDeath();
        String playerName = towerPlayer.getTeam().getColorCode()+player.getName();

        EntityDamageEvent deathCause = player.getLastDamageCause();
        if(deathCause == null)
        {
            e.setDeathMessage(GameManager.getMessage("MSG_DEATH_DEFAULT", playerName));
            return;
        }

        e.setDeathMessage(getKillMessage(deathCause.getCause(), playerName, null));

        TowerPlayer attacker = null;
        List<TowerPlayer> assists = new ArrayList<>();
        switch (deathCause.getCause())
        {
            case FIRE_TICK -> {
                attacker = towerPlayer.getLastBurntBy();
                if(attacker != null) {
                    String attackerName = addKill(attacker, playerName);
                    assists = towerPlayer.getLastAssistedBy(false);
                    //The killer can be in the assist list because he hit that player 2 sec before (with fire aspect) and another player hit it too between the fire aspect hit and the deadly fire tick
                    assists.remove(attacker);
                    //Transform the last damager to an assist because he did the last damage to the dead player before the fire tick kill him.
                    TowerPlayer lastDamager = towerPlayer.getLastDamagedBy();
                    if(lastDamager != null && !attacker.equals(lastDamager))
                    {
                        assists.add(0, lastDamager);
                    }
                    addAssist(assists, playerName);
                    e.setDeathMessage(getKillMessage(deathCause.getCause(), playerName, attackerName));
                }
            }
            case ENTITY_ATTACK, VOID, FALL, PROJECTILE, MAGIC -> {
                attacker = towerPlayer.getLastDamagedBy();
                if(attacker != null)
                {
                    String attackerName = addKill(attacker, playerName);
                    assists = towerPlayer.getLastAssistedBy(true);
                    addAssist(assists, playerName);
                    e.setDeathMessage(getKillMessage(deathCause.getCause(), playerName, attackerName));
                }
            }
        }

        Bukkit.getPluginManager().callEvent(new GameKillEvent(towerPlayer, attacker, assists, deathCause.getCause()));

        towerPlayer.damage(null); //Reset last damager
        towerPlayer.damageFire(null, 0); //Reset the last burn damager

    }

    private void callDamageEvent(TowerPlayer attacker, TowerPlayer victim, double amount, double oldDamage, double newDamage)
    {
        GameDamageEvent onDamage = new GameDamageEvent(attacker, victim, amount, oldDamage, newDamage);
        Bukkit.getPluginManager().callEvent(onDamage);
    }

    private Player getDamager(Entity damager)
    {
        if(damager instanceof Player p)
        {
            return p;
        }
        else if(damager instanceof Projectile s)
        {
            return (Player) s.getShooter();
        }
        return null;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e)
    {
        Player attacker = getDamager(e.getDamager());
        if(attacker != null && e.getEntity() instanceof Player victim)
        {
            TowerPlayer towerVictim = TowerPlayer.getPlayer(victim);
            TowerPlayer towerAttacker = TowerPlayer.getPlayer(attacker);
            if(towerVictim != null && towerAttacker != null)
            {

                //Disable friendly fire manually as the teams are only client side.
                if(!GameManager.ConfigField.FRIENDLY_FIRE.getBool()&&towerVictim.getTeam()!=null&&towerVictim.getTeam().equals(towerAttacker.getTeam()))
                {
                    e.setCancelled(true);
                    return;
                }

                towerVictim.damage(towerAttacker);
                double oldDamage = towerAttacker.getDamage();
                double newDamage = towerAttacker.addDamageWithWeapon(e);
                callDamageEvent(towerAttacker, towerVictim, e.getFinalDamage(), oldDamage, newDamage);
            }
        }
    }

    @EventHandler
    public void onBurnDamage(EntityDamageEvent e)
    {
        if(e.getCause() != DamageCause.FIRE_TICK || !(e.getEntity() instanceof Player v))
            return;

        TowerPlayer victim = TowerPlayer.getPlayer(v);
        if(victim == null)
            return;

        TowerPlayer attacker = victim.getLastBurntBy(); //call before tickFire() because of the reset of burn damager when calling getLastBurntBy if burnTicksLeft == 1
        victim.tickFire();

        if(attacker != null)
        {
            double oldDamage = attacker.getDamage();
            double newDamage = attacker.addDamage(e.getFinalDamage());
            callDamageEvent(attacker, victim, e.getFinalDamage(), oldDamage, newDamage);
        }

    }

    @EventHandler
    public void onApplyBurnDamage(EntityCombustByEntityEvent e)
    {
        if(e.getEntity() instanceof Player victim && e.getCombuster() instanceof Player attacker)
        {
            TowerPlayer tpVictim = TowerPlayer.getPlayer(victim);
            if(tpVictim != null)
            {
                TowerPlayer tpAttacker = TowerPlayer.getPlayer(attacker);
                if(tpAttacker != null)
                {
                    tpVictim.damageFire(tpAttacker, e.getDuration());
                }
            }
        }
    }

    //Player has stopped burning (other than fade fire ticks)
    @EventHandler
    public void onFireResistancePotionConsume(PlayerItemConsumeEvent e)
    {
        if(e.getItem().getType() == Material.POTION && e.getItem().getItemMeta() instanceof PotionMeta pm)
        {
            List<PotionEffect> effects = pm.getBasePotionType().getPotionEffects();
            if(effects.size() == 1 && effects.get(0).getType()==PotionEffectType.FIRE_RESISTANCE)
            {
                TowerPlayer tp = TowerPlayer.getPlayer(e.getPlayer());
                if(tp != null)
                {
                    tp.damageFire(null, 0);
                }
            }
        }
    }

    @EventHandler
    public void onMoveInsideWater(PlayerMoveEvent e)
    {
        if(e.getTo() == null)
            return;

        if(e.getFrom().getBlock().getType() != Material.WATER && e.getTo().getBlock().getType() == Material.WATER && e.getPlayer().getFireTicks() > 0)
        {
            TowerPlayer tp = TowerPlayer.getPlayer(e.getPlayer());
            if(tp != null)
            {
                tp.damageFire(null, 0);
            }
        }
    }

    //------------------- END OF THE KILLS SECTION -------------------//

    //------------------- START OF THE POINT POOL SECTION -------------------//

    private WorldZone redPool;
    private WorldZone bluePool;

    @EventHandler
    public void onMoveDetectsPool(PlayerMoveEvent e)
    {
        //checks if the player actually moved or if it only changed direction with his eyes (pitch/yaw), in such case he cant go from outside to inside the pool
        //and I remove a lot of checking which takes time.
        if(e.getTo() == null || e.getTo().distance(e.getFrom()) < 0.001)
            return;
        if(e.getPlayer().getGameMode() == GameMode.SPECTATOR)
            return;

        if(bluePool != null && bluePool.isInside(e.getTo()))
        {
            checkAndScorePoint(e.getPlayer(), true);
        }
        else if(redPool != null && redPool.isInside(e.getTo()))
        {
            checkAndScorePoint(e.getPlayer(), false);
        }
    }

    private void checkAndScorePoint(Player player, boolean bluePool)
    {
        TowerPlayer towerPlayer = TowerPlayer.getPlayer(player);
        if(towerPlayer == null || towerPlayer.getTeam() == null)
            return;
        if(towerPlayer.getTeam() == TeamsManager.PlayerTeam.BLUE && !bluePool)
        {
            score(towerPlayer, TeamsManager.PlayerTeam.BLUE);
        }
        else if(towerPlayer.getTeam() == TeamsManager.PlayerTeam.RED && bluePool)
        {
            score(towerPlayer, TeamsManager.PlayerTeam.RED);
        }

    }

    private void score(TowerPlayer player, TeamsManager.PlayerTeam team)
    {
        int oldPoints = team.getPoints();
        int points = team.addPointAndGet();
        int goal = GameManager.ConfigField.GOAL_POINTS.get();
        Bukkit.getPluginManager().callEvent(new GamePointEvent(team, player, oldPoints, points, goal)); //cannot be cancelled
        player.addPoint();
        player.asPlayer().teleport(team.getSpawn());
        player.asPlayer().setHealth(20d);
        player.asPlayer().setFireTicks(0);
        player.damageFire(null, 0);
        Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_POINT", team.getColorCode()+player.asOfflinePlayer().getName(), String.valueOf(points), String.valueOf(goal)));
        for(Player p : Bukkit.getOnlinePlayers())
        {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.2f, 1f);
            if(team == TeamsManager.PlayerTeam.BLUE)
                ScoreboardManager.BoardField.POINTS_BLUE.update(p, String.valueOf(points));
            else
                ScoreboardManager.BoardField.POINTS_RED.update(p, String.valueOf(points));
        }
        if(points >= goal)
        {
            Main.getInstance().getManager().setState(GameManager.GameState.END);
        }
    }

    //------------------- END OF THE POINT POOL SECTION -------------------//

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
        else
        {
            e.setRespawnLocation(TeamsManager.PlayerTeam.SPECTATOR.getSpawn());
        }
    }

    @EventHandler
    public void onChatByPlayer(AsyncPlayerChatEvent e)
    {
        e.setCancelled(true);
        String message = "";
        Player p = e.getPlayer();
        TeamsManager.PlayerTeam t = TeamsManager.getPlayerTeam(p);
        if (t != null)
        {
            message += t.getColorCode() + "[" + t.getInfo().apiTeam().getDisplayName() + "] " + p.getName() + "§r";
        }
        else //cannot happen unless a bug manifested during the game
        {
            p.sendMessage(GameManager.getMessage("MSG_GAME_CANNOT_SEND_MESSAGE"));
            return;
        }

        if (e.getMessage().startsWith("!") && e.getMessage().length() != 1 && t != TeamsManager.PlayerTeam.SPECTATOR)
        {
            message += " (Global) : " + e.getMessage().substring(1);
            Bukkit.broadcastMessage(message);
        }
        else
        {
            message += " : " + e.getMessage();
            for(Player playerOfTeam: t.getInfo().getPlayers())
            {
                playerOfTeam.sendMessage(message);
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

    //------------------- START OF SPAWN PROTECTION SECTION -------------------//

    private WorldZone blueSpawnProtection;
    private WorldZone redSpawnProtection;

    private boolean isProtected(Location location)
    {
        return blueSpawnProtection.isInside(location) || redSpawnProtection.isInside(location);
    }

    @EventHandler
    public void onPlaceBlockProtected(BlockPlaceEvent e)
    {
        e.setCancelled(isProtected(e.getBlockPlaced().getLocation()));
    }

    @EventHandler
    public void onBreakBlockProtected(BlockBreakEvent e)
    {
        e.setCancelled(isProtected(e.getBlock().getLocation()));
    }

    @EventHandler
    public void onExplodeBlockProtected(EntityExplodeEvent e)
    {
        e.blockList().removeIf(b -> isProtected(b.getLocation()));
    }

    @EventHandler
    public void onAnvilBlockProtected(BlockPhysicsEvent e)
    {
        Block b = e.getBlock();
        if(b.getType().hasGravity() && isProtected(b.getLocation())) {
            b.setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPistonEx(BlockPistonExtendEvent e) {
        for(Block b : e.getBlocks()) {
            if(isProtected(b.getLocation().add(e.getDirection().getDirection()))) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPistonRe(BlockPistonRetractEvent e) {
        for(Block b : e.getBlocks()) {
            if(isProtected(b.getLocation().add(e.getDirection().getDirection()))) {
                e.setCancelled(true);
                break;
            }
        }
    }

    //------------------- END OF SPAWN PROTECTION SECTION -------------------//

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        TowerPlayer asTowerPlayer = TowerPlayer.getPlayer(p);
        if(asTowerPlayer == null)
        {
            TeamsManager.PlayerTeam.SPECTATOR.addPlayer(p);
            p.setGameMode(GameMode.SPECTATOR);
            p.getInventory().clear();
            e.setJoinMessage("");
        }
        else
        {
            TeamsManager.PlayerTeam team = asTowerPlayer.stopAbandon();
            if(team != null)
            {
                team.addPlayer(p);
            }
            else
            {
                p.kickPlayer(GameManager.getMessage("MSG_GAME_ABANDON_4"));
            }
            e.setJoinMessage(null);
        }
        p.teleport(TeamsManager.getPlayerTeam(p).getSpawn());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();
        TowerPlayer asTowerPlayer = TowerPlayer.getPlayer(p);
        if(asTowerPlayer == null)
        {
            e.setQuitMessage("");
        }
        else
        {
            TeamsManager.PlayerTeam team = asTowerPlayer.startAbandon();
            e.setQuitMessage(null);
            if(team != null)
            {
                if(team.getInfo().getPlayers().size() == 1)
                {
                    TeamsManager.PlayerTeam opposite = team == TeamsManager.PlayerTeam.BLUE ? TeamsManager.PlayerTeam.RED : TeamsManager.PlayerTeam.BLUE;
                    String currentTeam = team.getColorCode()+team.getInfo().apiTeam().getDisplayName();
                    String oppositeTeam = opposite.getColorCode()+opposite.getInfo().apiTeam().getDisplayName();
                    Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_QUIT_LAST", currentTeam, oppositeTeam));
                    isForfeit = true;
                    winner = opposite;
                    Main.getInstance().getManager().setState(GameManager.GameState.END);
                }
            }
        }
    }

    private boolean isForfeit = false; //only used in onQuit and onStateLeave
    private TeamsManager.PlayerTeam winner; //stored at the end of this state (GameState.GAME) because we need it to update in the onStateBegin of GameState.END (Scoreboards)

    public TeamsManager.PlayerTeam getWinner()
    {
        return this.winner;
    }

    //register every arrow shot by players to link them to projectile damage on EntityDamageByEntityEvent
    private final WeaponStatistics.ShootListener bowShooterListener = new WeaponStatistics.ShootListener();

    @Override
    public void onStateBegin()
    {
        TowerPlayer.registerPlayers();
        Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_START"));
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
        bowShooterListener.start();
        Main.getInstance().getManager().getNpcManager().load();
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryEvent(), Main.getInstance());
        redPool = TeamsManager.PlayerTeam.RED.getInfo().pool();
        bluePool = TeamsManager.PlayerTeam.BLUE.getInfo().pool();
        redSpawnProtection = TeamsManager.PlayerTeam.RED.getInfo().spawnProtection();
        blueSpawnProtection = TeamsManager.PlayerTeam.BLUE.getInfo().spawnProtection();
        startedAt = System.currentTimeMillis();
        maxTimerSeconds = GameManager.ConfigField.TIMER_DURATION_GAME.get();
        Main.getInstance().getManager().getActionsManager().startListening();
        timerTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), ()->{
            long current = System.currentTimeMillis();
            int elapsedSec = (int) ((current - startedAt)/1000);
            for(Player online : Bukkit.getOnlinePlayers())
            {
                ScoreboardManager.BoardField.TIMER.update(online, ScoreboardManager.PlaceholderHelper.getGameTimer(elapsedSec));
            }
            int remaining = Math.max(0, maxTimerSeconds - elapsedSec);
            switch (remaining)
            {
                case 180, 120 -> Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_TIMER", (remaining/60)+" minutes"));
                case 60 -> Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_TIMER", "1 minute"));
                case 30,10,5 -> Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_TIMER", remaining+" seconds"));
                case 0 -> Main.getInstance().getManager().setState(GameManager.GameState.END);
            }
        }, 40L, 20L);
    }

    @Override
    public void onStateLeave()
    {
        Main.getInstance().getManager().getActionsManager().stopListening();
        timerTask.cancel();
        bowShooterListener.stop();

        TeamsManager.PlayerTeam blue = TeamsManager.PlayerTeam.BLUE;
        TeamsManager.PlayerTeam red = TeamsManager.PlayerTeam.RED;

        String bluePoints = blue.getColorCode()+blue.getPoints();
        String redPoints = red.getColorCode()+red.getPoints();

        if(!isForfeit) //if the game ended either by a team which reached goal point or at the end of the timer
        {
            if(blue.getPoints()==red.getPoints()) //possible if the timer went to 0 and the two teams have the same number of points
            {
                Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_EX", bluePoints, redPoints));
            }
            else
            {
                boolean blueWon = blue.getPoints()>red.getPoints();
                winner = blueWon ? blue : red;
                String winner = blueWon ? blue.getColorCode()+blue.getInfo().apiTeam().getDisplayName() : red.getColorCode()+red.getInfo().apiTeam().getDisplayName();
                Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_WIN", winner, blueWon ? bluePoints : redPoints, blueWon ? redPoints : bluePoints));
            }
        }

        AtomicInteger counter = new AtomicInteger();
        BiFunction<ToIntFunction<TowerPlayer>,String,String> sort = (func, suffix) -> TowerPlayer.getPlayers().stream()
                .peek(x->counter.set(1))
                .sorted(Comparator.comparingInt(func).reversed())
                .filter(tp->tp.asOfflinePlayer().getName()!=null)
                .map(tp->String.format("§b#%d §f"+(counter.get()==1?"§l":"")+"%s §e%d %s", counter.getAndIncrement(), tp.asOfflinePlayer().getName(), func.applyAsInt(tp), suffix))
                .limit(3)
                .reduce("", (partialString, element) -> partialString + "\n" + element).substring(1);

        String headerFormat = "§6§l%20s\n";
        String transition = "§f§m                            \n";

        String fullMessage = "\n" +
                String.format(headerFormat, "Top Kills") +
                sort.apply(TowerPlayer::getKills, "") +
                "\n" +
                transition +
                String.format(headerFormat, "Top Damage") +
                sort.apply((p)-> (int) p.getDamage(), "♥") +
                "\n" +
                transition +
                String.format(headerFormat, "Top Deaths") +
                sort.apply(TowerPlayer::getDeaths, "") +
                "\n" +
                transition +
                String.format(headerFormat, "Top Points") +
                sort.apply(TowerPlayer::getPoints, "") +
                "\n" +
                transition;

        Bukkit.broadcastMessage(fullMessage);

    }

}
