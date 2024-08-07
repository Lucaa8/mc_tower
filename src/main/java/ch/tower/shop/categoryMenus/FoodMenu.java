package ch.tower.shop.categoryMenus;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.shop.ShopMenu;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FoodMenu extends ShopMenu {

    public FoodMenu(String id, JSONApi.JSONReader json)
    {
        super(id, json);
    }

    @Override
    public Inventory createInventory(TowerPlayer player)
    {
        Inventory inv = super.createInventory(player);
        for(TowerItem i : super.content)
        {
            ItemStack is = prepareItem(i, true);
            ItemMeta im = is.getItemMeta();
            int itemLevel = Integer.parseInt(i.getUid().split("_")[0]);
            int playerLevel = player.getLevels().getFoodLevel()+1;
            if(playerLevel < itemLevel)
            {
                List<String> lore = im.getLore();
                addLine(lore, lore.size(), "");
                addLine(lore, lore.size(), "§cYou need to unlock the previous upgrade (Level "+itemLevel+") to do buy this one.");
                im.setLore(lore);
                is.setItemMeta(im);
                is.setType(Material.BARRIER);
                is.setAmount(1);
            }
            else if(playerLevel > itemLevel)
            {
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                List<String> lore = im.getLore();
                if(lore != null)
                {
                    lore = new ArrayList<>(lore);
                    lore.set(2, "§cYou already purchased this.");
                    im.setLore(lore);
                }
                is.setItemMeta(im);
                is.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            //i cannot put that here because it will cause the addUnsafeEnchantment to be reset and not taken into account.
            //is.setItemMeta(im);
            inv.setItem(i.getSlot(), is);
        }
        return inv;
    }

    @Override
    public double clicked(TowerPlayer player, TowerItem item, ClickType click)
    {
        double price = super.clicked(player, item, click);
        if(price >= 0.0)
        {
            int itemLevel = Integer.parseInt(item.getUid().split("_")[0]);
            int playerLevel = player.getLevels().getFoodLevel()+1;
            if(itemLevel == playerLevel)
            {
                player.takeMoney(price);
                player.getLevels().addFoodLevel();
                player.giveFood();
                Main.getInstance().getManager().getShopManager().openShop(getId(), player);
            }
            else
            {
                player.asPlayer().sendMessage("§cUnauthorized action: " + (itemLevel>playerLevel ? "You need to unlock the previous upgrade (Level "+itemLevel+") to do buy this one." : "You already bought this food."));
            }
        }
        return -1.0;
    }

    @Nullable
    public TowerItem getItemForLevel(int level)
    {
        for(TowerItem item : super.content)
        {
            if(item.getUid().startsWith(""+level))
            {
                return item;
            }
        }
        return null;
    }
}
