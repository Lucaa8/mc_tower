package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NPCApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.items.TowerItem;
import ch.tower.shop.Shop;
import ch.tower.shop.ShopMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopMenuManager {

    public static final File SHOP_FILE = new File(Main.getInstance().getDataFolder(), "shop.json");

    private final List<ShopMenu> shops;

    public ShopMenuManager()
    {
        this.shops = new ArrayList<>();
    }

    public void loadShops()
    {
        if(!shops.isEmpty())
        {
            return;
        }
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(SHOP_FILE);
        for(Object shop : r.getArray("Shop-Items"))
        {
            if(!(shop instanceof JSONObject jShop))
            {
                continue;
            }
            shops.add((ShopMenu) Shop.loadShop(jShop));
        }
    }

    public void openShop(NPCApi.NPC npc, Player player, ClickType click)
    {
        if(!click.isRightClick())
            return;
        String shop = npc.getName().toLowerCase();
        if(shop.contains("quick shop"))
            shop = "quick";
        if(shop.endsWith("§f"))
            shop = shop.substring(0, shop.length()-4);
        openShop(shop, TowerPlayer.getPlayer(player));
    }

    public void openShop(String shop, TowerPlayer player)
    {
        if(player == null || player.getTeam()==TeamsManager.PlayerTeam.SPECTATOR)
            return;
        ShopMenu s = getShop(shop);
        if(s!=null)
        {
            player.asPlayer().openInventory(s.createInventory(player));
        }
    }

    @Nullable
    public ShopMenu getShop(String id)
    {
        for(ShopMenu s : shops)
        {
            if(s.getId().equals(id))
            {
                return s;
            }
        }
        return null;
    }

    public void invClickManager(Player player, String shop, String item, ClickType click){
        for(ShopMenu sm : this.shops)
        {
            if(sm.getId().equals(shop))
            {
                TowerItem clicked = sm.getItem(item);
                if(clicked != null || item.equals("back"))
                {
                    sm.clicked(TowerPlayer.getPlayer(player), clicked, click);
                    break;
                }
            }
        }
    }

}
