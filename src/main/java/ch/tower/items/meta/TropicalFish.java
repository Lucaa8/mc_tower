package ch.tower.items.meta;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class TropicalFish implements Meta {

    DyeColor body;
    DyeColor colorPattern;
    org.bukkit.entity.TropicalFish.Pattern pattern;

    public TropicalFish(JSONObject json){
        body = json.containsKey("BodyColor") ? DyeColor.valueOf((String)json.get("BodyColor")) : DyeColor.BLACK;
        colorPattern = json.containsKey("PatternColor") ? DyeColor.valueOf((String)json.get("PatternColor")) : DyeColor.WHITE;
        pattern = json.containsKey("Pattern") ? org.bukkit.entity.TropicalFish.Pattern.valueOf((String)json.get("Pattern")) : org.bukkit.entity.TropicalFish.Pattern.KOB;
    }

    public TropicalFish(org.bukkit.entity.TropicalFish.Pattern pattern, DyeColor bodyColor, DyeColor patternColor){
        this.body = bodyColor==null?DyeColor.BLACK:bodyColor;
        this.colorPattern = patternColor==null?DyeColor.WHITE:patternColor;
        this.pattern = pattern==null? org.bukkit.entity.TropicalFish.Pattern.KOB:pattern;
    }

    public TropicalFish(TropicalFishBucketMeta tm){
        if(tm!=null){
            this.body = tm.getBodyColor();
            this.colorPattern = tm.getPatternColor();
            this.pattern = tm.getPattern();
        }else{
            this.body = DyeColor.BLACK;
            this.colorPattern = DyeColor.WHITE;
            this.pattern = org.bukkit.entity.TropicalFish.Pattern.KOB;
        }
    }

    private int getData() {
        int DataValue = 0;
        try{
            Class<?> c = ReflectionApi.getOBCClass("entity","CraftTropicalFish$CraftPattern");
            Object value = ((Object[]) c.getMethod("values").invoke(c))[pattern.ordinal()];
            DataValue = (int) value.getClass().getMethod("getDataValue").invoke(value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return colorPattern.getWoolData() << 24 | body.getWoolData() << 16 | DataValue;
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item==null||item.getItemMeta()==null)return null;
        if(item.getType()== Material.TROPICAL_FISH_BUCKET){
            return SpigotApi.getNBTTagApi().getNBT(item).setTag("BucketVariantTag",getData()).getBukkitItem();
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        j.put("Pattern",pattern.name());
        j.put("BodyColor",body.name());
        j.put("PatternColor",colorPattern.name());
        return j;
    }

    @Override
    public String toString(){
        return "{MetaType:TROPICAL_FISH,Pattern:"+pattern.name()+",PatternColor:"+colorPattern.name()+",BodyColor:"+body.name()+"}";
    }

    @Override
    public MetaType getType() {
        return MetaType.TROPICAL_FISH;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof TropicalFishBucketMeta){
            TropicalFishBucketMeta m = (TropicalFishBucketMeta) item.getItemMeta();
            return m.getBodyColor().equals(body)&&m.getPattern().equals(pattern)&&colorPattern.equals(m.getPatternColor());
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        return SpigotApi.getNBTTagApi().getNBT(item).hasTag("BucketVariantTag");
    }
}
