package ru.goldfinch.dungeons.game.items.types;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.game.items.AbstractItem;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.utils.ItemBuilder;

@EqualsAndHashCode(callSuper = true)@Data
public class GoldItem extends AbstractItem {

    private int value;

    public GoldItem(int value) {
        super(Dungeons.getInstance().getItemsManager().getGoldName(value), null, ItemType.GOLD, Material.GOLD_NUGGET, Rarity.LEGENDARY, 0);
        this.setValue(value);
    }

    @Override
    public ItemStack toItem() {
/*
        List<String> lore = new ArrayList<>(super.toItem().getItemMeta().getLore());
        lore.add(getRarity().getColor() + "Параметры:");
        lore.add(getRarity().getColor() + "▐ Количество золота: &f" + this.getValue());
        lore.add("");

         */

        ItemStack itemStack = new ItemBuilder(super.toItem())
                .lore("")
               // .lore(StylingUtils.parseColors(lore))
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger("value", this.getValue());
        return nbtItem.getItem();
    }

    public static GoldItem fromItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("type") && !nbtItem.getString("type").equals(ItemType.GOLD.name())) return null;

        return new GoldItem(nbtItem.getInteger("value"));
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();
        document.append("value", this.getValue());
        return document;
    }


    public static GoldItem fromDocument(Document document) {
        return new GoldItem(document.getInteger("value"));
    }
}
