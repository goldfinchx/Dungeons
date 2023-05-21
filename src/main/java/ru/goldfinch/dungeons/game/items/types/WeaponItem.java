package ru.goldfinch.dungeons.game.items.types;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Range;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.game.items.AbstractEquipment;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.game.items.parameters.weapon.AttackRange;
import ru.goldfinch.dungeons.game.items.parameters.weapon.AttackSpeed;
import ru.goldfinch.dungeons.game.items.parameters.weapon.WeaponParameter;
import ru.goldfinch.dungeons.utils.ItemBuilder;
import ru.goldfinch.dungeons.utils.MathUtils;
import ru.goldfinch.dungeons.utils.StylingUtils;

import java.util.HashMap;
import java.util.List;

@EqualsAndHashCode(callSuper = true)@Data
public class WeaponItem extends AbstractEquipment {

    private AttackSpeed attackSpeed;
    private AttackRange attackRange;

    private Range<Integer> damage;
    private HashMap<WeaponParameter, Integer> additionalParameters;

    public WeaponItem(String title, String description, Material material, Rarity rarity, int level, int durability,
                      AttackSpeed attackSpeed, AttackRange attackRange, Range<Integer> damage, HashMap<WeaponParameter, Integer> additionalParameters) {
        super(title, description, ItemType.WEAPON, material, rarity, level, durability);

        this.attackSpeed = attackSpeed;
        this.attackRange = attackRange;
        this.damage = damage;
        this.additionalParameters = additionalParameters;
    }

    // TODO: 09.04.2023 применять прочность
    @Override
    public ItemStack toItem() {
        List<String> lore = super.toItem().getItemMeta().getLore();

        lore.add(getRarity().getColor() + " Характеристики:");
        lore.add(getRarity().getColor() + " ▐ " + DungeonColor.GRAY + "Скорость: " + DungeonColor.WHITE + "■".repeat(attackSpeed.ordinal()+1) + "□".repeat(AttackSpeed.values().length - attackSpeed.ordinal()-1));
        lore.add(getRarity().getColor() + " ▐ " + DungeonColor.GRAY + "Дальность: " + DungeonColor.WHITE + "■".repeat(attackRange.ordinal()+1) + "□".repeat(AttackRange.values().length - attackRange.ordinal()-1));
        lore.add(getRarity().getColor() + " ▐ " + DungeonColor.GRAY + "Урон: " + DungeonColor.WHITE + damage.getMinimum() + "—" + damage.getMaximum());
        lore.add("");

        if (additionalParameters.values().stream().anyMatch(integer -> integer != 0)) {
            lore.add(getRarity().getColor() + " Доп. параметры:");

            if (additionalParameters.size() > 1) {
                MathUtils.sortByValue(additionalParameters, false).forEach((weaponParameter, value) -> {
                    if (value != 0)  lore.add(getRarity().getColor() + " ▐ " + DungeonColor.WHITE + "+" + value + (weaponParameter.isPercentValue() ? "% " : " ") + weaponParameter.getUsageString());
                });
            } else {
                WeaponParameter weaponParameter = additionalParameters.keySet().stream().findFirst().orElse(null);
                if (weaponParameter != null) lore.add(getRarity().getColor() + " ▐ " + DungeonColor.WHITE + "+" + additionalParameters.get(weaponParameter) + (weaponParameter.isPercentValue() ? "% " : " ") + weaponParameter.getUsageString());
            }

            lore.add("");
        }


        ItemStack itemStack = new ItemBuilder(super.toItem())
                .lore(StylingUtils.parseColors(lore))
                .attackSpeed(attackSpeed.getValue())
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);

        nbtItem.setString("title", getTitle());
        nbtItem.setString("attackRange", attackRange.name());
        nbtItem.setString("attackSpeed", attackSpeed.name());
        nbtItem.setInteger("damageMin", damage.getMinimum());
        nbtItem.setInteger("damageMax", damage.getMaximum());
        additionalParameters.forEach((weaponParameter, integer) -> nbtItem.setInteger(weaponParameter.name(), integer));

        return nbtItem.getItem();
    }


    public static WeaponItem fromItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("type") && !nbtItem.getString("type").equals(ItemType.WEAPON.name())) return null;

        Range<Integer> damage = Range.between(nbtItem.getInteger("damageMin"), nbtItem.getInteger("damageMax"));
        HashMap<WeaponParameter, Integer> additionalParameters = new HashMap<>();
        for (WeaponParameter value : WeaponParameter.values())
            additionalParameters.put(value, nbtItem.getInteger(value.name()));

        return new WeaponItem(
                nbtItem.getString("title"),
                nbtItem.getString("description"),
                itemStack.getType(),
                Rarity.valueOf(nbtItem.getString("rarity")),
                nbtItem.getInteger("level"),
                nbtItem.getInteger("durability"),
                AttackSpeed.valueOf(nbtItem.getString("attackSpeed")),
                AttackRange.valueOf(nbtItem.getString("attackRange")),
                damage,
                additionalParameters);
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();

        document.append("attackSpeed", attackSpeed.name());
        document.append("attackRange", attackRange.name());
        document.append("damageMin", damage.getMinimum());
        document.append("damageMax", damage.getMaximum());
        additionalParameters.forEach((weaponParameter, integer) -> document.append(weaponParameter.name(), integer));
        return document;
    }

    public static WeaponItem fromDocument(Document document) {
        Range<Integer> damage = Range.between(document.getInteger("damageMin"), document.getInteger("damageMax"));

        HashMap<WeaponParameter, Integer> additionalParameters = new HashMap<>();
        for (WeaponParameter value : WeaponParameter.values()) {
            if (document.containsKey(value.name())) additionalParameters.put(value, document.getInteger(value.name()));
        }

        return new WeaponItem(
                document.getString("title"),
                document.getString("description"),
                Material.valueOf(document.getString("material")),
                Rarity.valueOf(document.getString("rarity")),
                document.getInteger("level"),
                document.getInteger("durability"),
                AttackSpeed.valueOf(document.getString("attackSpeed")),
                AttackRange.valueOf(document.getString("attackRange")),
                damage,
                additionalParameters);
    }

    @Override
    public WeaponItem changeDurability(int value) {
        return new WeaponItem(
                getTitle(),
                getDescription(),
                getMaterial(),
                getRarity(),
                getLevel(),
                value,
                attackSpeed,
                attackRange,
                damage,
                additionalParameters
        );
    }

}
