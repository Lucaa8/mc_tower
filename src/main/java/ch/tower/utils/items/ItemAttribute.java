package ch.tower.utils.items;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.UUID;

public class ItemAttribute {

    private Attribute attribute;
    private AttributeModifier modifier;

    public ItemAttribute(String json){
        try {
            JSONObject j = (JSONObject) new JSONParser().parse(json);
            attribute = Attribute.valueOf((String)j.get("Attribute"));
            String name = (String)j.get("Name");
            double value = (double)j.get("Value");
            AttributeModifier.Operation operation = j.containsKey("Operation") ? AttributeModifier.Operation.valueOf((String)j.get("Operation")) : AttributeModifier.Operation.ADD_NUMBER;
            EquipmentSlot slot = j.containsKey("Slot") ? EquipmentSlot.valueOf((String)j.get("Slot")) : null;
            if(slot==null) modifier = new AttributeModifier(UUID.randomUUID(), name, value, operation);
            else modifier = new AttributeModifier(UUID.randomUUID(), name, value, operation, slot);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ItemAttribute(Attribute attribute, String name, double value, AttributeModifier.Operation operation, EquipmentSlot slot) {
        this.attribute = attribute;
        if(slot==null) modifier = new AttributeModifier(UUID.randomUUID(), name, value, operation);
        else modifier = new AttributeModifier(UUID.randomUUID(), name, value, operation, slot);
    }

    public ItemStack apply(ItemStack i){
        ItemMeta im = i.getItemMeta();
        if(!hasAttribute(i)){
            im.addAttributeModifier(attribute, modifier);
            i.setItemMeta(im);
        }
        return i;
    }

    public ItemStack remove(ItemStack i){
        ItemMeta im = i.getItemMeta();
        if(hasAttribute(i)){
            im.removeAttributeModifier(attribute, modifier);
            i.setItemMeta(im);
        }
        return i;
    }

    public boolean hasAttribute(ItemStack i){
        if(i.hasItemMeta()){
            ItemMeta im = i.getItemMeta();
            if(im.hasAttributeModifiers()){
                for(AttributeModifier am : im.getAttributeModifiers().values()){
                    if(am.getName().equals(modifier.getName())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Attribute getAttribute(){
        return attribute;
    }

    public AttributeModifier getAttributeModifier(){
        return modifier;
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        j.put("Attribute", attribute.name());
        j.put("Name", modifier.getName());
        j.put("Value", modifier.getAmount());
        j.put("Operation", modifier.getOperation().name());
        if(modifier.getSlot()!=null){
            j.put("Slot", modifier.getSlot().name());
        }
        return j;
    }

    public static JSONArray listToJson(List<ItemAttribute> list){
        JSONArray jarr = new JSONArray();
        for(ItemAttribute a : list){
            jarr.add(a.toJson());
        }
        return jarr;
    }

    @Override
    public String toString(){
        return "{Attribute:"+attribute.name()+",Name:"+modifier.getName()+",Value:"+modifier.getAmount()+",Operation:"+ modifier.getOperation().name()+",Slot:"+
                (modifier.getSlot()==null?"Empty": modifier.getSlot().name())+"}";
    }
}
