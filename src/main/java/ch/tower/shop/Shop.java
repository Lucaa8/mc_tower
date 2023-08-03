package ch.tower.shop;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.TowerPlayer;
import ch.tower.items.Item;
import ch.tower.shop.categoryMenus.BlocksMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.json.simple.JSONObject;

import java.lang.reflect.InvocationTargetException;

public interface Shop {
    Inventory createInventory(TowerPlayer player);

    double clicked(TowerPlayer player, Item item, ClickType click);

    static Shop loadShop(JSONObject json)
    {
        try {
            String shop = (String)json.get("Id");
            if(shop!=null&&!shop.isEmpty()){
                shop = (shop.contains("_") ? shop.split("_")[1] : shop)
                        .transform(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()) + "Menu";
                JSONObject shopContent = new JSONObject();
                shopContent.put("Shop", json.get("Shop"));
                shopContent.put("Prices", json.get("Prices"));
                return (Shop)Class.forName(BlocksMenu.class.getPackage().getName()+"."+shop)
                        .getConstructor(String.class, JSONApi.JSONReader.class)
                        .newInstance((String)json.get("Id"), SpigotApi.getJSONApi().getReader(shopContent));
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("Can't load Shop with JSON: \n" + JSONApi.prettyJson(json));
            e.printStackTrace();
        }
        return null;
    }
}