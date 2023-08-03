package ch.tower.items;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NBTTagApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.items.meta.Book;
import ch.tower.items.meta.Meta;
import ch.tower.items.meta.MetaLoader;
import ch.tower.items.meta.Skull;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.util.*;

public class Item {

    private String uid;
    private Material material;
    private String name;
    private List<String> lore;
    private List<Enchant> enchantList;
    private List<ItemFlag> flags;
    private List<ItemAttribute> attributes;
    private Meta meta;
    private int repairCost;//ItemStack editedCost = new NBTTag(i).setTag("RepairCost",34).getBukkitItem();
    private int customData;
    //En réalité c'est la durabilité, damage = 5 in game sera 5/1561 pr la hache en diams p.ex
    private int damage;//((Damageable)i.getItemMeta()).setDamage(i.getType().getMaxDurability()-{int:durability(p.ex123)});
    private boolean invulnerable;

    private int slot, fakeSlot;

    private int count;

    public Item(String uid, Material material, String name, List<String> lore, List<Enchant> enchantList, List<ItemFlag> flags, List<ItemAttribute> attributes, Meta meta, int repairCost, int customData, int damage, boolean invulnerable, int slot, int count) {
        this.uid = uid;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.enchantList = enchantList;
        this.flags = flags;
        this.attributes = attributes;
        this.meta = meta;
        this.repairCost = repairCost;
        this.customData = customData;
        this.damage = damage;
        this.invulnerable = invulnerable;
        int floorDivision = Math.floorDiv(slot, 7);
        this.slot = ((slot%7!=0?floorDivision+1:floorDivision)*9)+(slot%7==0?7:slot%7);
        this.fakeSlot = slot;
        this.count = count;
    }

    public static Item fromJson(JSONObject json){
        JSONApi.JSONReader r = SpigotApi.getJSONApi().getReader(json);
        ItemBuilder item = new ItemBuilder();
        if(r.c("Id"))item.setUid(r.getString("Id"));
        try{
            item.setMaterial(Material.valueOf(r.getString("Material")));
        }catch(Exception e){
            System.err.println("Can't load item '"+(r.c("Id")?r.getString("Id"):"unknown")+"' because bukkit couldn't find Material '"+r.getString("Material")+"'.");
            System.err.println("The present item will be replaced by a simple stone block until it gets fixed.");
            return new ItemBuilder().setMaterial(Material.STONE).createItem();
        }
        if(r.c("Name"))item.setName(r.getString("Name"));
        if(r.c("RepairCost"))item.setRepairCost(r.getInt("RepairCost"));
        if(r.c("CustomData"))item.setCustomData(r.getInt("CustomData"));
        if(r.c("Lore")){
            JSONArray jarr = r.getArray("Lore");
            List<String> l = new ArrayList<String>();
            jarr.forEach(o->l.add((String)o));
            item.setLore(l);
        }
        if(r.c("Enchants")){
            JSONArray jarr = r.getArray("Enchants");
            List<Enchant> e = new ArrayList<>();
            jarr.forEach(o->e.add(new Enchant(((JSONObject)o).toJSONString())));
            item.setEnchantList(e);
        }
        if(r.c("Attributes")){
            JSONArray jarr = r.getArray("Attributes");
            List<ItemAttribute> a = new ArrayList<>();
            jarr.forEach(o->a.add(new ItemAttribute(((JSONObject)o).toJSONString())));
            item.setAttributes(a);
        }
        if(r.c("Flags")){
            JSONArray jarr = r.getArray("Flags");
            List<ItemFlag> f = new ArrayList<ItemFlag>();
            jarr.forEach(o->f.add(ItemFlag.valueOf((String)o)));
            item.setFlags(f);
        }
        if(r.c("ItemMeta")){
            item.setMeta(new MetaLoader().load(r.getJson("ItemMeta").asJson()));
        }
        if(r.c("Durability"))item.setDamage(r.getInt("Durability"));
        if(r.c("Invulnerable"))item.isInvulnerable(r.getBool("Invulnerable"));
        if(r.c("Slot"))item.setSlot(r.getInt("Slot"));
        if(r.c("Count"))item.setCount(r.getInt("Count"));
        return item.createItem();
    }

    public void glow(){
        if(flags==null)flags = new ArrayList<>();
        flags.add(ItemFlag.HIDE_ENCHANTS);
        if(enchantList==null)enchantList=new ArrayList<>();
        enchantList.add(new Enchant(Enchantment.LUCK,1));
    }

