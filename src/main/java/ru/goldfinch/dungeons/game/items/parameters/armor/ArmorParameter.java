package ru.goldfinch.dungeons.game.items.parameters.armor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor@Getter
public enum ArmorParameter {

    ADDITIONAL_HP("Доп. здоровье", false, "к здоровью"),

    ;

    private final String title;
    private final boolean isPercentValue;
    private final String usageString;

}
