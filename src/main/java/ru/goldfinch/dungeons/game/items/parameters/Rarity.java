package ru.goldfinch.dungeons.game.items.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bukkit.ChatColor;
import ru.goldfinch.dungeons.design.DungeonColor;

@AllArgsConstructor@Getter@BsonDiscriminator
public enum Rarity {

    COMMON("Обычный", DungeonColor.GREEN),
    RARE("Редкий", DungeonColor.BLUE),
    EPIC("Эпический", DungeonColor.DARK_PURPLE),
    LEGENDARY("Легендарный", DungeonColor.GOLD),;

    private final String title;
    private final ChatColor color;

}
