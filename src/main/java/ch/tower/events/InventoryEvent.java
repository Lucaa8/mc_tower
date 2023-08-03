package ch.tower.events;

import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.managers.ShopMenuManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryEvent implements Listener {

    private final ShopMenuManager shop = Main.getInstance().getManager().getShopManager();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if(e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.CHEST)
            return;
        ItemStack i0 = e.getClickedInventory().getItem(0);
        if(i0 == null)
            return;
        NBTTagApi.NBTItem item = SpigotApi.getNBTTagApi().getNBT(i0);
        if(item.hasTag("id-inv"))
        {
            e.setCancelled(true);
            NBTTagApi.NBTItem clicked = SpigotApi.getNBTTagApi().getNBT(e.getCurrentItem());
            if(clicked.hasTag("UUID"))
            {
                String id = clicked.getString("UUID");
                if(!id.equals("red_glass") && !id.equals("blue_glass"))
                    shop.invClickManager((Player)e.getWhoClicked(), item.getString("id-inv"), id, e.getClick());
            }
        }
    }
}