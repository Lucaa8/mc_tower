package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.shop.ShopMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class QuickMenu extends ShopMenu {

    public QuickMenu(JSONApi.JSONReader json) {
        super(json);

        for(int i = 0; i<super.content.size(); i++)
        {
            TowerItem item = super.content.get(i);
            if(item.getUid() == null || !item.getUid().contains("."))
            {
                continue;
            }
            String[] info = item.getUid().split("\\.");
            ShopMenu menu = Main.getInstance().getManager().getShopManager().getShop(info[0]);
            if(menu == null)
            {
                Logger.warn("Quick Shop: Tried to dynamically add an item from shop " + info[0] + " but this shop does not exist.");
                continue;
            }
            TowerItem shopItem = menu.getItem(info[1]);
            if(shopItem == null)
            {
                Logger.warn("Quick Shop: Tried to dynamically add an item " + item.getUid() + " but it does not exist in shop " + info[0]);
                continue;
            }
            ItemPrice price = menu.getPrice(info[1]);
            if(price == null)
            {
                Logger.warn("Quick Shop: Tried to dynamically add an item " + item.getUid() + " in shop " + info[0] + " but price has been not found.");
                continue;
            }
            shopItem = shopItem.clone();
            shopItem.setSlot(item.getFakeSlot());
            super.content.set(i, shopItem);
            super.prices.add(price);
        }

    }

    //Just a copy and paste from BlocksMenu. Overall, bad implementation for the shop system.

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(TowerItem i : super.content)
        {
            TowerItem toAdd = BlocksMenu.cloneColouredGlass(i, player.getTeam());
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
            player.takeMoney(price);
            if(item.getUid() != null && item.getUid().equals("glass"))
            {
                item = BlocksMenu.cloneColouredGlass(item, player.getTeam());
            }
            ItemStack toGive = item.toItemStack(item.getCount(), player.asOfflinePlayer());
            if(toGive.getType().isBlock())
            {
                NBTTagApi.NBTItem toGiveNBT = SpigotApi.getNBTTagApi().getNBT(toGive);
                for(String tag : toGiveNBT.getTags().keySet()) {
                    toGiveNBT.removeTag(tag);
                }
                toGive = toGiveNBT.getBukkitItem();
            }
            if(click == ClickType.LEFT)
            {
                player.takeMoney(price);
                giveItem(player.asPlayer(), toGive);
            } else //SHIFT_LEFT
                giveToEnderChest(player, toGive, price);
        }
        return -1.0;
    }

}
