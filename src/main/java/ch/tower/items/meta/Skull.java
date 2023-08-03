package ch.tower.items.meta;

import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.util.UUID;

public class Skull implements Meta {

    public enum SkullOwnerType{
        PLAYER, //A dynamic custom player (with applyOwner(UniPlayer)) (owner = null)
        PSEUDO, //A defined player pseudo i.e "Luca008" (owner = "Luca008")
        MCHEADS; //A defined existing mcheads custom head i.e "King Creeper" (owner = "King Creeper's value")
    }
    private SkullOwnerType type = null;
    private String owner = null; //si player = p.ex "Luca008" et si Custom = p.ex "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZhM2JiYTJiN2EyYjRmYTQ2OTQ1YjE0NzE3NzdhYmU0NTk5Njk1NTQ1MjI5ZTc4MjI1OWFlZDQxZDYifX19"

    public Skull(JSONObject json){
        if(json.containsKey("Type")){
            type = SkullOwnerType.valueOf((String)json.get("Type"));
        }
        if(type!=null&&type!=SkullOwnerType.PLAYER){
            if(json.containsKey("Owner")){
                owner = (String) json.get("Owner");
            }
        }
    }
    public Skull(SkullOwnerType type, String owner){
        this.type = type;
        this.owner = owner;
    }

    @Override
    public boolean hasSameMeta(ItemStack item, @Nullable OfflinePlayer player) {
        if(item!=null&&item.getItemMeta() instanceof SkullMeta &&type!=null){
            if(type==SkullOwnerType.PLAYER)
                return true; //No fixed data on this skull meta instance, so nothing to check.
            else if(type==SkullOwnerType.PSEUDO){
                SkullMeta sm = (SkullMeta) item.getItemMeta();
                if(sm.hasOwner()){
                    String name = sm.getOwningPlayer().getName();
                    return name != null && name.equals(owner);
                }
            }
            else{//MCHEADS
                NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
                if(nbt.hasTags()&&owner!=null){
                    try{
                        String value = nbt.getTags().toString().split("Value:\"")[1];
                        return owner.equals(value.substring(0,value.indexOf("\"")));
                    }catch(Exception e){
                        System.err.println("Can't read and parse(trying to find 'Value:\"someValue\"') the following nbttag: " + nbt.getTags());
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasMeta(ItemStack item){
        if(item.getType()!= Material.PLAYER_HEAD)return false;
        return SpigotApi.getNBTTagApi().getNBT(item).hasTag("SkullOwner");
    }

    @Override
    public ItemStack apply(ItemStack item) {
        if(item==null||item.getType()!=Material.PLAYER_HEAD||item.getItemMeta()==null||type==null||type==SkullOwnerType.PLAYER)return item;
        if(type==SkullOwnerType.PSEUDO){
            SkullMeta sm = (SkullMeta) item.getItemMeta();
            sm.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
            item.setItemMeta(sm);
            return item;
        }else return applySignature(item, owner);
    }

    public ItemStack applyOwner(ItemStack item, UUID player){
        if(item==null||item.getType()!=Material.PLAYER_HEAD||item.getItemMeta()==null||type==null||type!=SkullOwnerType.PLAYER)return item;
        SkullMeta sm = (SkullMeta) item.getItemMeta();
        sm.setOwningPlayer(Bukkit.getOfflinePlayer(player));
        item.setItemMeta(sm);
        return item;
    }

    public SkullOwnerType getOwningType(){
        return type;
    }

    @Override
    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(type!=null){
            j.put("Type", type.name());
        }
        if(owner!=null){
            j.put("Owner", owner);
        }
        return j;
    }

    @Override
    public MetaType getType(){
        return MetaType.SKULL;
    }

    @Override
    public String toString(){
        return "{MetaType:SKULL,Type:"+(type==null?"Null":type.name())+"Owner:{Value:"+(owner==null?"None":owner)+"}}";
    }

    public static ItemStack applySignature(ItemStack item, String encodedURL){
        net.minecraft.world.item.ItemStack nmsItem = SpigotApi.getNBTTagApi().getNMSItem(item);

        NBTTagCompound tags = SpigotApi.getNBTTagApi().getNBT(item).getTags(); //NBTTagCompound "tag"

        NBTTagIntArray id = new NBTTagIntArray(new int[]{0,0,0,0});
        NBTTagString val = NBTTagString.a(encodedURL);

        NBTTagCompound compoundSO = new NBTTagCompound();
        NBTTagCompound compoundProp = new NBTTagCompound();
        NBTTagCompound compound0 = new NBTTagCompound();
        NBTTagList textures = new NBTTagList();

        compound0.a("Value", val);
        textures.b(0, compound0);
        compoundProp.a("textures", textures);
        compoundSO.a("Id", id);
        compoundSO.a("Properties", compoundProp);

        tags.a("SkullOwner", compoundSO);
        nmsItem.c(tags);

        return SpigotApi.getNBTTagApi().getBukkitItem(nmsItem);
    }
}
