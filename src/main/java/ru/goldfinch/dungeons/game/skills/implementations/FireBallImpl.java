package ru.goldfinch.dungeons.game.skills.implementations;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FireBallImpl implements SkillImpl {

    public int radius = 2;

    public static FireBallImpl get() {
        return new FireBallImpl();
    }

    @Override
    public void use() {}

    public FireBallImpl setRadius(int radius) {
        this.radius = radius;
        return this;
    }

}
