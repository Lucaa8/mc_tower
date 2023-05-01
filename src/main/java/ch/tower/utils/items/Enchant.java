package ch.tower.utils.items;

import ch.tower.utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.TreeMap;

public class Enchant {

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

    private Enchantment enchantment;
    private int level;

    public Enchant(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public Enchant(String json){
        try {
            JSONObject j = (JSONObject) new JSONParser().parse(json);
            enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft((String)j.get("Key")));
            level = Utils.getInt(j, "Level");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ItemStack apply(ItemStack item){
        if(item==null)return null;
        if(!hasEnchant(item)){
            item.addUnsafeEnchantment(enchantment, level);
        }
        return item;
    }

    public ItemStack remove(ItemStack item){
        if(item==null)return null;
        if(hasEnchant(item)){
            item.getEnchantments().remove(enchantment);
        }
        return item;
    }

    public boolean hasEnchant(ItemStack item){
        if(item==null)return false;
        return item.getEnchantments().containsKey(enchantment);
    }

    public Enchantment getEnchantment(){
        return enchantment;
    }

    public int getLevel(){
        return level;
    }

    private String toRoman(int lvl)
    {
        int l =  map.floorKey(lvl);
        if (lvl == l)
            return map.get(lvl);
        return map.get(l) + toRoman(lvl-l);
    }

    public String getRomanLevel()
    {
        return toRoman(level);
    }

    public String asMinecraftEnchantment()
    {
        return "§7" + Utils.enumName(enchantment.getKey().getKey()) + " " + getRomanLevel();
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        j.put("Key", enchantment.getKey().getKey());
        j.put("Level", level);
        return j;
    }

    public static JSONArray listToJson(List<Enchant> list){
        JSONArray jarr = new JSONArray();
        for(Enchant e : list){
            jarr.add(e.toJson());
        }
        return jarr;
    }

    @Override
    public String toString(){
        return "Enchantment{Key:"+enchantment.getKey().getKey()+",Level:"+level+"}";
    }

}