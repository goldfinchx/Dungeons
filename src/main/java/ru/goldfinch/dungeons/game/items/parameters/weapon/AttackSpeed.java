package ru.goldfinch.dungeons.game.items.parameters.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor@Getter
public enum AttackSpeed {

    VERY_SLOW(-3.3),
    SLOW(-2.5),
    NORMAL(-2),
    FAST(-1),
    VERY_FAST(-0.5);

    private final double value;

}
