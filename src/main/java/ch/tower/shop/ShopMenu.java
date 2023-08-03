package ch.tower.shop;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.items.Enchant;
import ch.tower.items.Item;
import ch.tower.items.ItemBuilder;
import ch.tower.managers.TeamsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ShopMenu implements Shop {

    public record ItemPrice(String itemUid, double priceOne, double priceDefinitive) { }

    public enum PriceType {
        PRICE_ONE, PRICE_DEFINITIVE;

        public double get(ItemPrice itemPrice)
        {
            return this == PRICE_ONE ? itemPrice.priceOne() : itemPrice.priceDefinitive();
        }
    }

    private static final ItemStack redGlass = new ItemBuilder().setUid("red_glass").setMaterial(Material.RED_STAINED_GLASS_PANE).setName("§cRed Team")
            .createItem().toItemStack(1);
    private static final ItemStack blueGlass = new ItemBuilder().setUid("blue_glass").setMaterial(Material.BLUE_STAINED_GLASS_PANE).setName("§9Blue Team")
            .createItem().toItemStack(1);

    protected String id;
    protected String name;
    protected int size;
    protected List<Item> content;
    private List<ItemPrice> prices;
    protected int backSlot;

    public ShopMenu(String id, JSONApi.JSONReader json)
    {
        this.id = id;
        JSONApi.JSONReader shop = json.getJson("Shop");
        this.name = shop.getString("Name");
        this.size = shop.getInt("Size");
        if(this.size < 7 || this.size > 28 || this.size%7!=0)
        {
            this.size = -1;
            System.err.println("Invalid inventory size ("+this.size+") for " + this.id + " inventory.");
            System.err.println("1 - Must be bigger or equals to 7 and less or equals to 28.");
            System.err.println("2 - Must be a multiple of 7.");
            return;
        }
        this.size += this.size*(2f/7) + 18;
        int back = shop.getInt("Back");
        this.backSlot = back != -1 ? this.size-(10-back) : -1;
        this.content = ((List<Object>)shop.getArray("Content")).stream()
                .filter(JSONObject.class::isInstance)
                .map(JSONObject.class::cast).map(Item::fromJson).collect(Collectors.toList());
        this.prices = ((List<Object>)json.getArray("Prices")).stream()
                .filter(JSONObject.class::isInstance).map(JSONObject.class::cast).map(SpigotApi.getJSONApi()::getReader)
                .map(r->new ItemPrice(r.getString("Item"), r.c("Price-One") ? r.getDouble("Price-One") : -1.0, r.c("Price-Definitive") ? r.getDouble("Price-Definitive") : -1.0))
                .collect(Collectors.toList());
    }

    @Nullable
    public Item getItem(String id)
    {
        for(Item i : this.content)
        {
            if(i.getUid().equals(id))
                return i;
        }
        return null;
    }

    /**
     * Get the price of the given item
     * @param id the item's unique id for this shop
     * @param type One time use ou Definitive buy
     * @return -1.0 if the item doesn't exist in this shop OR if this item cant be bought this way (type). Otherwise, return the item's price.
     */
    public double getPrice(String id, PriceType type)
    {
        ItemPrice price = getPrice(id);
        return price == null ? -1.0 : type.get(price);
    }

    /**
     * See {@link #getPrice(String, PriceType)}
     * @param id the item's unique id for this shop.
     * @return null if the item wasn't found in this shop.
     */
    @Nullable
    public ItemPrice getPrice(String id)
    {
        for(ItemPrice price : this.prices)
        {
            if(price.itemUid().equals(id))
            {
                return price;
            }
        }
        return null;
    }

    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = Bukkit.createInventory(null, this.size, this.name);
        ItemStack border = player.getTeam()==TeamsManager.PlayerTeam.BLUE?blueGlass:redGlass;
        inv.setItem(0, SpigotApi.getNBTTagApi().getNBT(border).setTag("id-inv", this.getId()).getBukkitItem());
        for(int i=1;i<this.size;i++)
        {
            if(i<10 || i>(this.size-9) || i%9==0 || i%9==8)
            {
                inv.setItem(i, player.getTeam()==TeamsManager.PlayerTeam.BLUE?blueGlass:redGlass);
            }
        }
        if(this.backSlot != -1)
            inv.setItem(this.backSlot, new ItemBuilder().setMaterial(Material.OAK_DOOR).setName("§cBack").setUid("back").createItem().toItemStack(1));
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, Item item, ClickType click)
    {
        if(player != null && item != null)
        {
            ItemPrice price = getPrice(item.getUid());
            if(price != null)
            {
                if(click == ClickType.LEFT && price.priceOne() >= 0.0 && player.getMoney() >= price.priceOne())
                {
                    return price.priceOne();
                }
                else if(click == ClickType.RIGHT && price.priceDefinitive() >= 0.0 && player.getMoney() >= price.priceDefinitive())
                {
                    return price.priceDefinitive();
                }
            }
        }
        //the only way we can come here is that the back door has been clicked (if item is null THIS method won't be called at all) See ShopMenuManager#invClickManager
        if(player != null && item == null)
        {
            Main.getInstance().getManager().getShopManager().openShop("utilities", player);
        }
        return -1.0;
    }

    public void giveItem(Player player, ItemStack item)
    {
        Map<Integer, ItemStack> full = player.getInventory().addItem(item);
        if (!full.isEmpty()) {
            Location loc = player.getLocation();
            for(Map.Entry<Integer, ItemStack> extra : full.entrySet()) {
                player.getWorld().dropItem(loc, extra.getValue());
            }
        }
    }

    public ItemStack prepareItem(Item item, boolean addPrices)
    {
        ItemPrice price = getPrice(item.getUid());
        ItemStack is = item.toItemStack(item.getCount());
        if(addPrices)
        {
            addPrices(is, price);
        }
        if(item.getEnchantList().size()>0 && !item.getEnchantList().get(0).getEnchantment().equals(Enchantment.LUCK))
        {
            addEnchants(is, item.getEnchantList());
        }
        return is;
    }

    protected void addPrices(ItemStack item, ItemPrice price)
    {
        if(item==null || item.getItemMeta()==null || price==null)
            return;
        List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        boolean isPotion = item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION;
        boolean isLuck = isPotion && item.hasItemMeta() && item.getItemMeta().hasLore();
        int index = isLuck ? item.getItemMeta().getLore().size() : 2;
        if(index == 2 && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().get(0).equals(""))
        {
            index--;
        }
        if(price.priceOne() >= 0.0)
        {
            if(isPotion)
            {
                addLine(lore, index++, "");
            }
            addLine(lore, index++, "§6"+price.priceOne()+" coins §efor a one time use (left-click)");
        }
        if(price.priceDefinitive() >= 0.0)
        {
            addLine(lore, index++, "§6"+price.priceDefinitive()+" coins §efor a definitive upgrade (right-click)");
            addLine(lore, index++, "");
            addLine(lore, index++, "§e§oDefinitive upgrade: You respawn with this item");
        }
        if(index > 2 && !isPotion)
        {
            addLine(lore, index, "");
        }
        if(item.hasItemMeta() && item.getItemMeta().hasAttributeModifiers())
        {
            addLine(lore, lore.size(), "§a§o1.8 PVP enabled");
            addLine(lore, lore.size(), "");
        }
        ItemMeta im = item.getItemMeta();
        im.setLore(lore);
        item.setItemMeta(im);
    }

    protected void addEnchants(ItemStack item, List<Enchant> enchantList)
    {
        if(item==null || item.getItemMeta()==null || enchantList==null || enchantList.size()<1)
            return;
        int index = item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().get(0).equals("") ? 1 : 2;
        List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        for(Enchant ench : enchantList)
        {
            addLine(lore, index++, ench.asMinecraftEnchantment());
        }
        addLine(lore, index, "");
        ItemMeta im = item.getItemMeta();
        im.setLore(lore);
        item.setItemMeta(im);
    }

    protected void addLine(List<String> lore, int index, String line)
    {
        if(lore==null)
            return;
        if(index >= lore.size())
        {
            lore.add(line);
        }
        else
        {
            lore.add(index, line);
        }
    }

}
