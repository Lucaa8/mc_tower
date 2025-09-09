package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.managers.GameManager;
import ch.tower.managers.TeamsManager;
import ch.tower.shop.ShopMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class BlocksMenu extends ShopMenu
{
    public BlocksMenu(JSONApi.JSONReader json)
    {
        super(json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(TowerItem i : super.content)
        {
            TowerItem toAdd = cloneColouredGlass(i, player.getTeam());
            inv.setItem(toAdd.getSlot(), prepareItem(toAdd, true));
        }
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, TowerItem item, ClickType click)
    {
        double price = super.clicked(player, item, click);
        if(price >= 0.0)
        {
            TowerItem toGive = cloneColouredGlass(item, player.getTeam());
            if(click == ClickType.LEFT)
            {
                player.takeMoney(price);
                toGive.giveOrDropWithoutNBT(player.asPlayer(), toGive.getCount());
            } else
                giveToEnderChestWithoutNBTs(player, toGive.toItemStack(toGive.getCount(), player.asOfflinePlayer()), price);
        }
        return -1.0;
    }

    public static TowerItem cloneColouredGlass(TowerItem whiteGlass, TeamsManager.PlayerTeam team)
    {
        if(whiteGlass.getUid() != null && whiteGlass.getUid().equals("glass"))
        {
            TowerItem clone = whiteGlass.clone();
            clone.setMaterial(team==TeamsManager.PlayerTeam.BLUE?Material.BLUE_STAINED_GLASS:Material.RED_STAINED_GLASS);
            return clone;
        }
        return whiteGlass;
    }

}
