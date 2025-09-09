package ch.tower;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.Api.ScoreboardAPI;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.items.ArmorEquipment;
import ch.tower.items.TowerItem;
import ch.tower.items.WeaponStatistics;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.ScoreboardManager.PlaceholderHelper;
import ch.tower.managers.TeamsManager;
import ch.tower.managers.TeamsManager.PlayerTeam;
import ch.tower.shop.ShopMenu;
import ch.tower.shop.categoryMenus.FoodMenu;
import ch.tower.shop.categoryMenus.ToolsMenu;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TowerPlayer
{

    public record Damage(TowerPlayer damagedBy, long damagedAt)
    {

        public Damage(TowerPlayer damagedByNow)
        {
            this(damagedByNow, System.currentTimeMillis());
        }

        public boolean isDamageStillValid()
        {
            return (System.currentTimeMillis()-this.damagedAt)<(GameManager.ConfigField.LAST_ATTACKER_TIMER.get()*1000L);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Damage damage)) return false;
            return damagedAt == damage.damagedAt && damagedBy.equals(damage.damagedBy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(damagedBy, damagedAt);
        }

        @Override
        public String toString() {
            return "Damage{" +
                    "damagedBy=" + damagedBy.player +
                    ", damagedAt=" + damagedAt +
                    '}';
        }

    }

    public static Map<String, ItemStack> defaultTools = new HashMap<>();

    static {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(ToolsMenu.DEFAULT_TOOLS_FILE);
        for(Object o : r.getArray("Items"))
        {
            JSONObject j = (JSONObject)o;
            TowerItem item = TowerItem.fromJson((JSONObject)j.get("Item"));
            defaultTools.put("-1_"+j.get("Type"), item.toItemStack(item.getCount()));
        }
    }

    private static final ArrayList<TowerPlayer> players = new ArrayList<>();

    //Can register only once a game. When state changes from wait to game.
    public static boolean registerPlayers()
    {
        if(players.size()!=0)
            return false;
        for(Player player : Bukkit.getOnlinePlayers())
        {
            PlayerTeam pt = TeamsManager.getPlayerTeam(player);
            if(pt == PlayerTeam.RED || pt == PlayerTeam.BLUE)
            {
                players.add(new TowerPlayer(player));
            }
            else
            {
                //The player isn't in a team when the game is starting: we kick it and then he can rejoin a little bit later as a spectator (gamestate GAME)
                BanList<PlayerProfile> list = Bukkit.getBanList(BanList.Type.PROFILE);
                list.addBan(player.getPlayerProfile(), "§4Come back later as a spectator.", new Date(System.currentTimeMillis()+10000), "Server");
                player.kickPlayer("§4Come back later as a spectator.");
            }
        }
        return true;
    }

    @Nullable
    public static TowerPlayer getPlayer(OfflinePlayer player)
    {
        if(player == null)
            return null;
        for(TowerPlayer tp : players)
        {
            if(tp.player.getUniqueId().equals(player.getUniqueId()))
                return tp;
        }
        return null;
    }

    public static void removePlayer(TowerPlayer player)
    {
        players.remove(player);
        if(player.asOfflinePlayer().isOnline())
        {
            player.asPlayer().kickPlayer("§cYou have been removed from the match");
        }
    }

    public static List<TowerPlayer> getPlayers()
    {
        return new ArrayList<>(players);
    }

    public static List<TowerPlayer> getPlayersInTeam(PlayerTeam team)
    {
        if(team==null)
            return new ArrayList<>();
        return players.stream().filter(p->p.getTeam()==team).toList();
    }

    public static class Levels implements Iterable<Map.Entry<String, Integer>>
    {

        private final Map<String, Integer> levels = new HashMap<>()
        {{
            put("_sword", -1);
            put("_pickaxe", -1);
            put("_axe", -1);
            put("_armor", -1);
            put("_bow", -1);
        }};

        private int food = -1;

        public void addLevel(String itemName)
        {
            if(itemName.contains("_"))
            {
                String key = "_"+itemName.split("_")[1];
                levels.replace(key, levels.get(key)+1);
            }
        }

        public void addFoodLevel()
        {
            this.food++;
        }

        public int getFoodLevel()
        {
            return this.food;
        }

        @Override
        public Iterator<Map.Entry<String, Integer>> iterator() {
            return this.levels.entrySet().iterator();
        }
    }

    private final OfflinePlayer player;
    private final WeaponStatistics weaponDamage = new WeaponStatistics();
    private double damage = 0d;
    private int points = 0;
    private int kills = 0;
    private int assists = 0;
    private int deaths = 0;
    private double money = 0d;

    private final Levels levels;

    //Can return the placeholder value for each player stat (like kills, etc...)
    //Can be public because final and unalterable (fields in PlayerHelper are also final)
    public final PlaceholderHelper.PlayerHelper boardHelder;

    private final List<Damage> lastDamagedBy = new ArrayList<>(4);
    private TowerPlayer lastBurntBy;
    private int burnTicksLeft = 0;
    private boolean isImmune = false; //OnDeath, players are immune x seconds to avoid spawn killing

    private String abandonTeam;
    private BukkitTask abandonTask;

    private TowerPlayer(Player player)
    {
        this.player = Bukkit.getOfflinePlayer(player.getUniqueId());
        this.boardHelder = PlaceholderHelper.getPlayerHelper(this);
        this.levels = new Levels();
    }

    public OfflinePlayer asOfflinePlayer()
    {
        return player;
    }

    public Player asPlayer()
    {
        return Bukkit.getPlayer(player.getUniqueId());
    }

    private BukkitTask cancelDisplayBarTextTask = null;
    public void displayBarText(String text, long ticks)
    {
        Player p = asPlayer();

        if(p != null && p.isOnline())
        {
            //Avoid clearing the next action bar too fast, remove the last scheduled reset and recreate one with the new given ticks
            if(cancelDisplayBarTextTask != null)
            {
                cancelDisplayBarTextTask.cancel();
            }

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(text));

            cancelDisplayBarTextTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->{
                if(p.isOnline())
                {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                }
                cancelDisplayBarTextTask = null;
            }, ticks);

        }

    }

    @Nullable
    public PlayerTeam getAbandoningTeam()
    {
        try {
            return PlayerTeam.valueOf(this.abandonTeam);
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public PlayerTeam startAbandon()
    {
        PlayerTeam team = getTeam();
        if(team != null)
        {
            int abandonSeconds = GameManager.ConfigField.ABANDON_AFTER.get();
            this.abandonTeam = team.name();
            this.abandonTask = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), ()->Main.getInstance().getManager().abandon(this), abandonSeconds*20L); //Transform x seconds to ticks
            String unit = abandonSeconds < 60 ? String.format("%d seconds", abandonSeconds) : String.format("%d minutes", (int)(abandonSeconds/60));
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_ABANDON_1", team.getColorCode()+asOfflinePlayer().getName(), unit));
        }
        return team;
    }

    @Nullable
    public PlayerTeam stopAbandon()
    {
        PlayerTeam team = getAbandoningTeam();
        this.abandonTeam = null;
        this.abandonTask.cancel();
        this.abandonTask = null;
        if(team != null)
        {
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_ABANDON_3", team.getColorCode()+asOfflinePlayer().getName()));
        }
        return team;
    }

    /**
     * Attempts to add the given ItemStack into the player's ender chest (if the player is online).
     * <p>
     * Behavior:
     * <ul>
     *   <li>If the item is {@code null} or of type AIR, nothing is added.</li>
     *   <li>If the player is not online, nothing is added.</li>
     *   <li>If a slot already contains a similar item (same type and matching metadata)
     *       and has enough space to hold the entire stack, the item is merged into that slot.</li>
     *   <li>Otherwise, if there is an empty slot available, the item is placed there.</li>
     *   <li>If no suitable slot exists, the item is not added.</li>
     * </ul>
     *
     * <p><b>Note:</b> This method only succeeds if the full ItemStack can fit in the ender chest.
     * Partial insertion is not performed.</p>
     *
     * @param item the ItemStack to add (with {@code amount} ≤ the maximum stack size allowed
     *             for its material)
     * @return {@code true} if the full stack was added, {@code false} otherwise
     */
    //An enhancement of this method could be to add every bit of space in each stack of the same ite
    //Because currently adding is not optimized, 2 stacks of oak_planks x 48 in the enderchest could hold 32x new oak_planks
    public boolean addToEnderChest(@Nullable ItemStack item)
    {
        if(item == null || item.getType() == Material.AIR)
        {
            return false;
        }
        Player p =  asPlayer();
        if(p == null || !p.isOnline())
        {
            return false;
        }
        for(ItemStack ecItem : p.getEnderChest().getContents())
        {
            //UUID tag is not taken in account... not sure it's a nice idea but yeah, I need to move on.
            if(ecItem == null || !ecItem.isSimilar(item))
            {
                continue;
            }
            //Checks if the new itemstack's count can fit in this slot
            if(ecItem.getMaxStackSize() - ecItem.getAmount() - item.getAmount() < 0)
            {
                continue;
            }
            ecItem.setAmount(ecItem.getAmount() + item.getAmount());
            return true;
        }
        if(p.getEnderChest().firstEmpty() == -1)
            return false;
        p.getEnderChest().addItem(item);
        return true;
    }

    public void setImmune(double immuneTime)
    {
        this.isImmune = true;
        Player p = asPlayer();
        if(p != null && p.isOnline())
        {
            p.setInvulnerable(true);
            displayBarText("§4§lImmune during " + immuneTime + " seconds.", (long)immuneTime*20);
        }
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->{
            this.isImmune = false;
            if(p != null && p.isOnline())
            {
                p.setInvulnerable(false);
            }
        }, (long)immuneTime*20);
    }

    public boolean isImmune()
    {
        return this.isImmune;
    }

    public int addPoint()
    {
        points++;
        ScoreboardManager.BoardField.POINTS.update(asPlayer(), boardHelder.getPoints());
        return points;
    }

    public int getPoints()
    {
        return points;
    }

    public double addDamageWithWeapon(EntityDamageByEntityEvent e)
    {
        weaponDamage.addDamageWith(e);
        //fix bug with current_item (add another ID on the item ?)
        return addDamage(e.getFinalDamage());
    }

    public double addDamage(double damage)
    {
        this.damage += damage;
        ScoreboardManager.BoardField.DAMAGE.update(asPlayer(), boardHelder.getDamage());
        return this.damage;
    }

    public double getDamage()
    {
        return damage;
    }

    public int addKill()
    {
        kills++;
        ScoreboardManager.BoardField.KILLS.update(asPlayer(), boardHelder.getKills());
        return kills;
    }

    public int getKills()
    {
        return kills;
    }

    public int addAssist()
    {
        assists++;
        ScoreboardManager.BoardField.ASSISTS.update(asPlayer(), boardHelder.getAssists());
        return assists;
    }

    public int getAssists()
    {
        return assists;
    }

    public int addDeath()
    {
        deaths++;
        ScoreboardManager.BoardField.DEATHS.update(asPlayer(), boardHelder.getDeaths());
        return deaths;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public double getMoney()
    {
        return money;
    }

    /**
     * Checks if a player has enough money to buy something
     * @param price the cost of the item he wants to buy
     * @return 0d if he <strong>CAN</strong> buy this item. <p>If anything else than 0d then it's the amount of money he needs to get before he can afford this item. <p>Example: I have 8$ and I want to buy a 10$ Sword then {@link #canBuy(double)} returns me 2.0$
     */
    public double canBuy(double price)
    {
        double delta = money - price;
        if(delta < 0)
        {
            return delta*-1;
        }
        return 0d;
    }

    /**
     * Adds the specified money amount to this player's bank account
     * @param money The amount of money to add
     * @return The new total amount of this player's bank account <strong>after</strong> the money got added.
     */
    public double giveMoney(double money)
    {
        this.money += money;
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), boardHelder.getMoney());
        return money;
    }

    /**
     * Removes the specified money amount to this player's bank account. Also check if he can afford the price.
     * @param money The amount of money to remove
     * @return The new total amount of this player's bank account <strong>after</strong> the money got removed. <p>If he cant afford the price then returns -1d
     */
    public double takeMoney(double money)
    {
        if(canBuy(money)!=0d)
        {
            return -1d;
        }
        this.money -= money;
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), boardHelder.getMoney());
        return money;
    }

    public void setMoney(double money){
        this.money = money;
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), boardHelder.getMoney());
    }

    public Levels getLevels()
    {
        return this.levels;
    }

    public void giveTools()
    {
        ShopMenu tools = Main.getInstance().getManager().getShopManager().getShop("tools");
        if(tools==null) return; //never the case but better be safe than sorry
        for(Map.Entry<String, Integer> entry : getLevels())
        {
            String itemUid = entry.getValue()+entry.getKey();
            if(entry.getKey().contains("_armor"))
            {
                ArmorEquipment.equip(this, itemUid);
                continue;
            }

            if(entry.getValue() == -1)
            {
                if(defaultTools.containsKey(itemUid))
                {
                    ItemStack item = defaultTools.get(itemUid);
                    replaceTool(entry.getKey(), item, itemUid);
                }
            }
            else
            {
                TowerItem i = tools.getItem(itemUid);
                if(i != null)
                {
                    ItemStack item = tools.prepareItem(i, false);
                    replaceTool(entry.getKey(), SpigotApi.getNBTTagApi().getNBT(item).setTag("UUID", "current"+entry.getKey()).getBukkitItem(), itemUid);
                }
            }
        }
    }

    public void giveFood()
    {
        FoodMenu food = (FoodMenu) Main.getInstance().getManager().getShopManager().getShop("utilities_food");
        if(food==null) return; //never the case but better be safe than sorry
        TowerItem current = food.getItemForLevel(getLevels().getFoodLevel());
        if(current==null) return;
        ItemStack item = current.toItemStack(current.getCount());
        replaceTool("_food", SpigotApi.getNBTTagApi().getNBT(item).setTag("UUID", "current_food").getBukkitItem(), null);
    }

    private void replaceTool(String type, ItemStack newItem, String realId)
    {
        String uid = "current"+type;
        Player p = asPlayer();
        int index = 0;
        if(realId != null) //null if food (or others), non-null if tools (axe, sword.. damageable items)
        {
            //for some weapons (definitive upgrades levels) the item's uuid become "current_sword", "current_axe", etc..
            //this is to distinct temporary buy vs definitive buy (to know when to drop item on death for example)
            //with this, it becomes impossible to track damage statistics for each weapon. I need somehow a way to keep track of which tool is used.
            //this is why I add "WeaponID" which is the real id (e.g. 0_sword, 1_axe, ... and not current_sword)
            newItem = SpigotApi.getNBTTagApi().getNBT(newItem).setTag("WeaponID", realId).getBukkitItem();
        }

        for(ItemStack i : p.getInventory())
        {
            index++;
            NBTTagApi.NBTItem nbtCurrent = SpigotApi.getNBTTagApi().getNBT(i);
            if(!nbtCurrent.hasTag("UUID") || !nbtCurrent.getString("UUID").equals(uid))
                continue;
            p.getInventory().setItem(index-1, newItem);
            return;
        }
        if(!p.getInventory().addItem(newItem).isEmpty())
        {
            p.sendMessage("§cYour inventory was full so your " + (type.contains("_") ? type.substring(1) : type) + " couldn't be delivered. You will get it at your next respawn.");
        }
    }

    public void damageFire(@Nullable TowerPlayer attacker, int duration)
    {
        this.lastBurntBy = attacker;
        this.burnTicksLeft = duration;
    }

    public void tickFire()
    {
        this.burnTicksLeft -= 1;
        if(this.burnTicksLeft < 1)
        {
            //less than 20 ticks of fire, it means the player will stop burning soon and the EntityDamageEvent wont be called.
            //We need to tell the TowerPlayer#getLastBurntBy method that this player stopped burning but in the same time we need to return one last time the last burn attacker for the DeathEvent
            //Look at the getLastBurntBy method, when 1 tick left (or less) we return the last burn attacker and then reset it.
            this.burnTicksLeft = 1;
        }
    }

    @Nullable
    public TowerPlayer getLastBurntBy()
    {
        //See TowerPlayer#tickFire to understand why 1
        if(burnTicksLeft > 1)
            return this.lastBurntBy;
        //Save the OfflinePlayer instance if existing to get back the TowerPlayer after lastBurntBy has been set to null (will work only once, for DeathEvent on last tick)
        OfflinePlayer lastBurnt = this.lastBurntBy != null ? this.lastBurntBy.asOfflinePlayer() : null;
        this.burnTicksLeft = 0;
        this.lastBurntBy = null;
        return TowerPlayer.getPlayer(lastBurnt);
    }

    /**
     * @param attacker must be null ONLY clear the list when this player is dead (reset the last damaged and assists)
     */
    public void damage(@Nullable TowerPlayer attacker)
    {
        if(attacker == null)
        {
            this.lastDamagedBy.clear();
            return;
        }
        Damage existing = null;
        for(Damage d : this.lastDamagedBy)
        {
            if(d.damagedBy.player.getUniqueId().equals(attacker.player.getUniqueId()))
            {
                existing = d;
            }
        }
        if(existing != null)
            this.lastDamagedBy.remove(existing);
        this.lastDamagedBy.add(0, new Damage(attacker)); //set the attacker (last damager) in position 0.
    }

    @Nullable
    public TowerPlayer getLastDamagedBy()
    {
        if(this.lastDamagedBy.size() == 0)
            return null;
        Damage d = this.lastDamagedBy.get(0); //the last damager will always be added at tne position 0.
        if(!d.isDamageStillValid())
        {
            this.lastDamagedBy.clear(); //If the last damager is invalid then all the OLDER assists wont be valid too so we can clear the list.
            return null;
        }
        return d.damagedBy();
    }

    @Nonnull
    public List<TowerPlayer> getLastAssistedBy(boolean addBurnDamage)
    {
        if(this.lastDamagedBy.size() <= 1) //the position 0 of the array is the last damager (the actual killer if called onDeath)
            return new ArrayList<>();
        List<TowerPlayer> assists = this.lastDamagedBy.stream().skip(1).filter(Damage::isDamageStillValid).map(Damage::damagedBy).collect(Collectors.toCollection(ArrayList::new));
        TowerPlayer lastBurn = getLastBurntBy();
        if(addBurnDamage && lastBurn != null && !lastBurn.equals(getLastDamagedBy()) && !assists.contains(lastBurn))
        {
            assists.add(lastBurn);
        }
        return assists;
    }

    @Nullable
    public ScoreboardAPI.PlayerScoreboard getScoreboard()
    {
        if(player.isOnline()){
            return SpigotApi.getScoreboardApi().getScoreboard(asPlayer());
        }
        return null;
    }

    @Nullable
    public PlayerTeam getTeam()
    {
        PlayerTeam team = TeamsManager.getPlayerTeam(asPlayer());
        if(team == null)
        {
            team = getAbandoningTeam();
        }
        return team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TowerPlayer that)) return false;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }

    @Override
    public String toString() {
        return "TowerPlayer{" +
                "player=" + player +
                ", damage=" + damage +
                ", points=" + points +
                ", kills=" + kills +
                ", assists=" + assists +
                ", deaths=" + deaths +
                ", money=" + money +
                ", lastDamagedBy=" + lastDamagedBy +
                ", lastBurntBy=" + lastBurntBy +
                ", isImmune=" + isImmune +
                '}';
    }
}
