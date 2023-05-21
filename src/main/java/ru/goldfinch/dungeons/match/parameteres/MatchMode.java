package ru.goldfinch.dungeons.match.parameteres;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.utils.ItemBuilder;

@Getter
public enum MatchMode {

    SOLO_SMALL(8, Material.RECORD_4),
    SOLO_MEDIUM(16, Material.RECORD_3),
    SOLO_LARGE(24, Material.RECORD_6),

    TEAM_SMALL(8, 4, Material.IRON_INGOT),
    TEAM_MEDIUM(16, 4, Material.GOLD_INGOT),
    TEAM_LARGE(24, 6, Material.DIAMOND);

    private final String title;
    private final int playersAmount;
    private final int teamsAmount;
    private final int playersPerTeam;
    private boolean isTeamMode;
    private final Material iconMaterial;

    MatchMode(int playersAmount, int teamsAmount, Material iconMaterial) {
        this.title = "Командная " + teamsAmount + "x" + playersAmount / teamsAmount;
        this.playersAmount = playersAmount;
        this.teamsAmount = teamsAmount;
        this.playersPerTeam = playersAmount / teamsAmount;
        this.iconMaterial = iconMaterial;
        this.isTeamMode = true;
    }

    MatchMode(int playersAmount, Material iconMaterial) {
        this.title = "Одиночная х" + playersAmount;
        this.playersAmount = playersAmount;
        this.teamsAmount = playersAmount;
        this.playersPerTeam = 1;
        this.iconMaterial = iconMaterial;
        this.isTeamMode = false;
    }


    public ItemStack toIcon() {
        return new ItemBuilder(iconMaterial)
                .displayName(DungeonColor.GOLD + title)
                .build();
    }

}