    public void hideAttributes(){
        if(flags==null)flags=new ArrayList<>();
        flags.add(ItemFlag.HIDE_ATTRIBUTES);
    }

    public void hideColor()
    {
        if(flags==null)flags=new ArrayList<>();
        flags.add(ItemFlag.HIDE_DYE);
    }

    private ItemStack _toItemStack(int amount, @Nullable Player player){
        if(amount>64)amount=64;
        ItemStack item = new ItemStack(material, amount);
        if(hasItemMeta()) item.setItemMeta(getItemMeta());
        if(meta!=null){
            item = meta.apply(item);
            if(player!=null){
                if(meta instanceof Skull){
                    Skull s = (Skull)meta;
                    if(s.getOwningType()==Skull.SkullOwnerType.PLAYER){
                        item = ((Skull)meta).applyOwner(item, player.getUniqueId());
                    }
                }
                else if(meta instanceof Book){
                    item = ((Book)meta).applyForPlayer(item, player.getName());
                }
            }
        }
        NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
        if(repairCost>0)nbt.setTag("RepairCost",repairCost);
        if(uid!=null&&!uid.isEmpty())nbt.setTag("UUID", uid);
        return nbt.getBukkitItem();
    }

    /**
     * Similar to {@link #toItemStacks(int, Player)} but amount cannot exceed 64
     * @param amount An amount between 0 and 64 inclusive
     * @return A stack with specified amount or rounded to 64 if more
     */
    public ItemStack toItemStack(int amount, @Nullable Player player){
        return _toItemStack(amount, player);
    }

    /**
     * See {@link #toItemStack(int, Player)}
     */
    public ItemStack toItemStack(int amount){
        return toItemStack(amount, null);
    }

    /**
     * Similar to {@link #toItemStack(int, Player)}} but amount can exceed 64
     * @param amount any amount >= 0 (can exceed 64)
     * @param player A potential player if skull or book meta are required (Nullable)
     * @return An array of stacks. If amount <= 64 the array will contain the only stack at index 0
     */
    public ItemStack[] toItemStacks(int amount, @Nullable Player player){
        if(amount<=64)return new ItemStack[]{_toItemStack(amount,player)};
        List<ItemStack> items = new ArrayList<>();
        ItemStack brut = _toItemStack(64, player);
        while(amount>0){
            if(amount>64){
                items.add(brut.clone());
                amount-=64;
            }else{
                ItemStack lessAmount = brut.clone();
                lessAmount.setAmount(amount);
                items.add(lessAmount);
                amount=0;
            }
        }
        return items.toArray(new ItemStack[0]);
    }

    /**
     * See {@link #toItemStacks(int, Player)}
     */
    public ItemStack[] toItemStacks(int amount){
        return toItemStacks(amount, null);
    }

    public boolean hasItemMeta(){
        return ((name!=null&&!name.isEmpty())
                ||(lore!=null&&!lore.isEmpty())
                ||(enchantList!=null&&!enchantList.isEmpty())
                ||(flags!=null&&!flags.isEmpty())
                ||(attributes!=null&&!attributes.isEmpty())
                ||invulnerable
                ||customData>0
                ||damage>0
                ||repairCost>0
                ||(uid!=null&&!uid.isEmpty())
                ||hasMeta());
    }

    public ItemMeta getItemMeta(){
        ItemMeta im = Bukkit.getItemFactory().getItemMeta(material);
        if(name!=null&&!name.isEmpty())im.setDisplayName(name.replace("&","§"));
        if(lore!=null&&!lore.isEmpty())im.setLore(lore);
        if(enchantList!=null){
            for(Enchant e : enchantList){
                im.addEnchant(e.getEnchantment(), e.getLevel(), true);
            }
        }
        if(flags!=null&&!flags.isEmpty())flags.forEach(im::addItemFlags);
        if(attributes!=null&&!attributes.isEmpty())attributes.forEach(a->im.addAttributeModifier(a.getAttribute(), a.getAttributeModifier()));
        if(invulnerable)im.setUnbreakable(true);
        if(customData>0)im.setCustomModelData(customData);
        if(damage>0)((Damageable)im).setDamage(material.getMaxDurability()-damage);
        return im;
    }

    /**
     * See {@link #isSimilar(ItemStack, Player)}
     */
    public boolean isSimilar(ItemStack item){//à tester lol
        return isSimilar(item,null);
    }

