package ch.tower.items;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Item.Meta.LeatherArmor;
import ch.luca008.SpigotApi.Packets.PacketsUtils;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.shop.ShopMenu;
import ch.tower.shop.categoryMenus.ToolsMenu;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ArmorEquipment {

    private static Map<String, Map<EquipmentSlot, TowerItem>> armorsSets = new HashMap<>();

    static {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(ToolsMenu.ARMOR_FILE);
        for(Object o : r.getArray("Armors"))
        {
            JSONApi.JSONReader level = SpigotApi.getJSONApi().getReader((JSONObject)o);
            String key = level.getString("Level")+"_armor";
            JSONApi.JSONReader contentPieces = level.getJson("Content");
            Map<EquipmentSlot, TowerItem> content = new HashMap<>(){{
                put(EquipmentSlot.HEAD,  TowerItem.fromJson(contentPieces.getJson("Helmet").asJson()));
                put(EquipmentSlot.CHEST, TowerItem.fromJson(contentPieces.getJson("Chestplate").asJson()));
                put(EquipmentSlot.LEGS,  TowerItem.fromJson(contentPieces.getJson("Leggings").asJson()));
                put(EquipmentSlot.FEET,  TowerItem.fromJson(contentPieces.getJson("Boots").asJson()));
            }};
            armorsSets.put(key, content);
        }
    }

    public static void equip(TowerPlayer player, String level)
    {
        if(armorsSets.containsKey(level))
        {
            Player p = player.asPlayer();
            ShopMenu tools = Main.getInstance().getManager().getShopManager().getShop("tools");
            if(tools==null) return; //never the case but better be safe than sorry
            for(Map.Entry<EquipmentSlot, TowerItem> armorPiece : armorsSets.get(level).entrySet())
            {
                TowerItem cloned = armorPiece.getValue().clone();
                if(!cloned.hasMeta())
                {
                    cloned.setMeta(new LeatherArmor(player.getTeam().getInfo().apiTeam().getColor() == PacketsUtils.ChatColor.BLUE ? Color.BLUE : Color.RED));
                }
                ItemStack item = tools.prepareItem(cloned, false);
                p.getInventory().setItem(armorPiece.getKey(), SpigotApi.getNBTTagApi().getNBT(item).setTag("UUID", "current_armor").getBukkitItem());
            }
        }
    }

}
