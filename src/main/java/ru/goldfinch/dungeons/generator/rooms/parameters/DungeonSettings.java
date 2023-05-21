package ru.goldfinch.dungeons.generator.rooms.parameters;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.utils.ItemBuilder;
import ru.goldfinch.dungeons.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum DungeonSettings {

    MEDIEVAL("Замок",
            "Старинный замок, построенный в 12 веке и брошеный несколько десятков лет спустя, в связи со слухами о злых духах, обитающих в нем...",
            Material.IRON_SWORD,
            "medieval",
            1,
            List.of("SkeletalKnight", "SkeletalMinion", "WomanGhost"),
            List.of("SkeletonKing")),
    ICE("Лабиринт Холода",
            "Старинный замок, построенный в 12 веке и брошеный несколько десятков лет спустя, в связи со слухами о злых духах, обитающих в нем...",
            Material.ICE,
            "ice_cave",
            10,
            List.of("IceCrab", "Bear", "IceGolem"),
            List.of("Yeti"))
    ;

    DungeonSettings(String title, String description, Material iconMaterial, String styleFolder, int requiredLevel, List<String> mobs, List<String> bosses) {
        this.title = title;
        this.description = description;
        this.iconMaterial = iconMaterial;
        this.styleFolder = styleFolder;
        this.requiredLevel = requiredLevel;
        this.mobs = mobs;
        this.bosses = bosses;
    }

    private final String title;
    private final String description;
    private final Material iconMaterial;
    private final String styleFolder;
    private final int requiredLevel;
    private final List<String> mobs;
    private final List<String> bosses;

    public ItemStack toIcon() {
        List<String> lore = new ArrayList<>();
        StringUtils.divideString(description, 30).forEach(str -> lore.add(DungeonColor.GRAY + str));
        lore.add("");
        lore.add(DungeonColor.GOLD + "Информация:");
        lore.add(DungeonColor.GOLD + "▐ " + DungeonColor.GRAY + "Требуемый уровень: " + DungeonColor.WHITE + requiredLevel);
        lore.add("");

        return new ItemBuilder(iconMaterial)
                .displayName(DungeonColor.GOLD + title)
                .lore(lore)
                .hideAll()
                .build();
    }
}
