package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.TowerPlayer;
import ch.tower.managers.TeamsManager;
import ch.tower.shop.ShopMenu;
import ch.tower.utils.items.Item;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public class BlocksMenu extends ShopMenu
{
    public BlocksMenu(String id, JSONApi.JSONReader json)
    {
        super(id, json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(Item i : super.content)
        {
            Item toAdd = cloneColouredGlass(i, player.getTeam());
            inv.setItem(toAdd.getSlot(), prepareItem(toAdd, true));
        }
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, Item item, ClickType click)
    {
        double price = super.clicked(player, item, click);
        if(price >= 0.0)
        {
            player.takeMoney(price);
            Item toGive = cloneColouredGlass(item, player.getTeam());
            toGive.giveOrDrop(player.asPlayer(), toGive.getCount());
        }
        return -1.0;
    }

    private Item cloneColouredGlass(Item whiteGlass, TeamsManager.PlayerTeam team)
    {
        if(whiteGlass.getUid().equals("glass"))
        {
            Item clone = whiteGlass.clone();
            clone.setMaterial(team==TeamsManager.PlayerTeam.BLUE?Material.BLUE_STAINED_GLASS:Material.RED_STAINED_GLASS);
            return clone;
        }
        return whiteGlass;
    }

}
