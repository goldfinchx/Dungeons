package ru.goldfinch.dungeons.game.skills.implementations;

public class StunImpl implements SkillImpl {

    public static StunImpl get() {
        return new StunImpl();
    }

    @Override
    public void use() {}
}
