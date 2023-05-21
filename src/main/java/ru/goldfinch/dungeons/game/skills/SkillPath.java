package ru.goldfinch.dungeons.game.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@AllArgsConstructor@Getter@BsonDiscriminator
public enum SkillPath {

    WAR("Войны"),
    DARK_MATTER("Темной материи"),
    LIGHT("Света");

    private String title;

}
