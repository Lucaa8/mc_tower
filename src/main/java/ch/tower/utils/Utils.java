package ch.tower.utils;

import ch.tower.utils.items.NBTTags;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static boolean equalLists(List<String> a, List<String> b){
        if (a == null && b == null) return true;
        if (((a==null)!=(b==null)) || (a.size() != b.size()))return false;
        return a.equals(b);
    }

    public static String addCap(String s){
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static String enumName(String name){
        if(!name.contains("_"))return addCap(name);
        String finalName = "";
        for(String s : name.split("_")){
            finalName+=addCap(s)+" ";
        }
        return finalName.substring(0,finalName.length()-1);
    }
    public static String enumName(Enum<?> e){
        return enumName(e.name());
    }

    @Nonnull
    public static List<String> asLore(@Nullable String lore){
        if(lore==null||lore.isEmpty())return new ArrayList<>();
        lore = lore.replace("&","§");
        List<String> loreArray = new ArrayList<>();
        String lastClr = "";
        if(lore.contains("\n")){
            for(String s : lore.split("\\n")){
                String line;
                if(!lastClr.isEmpty()){
                    line=lastClr+s;
                }else line=s;
                loreArray.add(line);
                for(int i=0;i<line.length()-1;i++){
                    char id = line.charAt(i);
                    char code = line.charAt(i+1);
                    if(id=='§'&&code!='§'){
                        if(lastClr.length()>=4){
                            lastClr = ""+id+code;
                        }else{
                            lastClr+=""+id+code;
                        }
                    }
                }
            }
        }else loreArray.add(lore);
        return loreArray;
    }

    public static long getLong(JSONObject json, String key){
        return ((Long)json.get(key)).longValue();
    }

    public static int getInt(JSONObject json, String key){
        Object o = json.get(key);
        if(o instanceof Long) return ((Long)json.get(key)).intValue();
        else return (int)o;
    }

    public static int getInt(Object o){
        if(o instanceof Long) return ((Long)o).intValue();
        else return (int)o;
    }

    public static String prettyJson(JSONObject json){
        return prettyJson(json.toJSONString());
    }
    public static String prettyJson(String json){
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(new JsonParser().parse(json));
    }

    //for 1.19
    public static ItemStack applySignature(ItemStack item, String encodedURL)
    {
        NBTTags.NBTItem nmsItem = NBTTags.getInstance().getNBT(item);

        NBTTagCompound skullowner = new NBTTagCompound();
        NBTTagCompound properties = new NBTTagCompound();
        NBTTagCompound value = new NBTTagCompound();
        NBTTagIntArray id = new NBTTagIntArray(Arrays.asList(0,0,0,0));
        NBTTagList textures = new NBTTagList();

        value.a("Value", NBTTagString.a(encodedURL));
        textures.b(0, value);

        properties.a("textures", textures);
        skullowner.a("Id", id);
        skullowner.a("Properties", properties);

        NBTTagCompound tags = nmsItem.getNMSItem().v();
        tags.a("SkullOwner", skullowner);

        nmsItem.getNMSItem().c(tags);

        return nmsItem.getBukkitItem();
    }

}
