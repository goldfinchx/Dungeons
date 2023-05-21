package ru.goldfinch.dungeons.game.items.parameters.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor@Getter
public enum WeaponParameter {

    VAMPIRISM("Вампиризм", false, "вампиризма"),
    CRITICAL("Критический удар", true, "шанс крит. удара"),
    BLEEDING("Кровотечение", true, "шанс кровотечения"),
    STUN("Оглушение", true, "шанс оглушения"),;

    private final String title;
    private final boolean isPercentValue;
    private final String usageString;

}
