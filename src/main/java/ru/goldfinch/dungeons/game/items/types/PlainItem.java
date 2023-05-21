package ru.goldfinch.dungeons.game.items.types;

import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.game.items.AbstractItem;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;

@EqualsAndHashCode(callSuper = true)@Data
public class PlainItem extends AbstractItem {

    public PlainItem(String title, String description, Material material, Rarity rarity, int level) {
        super(title, description, ItemType.OTHER, material, rarity, level);
    }

    public static PlainItem fromItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("type") && !nbtItem.getString("type").equals(ItemType.OTHER.name())) return null;

        return new PlainItem(
                nbtItem.getString("title"),
                nbtItem.getString("description"),
                itemStack.getType(),
                Rarity.valueOf(nbtItem.getString("rarity")),
                nbtItem.getInteger("level"));
    }

    public static AbstractItem fromDocument(Document document) {
        return new PlainItem(
                document.getString("title"),
                document.getString("description"),
                Material.valueOf(document.getString("material")),
                Rarity.valueOf(document.getString("rarity")),
                document.getInteger("level"));
    }

}
