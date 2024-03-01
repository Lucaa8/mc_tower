package ch.tower.items;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Item.Enchant;
import ch.luca008.SpigotApi.Item.Item;
import ch.luca008.SpigotApi.Item.ItemAttribute;
import ch.luca008.SpigotApi.Item.Meta.Meta;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.json.simple.JSONObject;

import java.util.List;

public class TowerItem extends Item {

    private int slot, fakeSlot, count;

    public TowerItem(String uid, Material material, String name, List<String> lore, List<Enchant> enchantList, List<ItemFlag> flags, List<ItemAttribute> attributes, Meta meta, int repairCost, int customData, int damage, boolean invulnerable, int slot, int count) {
        super(uid, material, name, lore, enchantList, flags, attributes, meta, repairCost, customData, damage, invulnerable);
        setSlot(slot);
        this.count = count;
    }

    public TowerItem(Item item, int slot, int count) {
        this(
                item.getUid(),
                item.getMaterial(),
                item.getName(),
                item.getLore(),
                item.getEnchantList(),
                item.getFlags(),
                item.getAttributes(),
                item.getMeta(),
                item.getRepairCost(),
                item.getCustomModelData(),
                item.getDamage(),
                item.isInvulnerable(),
                slot,
                count
        );
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setSlot(int slot) {
        int floorDivision = Math.floorDiv(slot, 7);
        this.slot = ((slot%7!=0?floorDivision+1:floorDivision)*9)+(slot%7==0?7:slot%7);
        this.fakeSlot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public void hideAttributes(){
        super.addFlag(ItemFlag.HIDE_ATTRIBUTES);
    }

    public void hideColor()
    {
        super.addFlag(ItemFlag.HIDE_DYE);
    }

    @Override
    public TowerItem clone()
    {
        return fromJson(toJson());
    }

    public static TowerItem fromJson(JSONObject json){
        JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader(json);
        return new TowerItem(Item.fromJson(r), r.c("Slot") ? r.getInt("Slot") : 0, r.c("Count") ? r.getInt("Count") : 1);
    }

    @Override
    public JSONObject toJson(){
        JSONObject json = super.toJson();
        json.put("Slot", fakeSlot);
        json.put("Count", count);
        return json;
    }

    @Override
    public String toString(){
        return "TowerItem{" +
                "\nId:"+(super.getUid()==null||super.getUid().isEmpty()?"null":super.getUid())+
                ",\nMaterial:"+super.getMaterial().name()+
                ",\nDisplayname:"+super.getName()+
                ",\nLore:"+(super.getLore()==null||super.getLore().isEmpty()?"null":super.getLore().toString())+
                ",\nEnchants:"+(super.getEnchantList()==null||super.getEnchantList().isEmpty()?"null":super.getEnchantList().toString())+
                ",\nAttributes:"+(super.getAttributes()==null||super.getAttributes().isEmpty()?"null":super.getAttributes().toString())+
                ",\nFlags:"+(super.getFlags()==null||super.getFlags().isEmpty()?"null":super.getFlags().toString())+
                ",\nItemMeta:"+(super.getMeta()==null?"null":super.getMeta().toString())+
                ",\nRepairCost:"+super.getRepairCost()+
                ",\nCustomData:"+super.getCustomModelData()+
                ",\nDurability:"+super.getDamage()+
                ",\nInvulnerable:"+super.isInvulnerable()+
                ",\nSlot:"+slot+
                ",\nCount:"+count+
                "\n}";
    }
}
