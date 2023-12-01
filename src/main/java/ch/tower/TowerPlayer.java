package ch.tower;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.Api.ScoreboardAPI;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.items.ArmorEquipment;
import ch.tower.items.Item;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager.PlaceholderHelper;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.TeamsManager;
import ch.tower.managers.TeamsManager.PlayerTeam;
import ch.tower.shop.ShopMenu;
import ch.tower.shop.categoryMenus.FoodMenu;
import ch.tower.shop.categoryMenus.ToolsMenu;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.*;

public class TowerPlayer
{

    public static Map<String, ItemStack> defaultTools = new HashMap<>();

    static {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(ToolsMenu.DEFAULT_TOOLS_FILE);
        for(Object o : r.getArray("Items"))
        {
            JSONObject j = (JSONObject)o;
            Item item = Item.fromJson((JSONObject)j.get("Item"));
            if(item!=null)
            {
                defaultTools.put("-1_"+(String)j.get("Type"), item.toItemStack(item.getCount()));
            }
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
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "§4Come back later as a spectator.", new Date(System.currentTimeMillis()+10000), "Server");
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
    private int points = 0;
    private int kills = 0;
    private int assists = 0;
    private int deaths = 0;
    private double money = 30d;

    private final Levels levels;

    //Can return the placeholder value for each player stat (like kills, etc...)
    //Can be public because final and unalterable (fields in PlayerHelper are also final)
    public final PlaceholderHelper.PlayerHelper boardHelder;

    private TowerPlayer lastDamagedBy;
    private long lastDamagedAt;

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
                    replaceTool(entry.getKey(), item);
                }
            }
            else
            {
                Item i = tools.getItem(itemUid);
                if(i != null)
                {
                    ItemStack item = tools.prepareItem(i, false);
                    replaceTool(entry.getKey(), SpigotApi.getNBTTagApi().getNBT(item).setTag("UUID", "current"+entry.getKey()).getBukkitItem());
                }
            }
        }
    }

    public void giveFood()
    {
        FoodMenu food = (FoodMenu) Main.getInstance().getManager().getShopManager().getShop("utilities_food");
        if(food==null) return; //never the case but better be safe than sorry
        Item current = food.getItemForLevel(getLevels().getFoodLevel());
        if(current==null) return;
        ItemStack item = current.toItemStack(current.getCount());
        replaceTool("_food", SpigotApi.getNBTTagApi().getNBT(item).setTag("UUID", "current_food").getBukkitItem());
    }

    private void replaceTool(String type, ItemStack newItem)
    {
        String uid = "current"+type;
        Player p = asPlayer();
        int index = 0;
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

    public void damage(@Nullable TowerPlayer attacker)
    {
        this.lastDamagedBy = attacker;
        if(attacker!=null)
            this.lastDamagedAt = System.currentTimeMillis();
    }

    @Nullable
    public TowerPlayer getLastDamagedBy()
    {
        if(this.lastDamagedBy==null || (System.currentTimeMillis()-this.lastDamagedAt)>(GameManager.ConfigField.LAST_ATTACKER_TIMER.get()*1000L))
            this.lastDamagedBy = null;
        return this.lastDamagedBy;
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
        return TeamsManager.getPlayerTeam(asPlayer());
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

}
