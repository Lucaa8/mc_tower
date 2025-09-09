package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.managers.GameManager;
import ch.tower.shop.ShopMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ResourcesMenu extends ShopMenu {

    public ResourcesMenu(JSONApi.JSONReader json)
    {
        super(json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(TowerItem i : super.content)
        {
            inv.setItem(i.getSlot(), prepareItem(i, true));
        }
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, TowerItem item, ClickType click)
    {
        double price = super.clicked(player, item, click);
        if(price >= 0.0)
        {
            if(click == ClickType.LEFT)
            {
                player.takeMoney(price);
                item.giveOrDropWithoutNBT(player.asPlayer(), item.getCount());
            } else //SHIFT_LEFT
                giveToEnderChestWithoutNBTs(player, item.toItemStack(item.getCount(), player.asOfflinePlayer()), price);
        }
        return -1.0;
    }

}