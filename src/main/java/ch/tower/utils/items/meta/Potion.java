package ch.tower.utils.items.meta;

import ch.tower.utils.Utils;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Potion implements Meta{

    private PotionData mainEffect;
    private List<PotionEffect> customsEffects;
    private Color color = null;

    public Potion(JSONObject json){
        if(json.containsKey("Color")){
            Object o = json.get("Color");
            if(o instanceof JSONObject){
                JSONObject j = (JSONObject)o;
                color = Color.fromRGB(Utils.getInt(j, "r"),Utils.getInt(j, "g"),Utils.getInt(j, "b"));
            }
        }
        if(json.containsKey("BaseEffect")){
            JSONObject j = (JSONObject) json.get("BaseEffect");
            mainEffect = new PotionData(PotionType.valueOf((String)j.get("Type")), (boolean)j.get("Extended"), (boolean)j.get("Upgraded"));
        }else mainEffect = new PotionData(PotionType.UNCRAFTABLE, false, false);
        if(json.containsKey("CustomEffects")){
            JSONArray jarr = (JSONArray) json.get("CustomEffects");
            customsEffects = new ArrayList<>();
            for (Object o : jarr) {
                JSONObject j = (JSONObject) o;
                customsEffects.add(new PotionEffect(PotionEffectType.getByName((String)j.get("Type")), Utils.getInt(j,"Duration"), Utils.getInt(j,"Amplifier"), j.containsKey("Ambient")?(boolean) j.get("Ambient"):false, j.containsKey("Particles")?(boolean)j.get("Particles"):false));
            }
        }
    }

    public Potion(PotionData mainEffect, List<PotionEffect> customsEffects, Color color) {
        this.mainEffect = mainEffect==null?new PotionData(PotionType.UNCRAFTABLE, false, false):mainEffect;
        this.customsEffects = customsEffects;
        this.color = color;
    }

    public Potion(PotionMeta pm){
        if(pm!=null){
            this.mainEffect = pm.getBasePotionData();
            if(pm.hasCustomEffects()){
                this.customsEffects = pm.getCustomEffects();
            }
            if(pm.hasColor()){
                this.color = pm.getColor();
            }
        }else{
            this.mainEffect = new PotionData(PotionType.UNCRAFTABLE, false, false);
        }
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item==null||item.getItemMeta()==null)return null;
        if(item.getItemMeta() instanceof PotionMeta){
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            if(color!=null)pm.setColor(color);
            pm.setBasePotionData(mainEffect);
            if(customsEffects!=null&&!customsEffects.isEmpty()){
                customsEffects.forEach(e->pm.addCustomEffect(e,true));
            }
            item.setItemMeta(pm);
            return item;
        }
        return null;
    }

    @Override
    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        if(color!=null){
            JSONObject rgb = new JSONObject();
            rgb.put("r",color.getRed());
            rgb.put("g",color.getGreen());
            rgb.put("b",color.getBlue());
            j.put("Color", rgb);
        }
        JSONObject base = new JSONObject();
        base.put("Type", mainEffect.getType().name());
        base.put("Extended", mainEffect.isExtended());
        base.put("Upgraded", mainEffect.isUpgraded());
        j.put("BaseEffect", base);
        if(customsEffects!=null&&!customsEffects.isEmpty()){
            JSONArray jarr = new JSONArray();
            for (PotionEffect eff : customsEffects) {
                JSONObject e = new JSONObject();
                e.put("Type", eff.getType().getName());
                e.put("Duration", eff.getDuration());
                e.put("Amplifier", eff.getAmplifier());
                if(eff.isAmbient()){
                    e.put("Ambient", true);
                }
                if(eff.hasParticles()){
                    e.put("Particles", true);
                }
                jarr.add(e);
            }
            j.put("CustomEffects", jarr);
        }
        return j;
    }

    @Override
    public String toString(){
        String effects = "NULL";
        if(customsEffects!=null&&!customsEffects.isEmpty()){
            effects = "{";
            for(PotionEffect e : customsEffects){
                effects+="{Type:"+e.getType().getName()+",Duration:"+e.getDuration()+",Amplifier:"+e.getAmplifier()+",Ambient:"+e.isAmbient()+",Particles:"+e.hasParticles()+"},";
            }
            effects = effects.substring(0,effects.length()-1);
            effects += "}";
        }
        return "{MetaType:POTION,BasePotion:{Effect:"+mainEffect.getType().name()+",Extended:"+mainEffect.isExtended()+",Upgraded:"+mainEffect.isUpgraded()+"}," +
                (color==null?"Color:NULL":"Color:{R:"+color.getRed()+",G:"+color.getGreen()+",B:"+color.getBlue()+"}," +
                        "CustomEffects:"+effects+"}");
    }

    @Override
    public MetaType getType() {
        return MetaType.POTION;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable Player player) {
        if(item!=null&&item.getItemMeta() instanceof PotionMeta){
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            PotionData metaData = pm.getBasePotionData();
            if(!(color!=null?(pm.hasColor()&&pm.getColor().equals(color)):!pm.hasColor()))return false;
            if(!mainEffect.equals(metaData))return false;
            if(customsEffects!=null&&pm.hasCustomEffects()){
                if(customsEffects.size()!=pm.getCustomEffects().size())return false;
                for (PotionEffect thiz : customsEffects) {
                    boolean contains = false;
                    for(PotionEffect that : pm.getCustomEffects()){
                        if(thiz.equals(that)){
                            contains = true;
                            break;
                        }
                    }
                    if(!contains)return false;
                }
            }else{
                if((customsEffects!=null&&!pm.hasCustomEffects())||(customsEffects==null&&pm.hasCustomEffects()))return false;
            }
            return true;
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item.getItemMeta() instanceof PotionMeta){
            PotionMeta pm = (PotionMeta) item.getItemMeta();
            if(pm.getBasePotionData().getType()!=PotionType.WATER)return true;
            if(pm.hasColor())return true;
            if(pm.hasCustomEffects())return true;
        }
        return false;
    }
}
