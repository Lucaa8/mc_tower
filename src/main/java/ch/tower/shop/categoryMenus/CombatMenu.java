package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.shop.ShopMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CombatMenu extends ShopMenu {

    public CombatMenu(JSONApi.JSONReader json)
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
            player.takeMoney(price);
            ItemStack is = prepareItem(item, false);
            removeMultiplesLF(is);
            if(click == ClickType.LEFT) {
                giveItem(player.asPlayer(), is);
                player.takeMoney(price);
            } else
                giveToEnderChest(player, is, price);
        }
        return -1.0;
    }

    private void removeMultiplesLF(ItemStack is)
    {
        if(is != null && is.hasItemMeta() && is.getItemMeta().hasLore())
        {
            List<String> lore = is.getItemMeta().getLore();
            String last = null;
            for(int i=0;i<lore.size();i++)
            {
                if(last==null)
                    last = lore.get(i);
                else
                {
                    if(last.equals("") && lore.get(i).equals(""))
                    {
                        lore.remove(i);
                        break;
                    }
                    last = lore.get(i);
                }
            }
            ItemMeta im = is.getItemMeta();
            im.setLore(lore);
            is.setItemMeta(im);
        }
    }
}
