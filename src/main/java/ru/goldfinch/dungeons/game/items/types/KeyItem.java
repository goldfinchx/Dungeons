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
public class KeyItem extends AbstractItem {

    private int caseId;

    public KeyItem(String title, String description, Rarity rarity, int caseId) {
        super(title, description, ItemType.KEY, Material.TRIPWIRE_HOOK, rarity, 0);
        this.setCaseId(caseId);
    }

    @Override
    public ItemStack toItem() {
        NBTItem nbtItem = new NBTItem(super.toItem());
        nbtItem.setInteger("caseId", this.getCaseId());
        return nbtItem.getItem();
    }

    public static KeyItem fromItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("type") && !nbtItem.getString("type").equals(ItemType.KEY.name())) return null;

        return new KeyItem(
                nbtItem.getString("title"),
                nbtItem.getString("description"),
                Rarity.valueOf(nbtItem.getString("rarity")),
                nbtItem.getInteger("caseId"));
    }

    @Override
    public Document toDocument() {
        Document document = super.toDocument();
        document.append("caseId", this.getCaseId());
        return document;
    }


    public static AbstractItem fromDocument(Document document) {
        return new KeyItem(
                document.getString("title"),
                document.getString("description"),
                Rarity.valueOf(document.getString("rarity")),
                document.getInteger("caseId"));
    }

}
