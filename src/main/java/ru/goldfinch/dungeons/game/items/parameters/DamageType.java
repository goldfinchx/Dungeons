package ru.goldfinch.dungeons.game.items.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import ru.goldfinch.dungeons.design.DungeonColor;

@AllArgsConstructor@Getter
public enum DamageType {

    PHYSICAL("Физический", DungeonColor.GOLD, "⚔", "от физ. урона"),
    FIRE("Огненный", DungeonColor.RED, "♨", "от огня"),
    FREEZE("Морозный", DungeonColor.WHITE, "❊", "от мороза"),
    POISON("Ядовитый", DungeonColor.GREEN, "☠", "от яда"),
    MAGIC("Магический", DungeonColor.BLUE, "✦", "от магии");

    private final String title;
    private final ChatColor color;
    private final String symbol;
    private final String protectionString;
}
