package ch.tower.items;

import ch.tower.items.meta.Meta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
    private String uid;
    private Material material = Material.STONE;
    private String name;
    private List<String> lore = new ArrayList<>();
    private List<Enchant> enchantList = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();
    private List<ItemAttribute> attributes = new ArrayList<>();
    private boolean glowing = false;
    private Meta meta;
    private int repairCost = 0;
    private int customData = 0;
    private int damage = 0;
    private boolean invulnerable = false;
    private int slot = 1;
    private int count = 1;

    public ItemBuilder setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    public ItemBuilder setEnchantList(List<Enchant> enchantList) {
        this.enchantList = enchantList;
        return this;
    }

    public ItemBuilder setFlags(List<ItemFlag> flags) {
        this.flags = flags;
        return this;
    }

    public ItemBuilder setAttributes(List<ItemAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public ItemBuilder setMeta(Meta meta) {
        this.meta = meta;
        return this;
    }

    public ItemBuilder setRepairCost(int repairCost) {
        this.repairCost = repairCost;
        return this;
    }

    public ItemBuilder setCustomData(int customData) {
        this.customData = customData;
        return this;
    }

    public ItemBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public ItemBuilder isInvulnerable(boolean invulnerable){
        this.invulnerable = invulnerable;
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing){
        this.glowing = glowing;
        return this;
    }

    public ItemBuilder setSlot(int slot){
        this.slot = slot;
        return this;
    }

    public ItemBuilder setCount(int count){
        this.count = count;
        return this;
    }

    public Item createItem() {
        Item i = new Item(uid, material, name, lore, enchantList, flags, attributes, meta, repairCost, customData, damage, invulnerable, slot, count);
        if(glowing){
            i.glow();
        }
        return i;
    }
}
