package ch.tower;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.TeamsManager;
import ch.tower.managers.TeamsManager.PlayerTeam;
import ch.tower.shop.ShopMenu;
import ch.tower.shop.categoryMenus.FoodMenu;
import ch.tower.shop.categoryMenus.ToolsMenu;
import ch.tower.utils.Scoreboard.PlayerBoard;
import ch.tower.utils.items.*;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.File;
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
    private double money = 0d;

    private final Levels levels;

    private TowerPlayer(Player player)
    {
        this.player = Bukkit.getOfflinePlayer(player.getUniqueId());
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

    public int addPoint()
    {
        ScoreboardManager.BoardField.POINTS.update(asPlayer(), ++points);
        return points;
    }

    public int getPoints()
    {
        return points;
    }

    public int addKill()
    {
        ScoreboardManager.BoardField.KILLS.update(asPlayer(), ++kills);
        return kills;
    }

    public int getKills()
    {
        return kills;
    }

    public int addAssist()
    {
        ScoreboardManager.BoardField.ASSISTS.update(asPlayer(), ++assists);
        return assists;
    }

    public int getAssists()
    {
        return assists;
    }

    public int addDeath()
    {
        ScoreboardManager.BoardField.DEATHS.update(asPlayer(), ++deaths);
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
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), this.money);
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
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), this.money);
        return money;
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
                    replaceTool(entry.getKey(), NBTTags.getInstance().getNBT(item).setTag("UUID", "current"+entry.getKey()).getBukkitItem());
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
        replaceTool("_food", NBTTags.getInstance().getNBT(item).setTag("UUID", "current_food").getBukkitItem());
    }

    private void replaceTool(String type, ItemStack newItem)
    {
        String uid = "current"+type;
        Player p = asPlayer();
        int index = 0;
        for(ItemStack i : p.getInventory())
        {
            index++;
            NBTTags.NBTItem nbtCurrent = NBTTags.getInstance().getNBT(i);
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

    public PlayerBoard getScoreboard()
    {
        return Main.getInstance().getManager().getScoreboardManager().getBoard(asPlayer());
    }

    public PlayerTeam getTeam()
    {
        return TeamsManager.getPlayerTeam(asPlayer());
    }

    public void updateBoard()
    {
        getScoreboard().updateLines(
                Map.of(
                    ScoreboardManager.BoardField.POINTS.toFormat(), points,
                    ScoreboardManager.BoardField.KILLS.toFormat(), kills,
                    ScoreboardManager.BoardField.ASSISTS.toFormat(), assists,
                    ScoreboardManager.BoardField.DEATHS.toFormat(), deaths,
                    ScoreboardManager.BoardField.MONEY.toFormat(), money
                )
        );
    }
}