    /**
     * Compare the item argument with info in this item class
     * <p>
     * Material type
     * <p>
     * Display name
     * <p>
     * Lore
     * <p>
     * Item flags
     * <p>
     * Enchantments
     * <p>
     * Attributes modifiers
     * <p>
     * Unbreakable
     * <p>
     * CustomModelData (Textures pack)
     * <p>
     * Damage (Durability)
     * <p>
     * RepairCost (Initial reparation cost for the item into the anvil)
     * <p>
     * A special UUID field into the item's nbttags (Name: UUID)
     * <p>
     * Meta: Skull, Book, TropicalFishBucket, LeatherArmor and Potions(Splash & Lingering included)
     * @param item The itemstack to compare. Can contain meta like skull, book(even with {P} balises for playername), nbttags like custom data, repaircost, damage, etc
     * @param player A potential player who can be used in {@link Meta#hasSameMeta(ItemStack, org.bukkit.OfflinePlayer)}. Can be null without throwing exceptions
     * @return if the itemstack match the current item
     */
    public boolean isSimilar(ItemStack item, @Nullable Player player){
        if(item==null||item.getType()==Material.AIR)return false;
        if(item.getType()!=material)return false;
        if(item.hasItemMeta()!=hasItemMeta())return false;
        if(item.hasItemMeta()){
            ItemMeta itemMeta1 = item.getItemMeta(), itemMeta2 = getItemMeta();
            if(!StringUtils.equals(itemMeta1.getDisplayName(), itemMeta2.getDisplayName()))return false;
            if(!ch.luca008.SpigotApi.Utils.StringUtils.equalLists(itemMeta1.getLore(), itemMeta2.getLore()))return false;
            if(!compareFlags(itemMeta1.getItemFlags(),itemMeta2.getItemFlags()))return false;
            if(!(itemMeta1.hasEnchants()?itemMeta2.hasEnchants()&&itemMeta2.getEnchants().equals(itemMeta1.getEnchants()):!itemMeta2.hasEnchants()))return false;
            if(!(itemMeta1.hasAttributeModifiers()?itemMeta2.hasAttributeModifiers()&&compareModifiers(itemMeta1.getAttributeModifiers(),itemMeta2.getAttributeModifiers()):!itemMeta2.hasAttributeModifiers()))return false;
            if(itemMeta1.isUnbreakable()!=itemMeta2.isUnbreakable())return false;
            if(!(itemMeta1.hasCustomModelData()?itemMeta2.hasCustomModelData()&&itemMeta2.getCustomModelData()==itemMeta1.getCustomModelData():!itemMeta2.hasCustomModelData()))return false;
            if(!(itemMeta1 instanceof Damageable) && itemMeta2 instanceof Damageable)return false;
            if(!(itemMeta2 instanceof Damageable) && itemMeta1 instanceof Damageable)return false;
            if(itemMeta1 instanceof Damageable){
                Damageable d1 = (Damageable) itemMeta1, d2 = (Damageable) itemMeta2;
                if(!(d1.hasDamage()?d2.hasDamage()&&d1.getDamage()==d2.getDamage():!d2.hasDamage()))return false;
            }
            NBTTagApi.NBTItem nbt = SpigotApi.getNBTTagApi().getNBT(item);
            int itemCost = nbt.hasTag("RepairCost")?Integer.parseInt(nbt.getTag("RepairCost").toString()):0;
            if(!(repairCost>0?repairCost==itemCost:itemCost==0))return false;
            String itemUid = nbt.hasTag("UUID")? nbt.getString("UUID"):null;
            if(!(uid!=null&&!uid.isEmpty()?itemUid!=null&&!itemUid.isEmpty()&&uid.equals(itemUid):(itemUid==null||itemUid.isEmpty())))return false;
            boolean hasMeta = Meta.hasMeta(item);
            if(!(hasMeta()?hasMeta&&meta.hasSameMeta(item,player):!hasMeta))return false;//check les meta potion, armure cuir, fish bucket, skull
        }
        return true;
    }

    public void giveOrDrop(Player p, int amount){
        PlayerInventory inv = p.getInventory();
        ItemStack current = toItemStack(amount);
        if(meta instanceof Skull){
            if(((Skull)meta).getOwningType()==Skull.SkullOwnerType.PLAYER)current = ((Skull)meta).applyOwner(current, p.getUniqueId());
        }
        if(meta instanceof Book){
            current = ((Book)meta).applyForPlayer(current, p.getName());
        }
        Map<Integer, ItemStack> full = inv.addItem(current);
        if (!full.isEmpty()) {
            Location loc = p.getLocation();
            for(Map.Entry<Integer, ItemStack> extra : full.entrySet()) {
                p.getWorld().dropItem(loc, extra.getValue());
            }
        }
    }

