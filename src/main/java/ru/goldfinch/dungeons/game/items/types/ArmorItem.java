package ru.goldfinch.dungeons.game.items.types;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.game.items.AbstractEquipment;
import ru.goldfinch.dungeons.game.items.parameters.DamageType;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.game.items.parameters.armor.ArmorParameter;
import ru.goldfinch.dungeons.utils.ItemBuilder;
import ru.goldfinch.dungeons.utils.MathUtils;
import ru.goldfinch.dungeons.utils.StylingUtils;

import java.util.HashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)@Data
public class ArmorItem extends AbstractEquipment {

    private HashMap<DamageType, Integer> protection;
    private HashMap<ArmorParameter, Integer> additionalParameters;

    public ArmorItem(String title, String description, Material material, Rarity rarity, int level, int durability,
                     HashMap<DamageType, Integer> protection, HashMap<ArmorParameter, Integer> additionalParameters) {

        super(title, description, ItemType.ARMOR, material, rarity, level, durability);
        this.protection = protection;
        this.protection.values().removeIf(integer -> integer == 0);
        this.additionalParameters = additionalParameters;
    }

    @Override
    public ItemStack toItem() {
        List<String> lore = super.toItem().getItemMeta().getLore();
        lore.add(getRarity().getColor() + " Защита:");
        if (protection.size() > 1) {
            MathUtils.sortByValue(protection, false).forEach((damageType, value) -> lore.add(getRarity().getColor() + " ▐ " + damageType.getColor() + damageType.getSymbol() + " +" + value + "% " + damageType.getProtectionString()));
        } else {
            protection.keySet().stream().findFirst().ifPresent(damageType -> lore.add(getRarity().getColor() + " ▐ " + damageType.getColor() + damageType.getSymbol() + " +" + protection.get(damageType) + "% " + damageType.getProtectionString()));
        }

        lore.add("");

        if (additionalParameters.values().stream().anyMatch(integer -> integer != 0)) {
            lore.add(getRarity().getColor() + " Доп. параметры:");

            if (additionalParameters.size() > 1) {
                additionalParameters.forEach((armorParameter, value) -> {
                    if (value != 0) lore.add(getRarity().getColor() + " ▐ " + DungeonColor.WHITE + "+" + value + "% " + DungeonColor.GRAY + armorParameter.getUsageString());
                });

            } else {
                ArmorParameter armorParameter = additionalParameters.keySet().stream().findFirst().orElse(null);
                if (armorParameter != null) lore.add(getRarity().getColor() + " ▐ " + DungeonColor.WHITE + "+" + additionalParameters.get(armorParameter) + "% " + DungeonColor.GRAY + armorParameter.getUsageString());
            }

            lore.add("");
        }

        ItemStack itemStack = new ItemBuilder(super.toItem())
                .lore(StylingUtils.parseColors(lore))
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("title", getTitle());
        protection.forEach((damageType, value) -> nbtItem.setInteger(damageType.name(), value));
        additionalParameters.forEach((armorParameter, value) -> nbtItem.setInteger(armorParameter.name(), value));

        return nbtItem.getItem();
    }

    public static ArmorItem fromItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("type") && !nbtItem.getString("type").equals(ItemType.ARMOR.name())) return null;

        HashMap<DamageType, Integer> protection = new HashMap<>();
        for (DamageType value : DamageType.values())
            if (nbtItem.hasKey(value.name())) protection.put(value, nbtItem.getInteger(value.name()));

        HashMap<ArmorParameter, Integer> additionalParameters = new HashMap<>();
        for (ArmorParameter value : ArmorParameter.values())
            if (nbtItem.hasKey(value.name())) additionalParameters.put(value, nbtItem.getInteger(value.name()));

        return new ArmorItem(nbtItem.getString("title"), nbtItem.getString("description"), itemStack.getType(), Rarity.valueOf(nbtItem.getString("rarity")), nbtItem.getInteger("level"), nbtItem.getInteger("durability"), protection, additionalParameters);
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();
        protection.forEach((damageType, value) -> document.append(damageType.name(), value));
        additionalParameters.forEach((armorParameter, value) -> document.append(armorParameter.name(), value));
        return document;
    }

    public static ArmorItem fromDocument(Document document) {
        HashMap<DamageType, Integer> protection = new HashMap<>();
        for (DamageType value : DamageType.values())
            if (document.containsKey(value.name())) protection.put(value, document.getInteger(value.name()));

        HashMap<ArmorParameter, Integer> additionalParameters = new HashMap<>();
        for (ArmorParameter value : ArmorParameter.values())
            if (document.containsKey(value.name())) additionalParameters.put(value, document.getInteger(value.name()));

        return new ArmorItem(document.getString("title"), document.getString("description"), Material.valueOf(document.getString("material")), Rarity.valueOf(document.getString("rarity")), document.getInteger("level"), document.getInteger("durability"), protection, additionalParameters);
    }


    @Override
    public ArmorItem changeDurability(int value) {
        return new ArmorItem(getTitle(), getDescription(), getMaterial(), getRarity(), getLevel(), getDurability() + value, protection, additionalParameters);
    }

}
