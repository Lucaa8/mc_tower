package ch.tower.items.meta;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;

public class LeatherArmor implements Meta {

    private Color color = Bukkit.getItemFactory().getDefaultLeatherColor();

    public LeatherArmor(JSONObject json){
        if(json.containsKey("Color")){
            Object o = json.get("Color");
            if(o instanceof JSONObject) {
                JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader((JSONObject) o);
                color = Color.fromRGB(r.getInt("r"), r.getInt("g"), r.getInt("b"));
            }
        }
    }

    public LeatherArmor(Color color){
        if(color!=null){
            this.color = color;
        }
    }

    public LeatherArmor(LeatherArmorMeta lm){
        this(lm==null?null:lm.getColor());
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item.getItemMeta()!=null&&item.getItemMeta() instanceof LeatherArmorMeta){
            LeatherArmorMeta lam = (LeatherArmorMeta) item.getItemMeta();
            lam.setColor(color);
            item.setItemMeta(lam);
            return item;
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        JSONObject rgb = new JSONObject();
        rgb.put("r",color.getRed());
        rgb.put("g",color.getGreen());
        rgb.put("b",color.getBlue());
        j.put("Color", rgb);
        return j;
    }

    @Override
    public String toString(){
        return "{MetaType:LEATHER_ARMOR,Color:{R:"+color.getRed()+",G:"+color.getGreen()+",B:"+color.getBlue()+"}}";
    }

    @Override
    public MetaType getType() {
        return MetaType.LEATHER_ARMOR;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof LeatherArmorMeta){
            return ((LeatherArmorMeta)item.getItemMeta()).getColor().equals(color);
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item.getItemMeta() instanceof LeatherArmorMeta){
            int c = ((LeatherArmorMeta)item.getItemMeta()).getColor().asRGB();
            int r = (c&0xFF0000)>>16;
            int g = (c&0xFF00)>>8;
            int b = c&0xFF;
            return !(r==160 && g==101 && b==64);
        }
        return false;
    }
}