    public String getUid(){
        return uid;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial(){
        return material;
    }

    public int getSlot(){
        return slot;
    }

    public int getCount(){
        return count;
    }

    public int getCustomData(){
        return customData;
    }

    public String getName(){
        if(name!=null&&!name.isEmpty())return name;
        return ch.luca008.SpigotApi.Utils.StringUtils.enumName(getMaterial());
    }

    public Meta getMeta(){
        return meta;
    }
    public boolean hasMeta(){
        return getMeta()!=null;
    }

    public void setMeta(Meta meta)
    {
        this.meta = meta;
    }
    public void setLore(String lore){
        this.lore = ch.luca008.SpigotApi.Utils.StringUtils.asLore(lore);
    }
    public List<String> getLore()
    {
        return this.lore;
    }

    public List<Enchant> getEnchantList()
    {
        return new ArrayList<>(this.enchantList);
    }

    public JSONObject toJson(){
        JSONObject j = new JSONObject();
        if(uid!=null&&!uid.isEmpty())j.put("Id",uid);
        j.put("Material",material.name());
        if(name!=null&&!name.isEmpty())j.put("Name",name);
        if(repairCost>0)j.put("RepairCost", repairCost);
        if(customData>0)j.put("CustomData", customData);
        if(lore!=null&&!lore.isEmpty()){
            JSONArray jarr = new JSONArray();
            for(String line : lore){
                jarr.add(line);
            }
            j.put("Lore", jarr);
        }
        if(enchantList!=null&&!enchantList.isEmpty()){
            j.put("Enchants",Enchant.listToJson(enchantList));
        }
        if(attributes!=null&&!attributes.isEmpty()){
            j.put("Attributes",ItemAttribute.listToJson(attributes));
        }
        if(flags!=null&&!flags.isEmpty()){
            JSONArray jarr = new JSONArray();
            for(ItemFlag f : flags){
                jarr.add(f.name());
            }
            j.put("Flags",jarr);
        }
        if(meta!=null){
            j.put("ItemMeta",new MetaLoader().unload(meta));
        }
        if(damage>0){
            j.put("Durability",damage);
        }
        if(invulnerable)j.put("Invulnerable",true);
        j.put("Slot", fakeSlot);
        j.put("Count", count);
        return j;
    }

    @Override
    public String toString(){
        return "Item{" +
                "\nId:"+(uid==null||uid.isEmpty()?"null":uid)+
                ",\nMaterial:"+material.name()+
                ",\nDisplayname:"+name+
                ",\nLore:"+(lore==null||lore.isEmpty()?"null":lore.toString())+
                ",\nEnchants:"+(enchantList==null||enchantList.isEmpty()?"null":enchantList.toString())+
                ",\nAttributes:"+(attributes==null||attributes.isEmpty()?"null":attributes.toString())+
                ",\nFlags:"+(flags==null||flags.isEmpty()?"null":flags.toString())+
                ",\nItemMeta:"+(meta==null?"null":meta.toString())+
                ",\nRepairCost:"+repairCost+
                ",\nCustomData:"+customData+
                ",\nDurability:"+damage+
                ",\nInvulnerable:"+invulnerable+
                ",\nSlot:"+slot+
                ",\nCount:"+count+
                "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return uid.equals(item.uid) && material == item.material && Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, material, name);
    }

    @Override
    public Item clone()
    {
        return fromJson(toJson());
    }

    //compareModifiers from org/bukkit/craftbukkit/inventory/CraftMetaItem.java
    private boolean compareModifiers(Multimap<Attribute, AttributeModifier> first, Multimap<Attribute, AttributeModifier> second) {
        if (first == null || second == null)return false;
        for (Map.Entry<Attribute, AttributeModifier> entry : first.entries()) {
            if (!second.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        for (Map.Entry<Attribute, AttributeModifier> entry : second.entries()) {
            if (!first.containsEntry(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean compareFlags(Set<ItemFlag> first, Set<ItemFlag> second){
        if (first == null || second == null)return false;
        if(first.size()!=second.size())return false;
        for(ItemFlag flag : first){
            if(!second.contains(flag))return false;
        }
        return true;
    }
}
