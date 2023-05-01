package ch.tower.utils.items;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.shop.ShopMenu;
import ch.tower.shop.categoryMenus.ToolsMenu;
import ch.tower.utils.Packets.TeamsPackets;
import ch.tower.utils.items.meta.LeatherArmor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ArmorEquipment {

    private static Map<String, Map<EquipmentSlot, Item>> armorsSets = new HashMap<>();

    static {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(ToolsMenu.ARMOR_FILE);
        for(Object o : r.getArray("Armors"))
        {
            JSONApi.JSONReader level = SpigotApi.getJSONApi().getReader((JSONObject)o);
            String key = level.getString("Level")+"_armor";
            JSONApi.JSONReader contentPieces = level.getJson("Content");
            Map<EquipmentSlot, Item> content = new HashMap<>(){{
                put(EquipmentSlot.HEAD,  Item.fromJson(contentPieces.getJson("Helmet").asJson()));
                put(EquipmentSlot.CHEST, Item.fromJson(contentPieces.getJson("Chestplate").asJson()));
                put(EquipmentSlot.LEGS,  Item.fromJson(contentPieces.getJson("Leggings").asJson()));
                put(EquipmentSlot.FEET,  Item.fromJson(contentPieces.getJson("Boots").asJson()));
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
            for(Map.Entry<EquipmentSlot, Item> armorPiece : armorsSets.get(level).entrySet())
            {
                Item cloned = armorPiece.getValue().clone();
                if(!cloned.hasMeta())
                {
                    cloned.setMeta(new LeatherArmor(player.getTeam().getInfo().getColor() == TeamsPackets.TeamColor.BLUE ? Color.BLUE : Color.RED));
                }
                ItemStack item = tools.prepareItem(cloned, false);
                p.getInventory().setItem(armorPiece.getKey(), NBTTags.getInstance().getNBT(item).setTag("UUID", "current_armor").getBukkitItem());
            }
        }
    }

}
