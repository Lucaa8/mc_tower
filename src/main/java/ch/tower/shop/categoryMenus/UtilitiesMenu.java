package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.shop.ShopMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class UtilitiesMenu extends ShopMenu {

    public UtilitiesMenu(JSONApi.JSONReader json)
    {
        super(json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(TowerItem i : super.content)
        {
            inv.setItem(i.getSlot(), i.toItemStack(i.getCount()));
        }
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, TowerItem item, ClickType click)
    {
        if(player != null && item != null)
        {
            Main.getInstance().getManager().getShopManager().openShop("utilities_"+item.getUid(), player);
        }
        return -1.0;
    }

}
