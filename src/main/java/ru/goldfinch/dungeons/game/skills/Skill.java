package ru.goldfinch.dungeons.game.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import ru.goldfinch.dungeons.game.skills.implementations.FireBallImpl;
import ru.goldfinch.dungeons.game.skills.implementations.SkillImpl;
import ru.goldfinch.dungeons.game.skills.implementations.StunImpl;

@AllArgsConstructor@Getter@BsonDiscriminator
public enum Skill {

    FIREBALL("Огненный шар",
            "Огненный шар наносит урон врагу",
            SkillPath.DARK_MATTER,
            false,
            1,
            15,
            FireBallImpl.get()),
    STUN("Оглушение",
            "Оглушение оглушает врага",
            SkillPath.WAR,
            false,
            1,
            15,
            StunImpl.get()),
    ;

    private String title;
    private String description;
    private SkillPath skillPath;
    private boolean isPassive;
    private int requiredLevel;
    private int cooldown;
    private SkillImpl implementation;

}
