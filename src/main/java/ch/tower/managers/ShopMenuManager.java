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
import java.util.List;
import java.util.Objects;

public class ShopMenuManager {

    //https://pastebin.com/5rQsE69L
    public static final File SHOP_FILE = new File(Main.getInstance().getDataFolder(), "shop.json");

    private final List<ShopMenu> shops;

    public ShopMenuManager()
    {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(SHOP_FILE);
        this.shops = ((List<Object>) r.getArray("Shop-Items")).stream()
                .filter(JSONObject.class::isInstance).map(JSONObject.class::cast)
                .peek(j->j.put("Prices", r.getJson("Shop-Prices").getArray((String) j.get("Id"))))
                .map(Shop::loadShop).filter(Objects::nonNull)
                .filter(ShopMenu.class::isInstance).map(ShopMenu.class::cast)
                .toList();
    }

    public void openShop(NPCApi.NPC npc, Player player, ClickType click)
    {
        String shop = npc.getName().toLowerCase();
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
