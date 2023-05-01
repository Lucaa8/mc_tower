package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.shop.ShopMenu;
import ch.tower.utils.items.Item;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Map;

public class ToolsMenu extends ShopMenu {

    public static final File DEFAULT_TOOLS_FILE = new File(Main.getInstance().getDataFolder(), "default_items.json");
    public static final File ARMOR_FILE = new File(Main.getInstance().getDataFolder(), "armor.json");

    public ToolsMenu(String id, JSONApi.JSONReader json)
    {
        super(id, json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);

        for(Map.Entry<String, Integer> entry : player.getLevels())
        {
            String itemUid = (entry.getValue()+1)+entry.getKey();
            Item toAdd = getItem(itemUid);
            if(toAdd == null)
            {
                toAdd = getItem("max_"+itemUid.split("_")[1]);
                inv.setItem(toAdd.getSlot(), toAdd.toItemStack(1));
            }
            else
            {
                inv.setItem(toAdd.getSlot(), prepareItem(toAdd, true));
            }
        }

        for(Item i : super.content)
        {
            if(inv.getItem(i.getSlot())==null)
            {
                inv.setItem(i.getSlot(), prepareItem(i, true));
            }
        }

        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, Item item, ClickType click)
    {
        double price = super.clicked(player, item, click);
        if(price >= 0.0 && !item.getUid().startsWith("max_"))
        {
            player.takeMoney(price);
            if(click == ClickType.RIGHT)
            {
                player.getLevels().addLevel(item.getUid());
                player.giveTools();
                Main.getInstance().getManager().getShopManager().openShop(getId(), player);
            }
            else
            {
                if(item.getLore() != null && item.getLore().size() > 3)
                {
                    Item cloned = item.clone();
                    cloned.getLore().add(2, "§e§oTemporary Item");
                    cloned.getLore().add(3, "");
                    giveItem(player.asPlayer(), prepareItem(cloned, false));
                }
                else
                {
                    item.giveOrDrop(player.asPlayer(), item.getCount());
                }
            }
        }
        return -1.0;
    }

}
