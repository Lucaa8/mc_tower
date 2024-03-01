package ch.tower.items;

import ch.luca008.SpigotApi.Item.Enchant;

import java.util.TreeMap;

public class EnchantUtils {

    private final static TreeMap<Integer, String> map = new TreeMap<>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    private static String getRomanLevel(int lvl)
    {
        int l =  map.floorKey(lvl);
        if (lvl == l)
            return map.get(lvl);
        return map.get(l) + getRomanLevel(lvl-l);
    }

    public static String asMinecraftEnchantment(Enchant enchant)
    {
        return "§7" + ch.luca008.SpigotApi.Utils.StringUtils.enumName(enchant.getEnchantment().getKey().getKey()) + " " + getRomanLevel(enchant.getLevel());
    }

}
