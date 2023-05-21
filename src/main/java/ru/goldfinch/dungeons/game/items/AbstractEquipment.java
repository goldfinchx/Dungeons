package ru.goldfinch.dungeons.game.items;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.utils.ItemBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)@Data@NoArgsConstructor
public abstract class AbstractEquipment extends AbstractItem {

    private int durability;

    public AbstractEquipment(String title, String description, ItemType type, Material material, Rarity rarity, int level, int durability) {
        super(title, description, type, material, rarity, level);
        this.durability = durability;
    }

    @Override
    public ItemStack toItem() {
        ItemStack item = super.toItem();
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        lore.remove(lore.size() - 1);
        ChatColor durabilityColor;

        if (durability >= 75) durabilityColor = DungeonColor.GREEN;
        else if (durability >= 50) durabilityColor = DungeonColor.YELLOW;
        else if (durability >= 25) durabilityColor = DungeonColor.GOLD;
        else durabilityColor = DungeonColor.RED;

        lore.add(this.getRarity().getColor() + " ▐ " + DungeonColor.GRAY + "Прочность: " + durabilityColor + durability + "%");
        lore.add("");

        item = new ItemBuilder(item)
                .lore(lore)
                .build();

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setInteger("durability", durability);
        return nbtItem.getItem();
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();
        document.append("durability", durability);
        return document;
    }

    public abstract AbstractEquipment changeDurability(int value);

}
