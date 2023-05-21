package ru.goldfinch.dungeons.game.items;

import com.mongodb.client.model.Filters;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.game.items.parameters.ItemType;
import ru.goldfinch.dungeons.game.items.parameters.Rarity;
import ru.goldfinch.dungeons.utils.ItemBuilder;
import ru.goldfinch.dungeons.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data@NoArgsConstructor
public abstract class AbstractItem {

    private String id;
    private String title;
    private String description;
    private ItemType type;
    private Material material;
    private Rarity rarity;
    private int level;

    public AbstractItem(String title, String description, ItemType type, Material material, Rarity rarity, int level) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.id = generateId();
        this.material = material;
        this.rarity = rarity;
        this.level = level;
    }

    public ItemStack toItem() {
        List<String> lore = new ArrayList<>();
        if (description != null) {
            StringUtils.divideString(description, 25).forEach(s -> lore.add(" " + DungeonColor.GRAY + s));
            lore.add("");
        }
        lore.add(rarity.getColor() + " Информация:");
        lore.add(rarity.getColor() + " ▐ " + DungeonColor.GRAY + "Редкость: " + rarity.getColor() + rarity.getTitle());
        if (level != 0) lore.add(rarity.getColor() + " ▐ " + DungeonColor.GRAY + "Уровень: " + DungeonColor.WHITE + level);
        lore.add(rarity.getColor() + " ▐ " + DungeonColor.GRAY + "Тип: " + DungeonColor.WHITE + type.getTitle());
        lore.add("");

        ItemStack item = new ItemBuilder(material)
                .displayName(rarity.getColor() + " " + title)
                .lore(lore)
                .hideAll()
                .setUnbreakable(true)
                .build();

        NBTItem nbtItem = new NBTItem(item);

        nbtItem.setString("id", id);
        nbtItem.setString("title", title);
        nbtItem.setString("description", description);
        nbtItem.setString("type", type.name());
        nbtItem.setString("rarity", rarity.name());
        nbtItem.setInteger("level", level);

        return nbtItem.getItem();
    }

    public Document toDocument() {
        Document document = new Document();

        document.put("_id", id);
        document.put("title", title);
        document.put("description", description);
        document.put("type", type.name());
        document.put("material", material.name());
        document.put("rarity", rarity.name());
        document.put("level", level);
        return document;
    }

    private String generateId() {
        StringBuilder id = new StringBuilder();
        switch (type) {
            case WEAPON:
                id.append("WEAPON_");
                break;
            case ARMOR:
                id.append("ARMOR_");
                break;
            case USABLE:
                id.append("USABLE_");
                break;
            case GOLD:
                id.append("GOLD_");
                break;
            case KEY:
                id.append("KEY_");
                break;
            case OTHER:
                id.append("OTHER_");
                break;
        }

        Bson filter = Filters.eq("type", type.name());
        id.append(ItemsManager.getItemsCollection().countDocuments(filter));
        return id.toString();
    }

}