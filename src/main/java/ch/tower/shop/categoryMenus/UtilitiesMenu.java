package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.shop.ShopMenu;
import ch.tower.utils.items.Item;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class UtilitiesMenu extends ShopMenu {

    public UtilitiesMenu(String id, JSONApi.JSONReader json)
    {
        super(id, json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(Item i : super.content)
        {
            Item toAdd = i;
            inv.setItem(toAdd.getSlot(), toAdd.toItemStack(toAdd.getCount()));
        }
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, Item item, ClickType click)
    {
        if(player != null && item != null)
        {
            Main.getInstance().getManager().getShopManager().openShop("utilities_"+item.getUid(), player);
        }
        return -1.0;
    }

}
