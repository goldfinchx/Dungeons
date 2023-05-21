package ru.goldfinch.dungeons.game.items.parameters.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor@Getter
public enum AttackRange {

    VERY_SHORT(1),
    SHORT(2),
    NORMAL(3),
    LONG(4),
    VERY_LONG(5);

    private final double value;

}
