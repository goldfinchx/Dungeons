package ru.goldfinch.dungeons.utils;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private ItemStack stack;

    public ItemBuilder(Material mat) {
        stack = new ItemStack(mat);
    }

    public ItemBuilder(Material mat, int data) {
        stack = new ItemStack(mat, 1, (short) data);
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemMeta getItemMeta() {
        return stack.getItemMeta();
    }

    public ItemBuilder setColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(color);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setData(short data) {
        stack.setDurability(data);
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow) {
            addEnchant(Enchantment.KNOCKBACK, 1);
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            ItemMeta meta = getItemMeta();
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
            }
        }
        return this;
    }

    public ItemBuilder hideAll() {
        addItemFlag(ItemFlag.HIDE_ATTRIBUTES);
        addItemFlag(ItemFlag.HIDE_DESTROYS);
        addItemFlag(ItemFlag.HIDE_ENCHANTS);
        addItemFlag(ItemFlag.HIDE_PLACED_ON);
        addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
        addItemFlag(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder setUnbreakable (boolean unbreakable) {
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setBannerColor (DyeColor color) {
        BannerMeta meta = (BannerMeta) stack.getItemMeta();
        meta.setBaseColor(color);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setHead(Player player) {
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(player);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setHead(OfflinePlayer player) {
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(player);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder displayName(String displayName) {
        ItemMeta meta = getItemMeta();

        meta.setDisplayName(StylingUtils.parseColors(displayName));
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setItemStack (ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = getItemMeta();
        meta.setLore(StylingUtils.parseColors(lore));
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder attackSpeed(double speed) {
        NBTItem nbtItem = new NBTItem(stack);

        NBTCompoundList attribute = nbtItem.getCompoundList("AttributeModifiers");
        NBTListCompound attackSpeedMod = attribute.addCompound();

        attackSpeedMod.setDouble("Amount", speed);
        attackSpeedMod.setString("AttributeName", "generic.attackSpeed");
        attackSpeedMod.setString("Name", "generic.attackSpeed");
        attackSpeedMod.setInteger("Operation", 0);
        attackSpeedMod.setInteger("UUIDLeast", 59764);
        attackSpeedMod.setInteger("UUIDMost", 31483);
        attackSpeedMod.setString("Slot", "mainhand");

        this.stack = nbtItem.getItem();
        return this;
    }

    public ItemBuilder lore(String[] lore) {
        ItemMeta meta = getItemMeta();
        meta.setLore(StylingUtils.parseColors(Arrays.asList(lore)));
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder loreLines(String... lore) {
        ItemMeta meta = getItemMeta();
        meta.setLore(StylingUtils.parseColors(Arrays.asList(lore)));
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String lore) {
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(lore);
        ItemMeta meta = getItemMeta();
        meta.setLore(StylingUtils.parseColors(loreList));
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        if (enchantment == null)
            return this;

        if (level == 0)
            return this;

        ItemMeta meta = getItemMeta();
        meta.addEnchant(enchantment, level, true);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return stack;
    }
}

