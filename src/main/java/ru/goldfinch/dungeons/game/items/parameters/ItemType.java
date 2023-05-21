package ru.goldfinch.dungeons.game.items.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@AllArgsConstructor@Getter@BsonDiscriminator
public enum ItemType {

    WEAPON("Оружие"),
    ARMOR("Броня"),
    USABLE("Разное"),
    GOLD("Золото"),
    KEY("Ключ"),
    OTHER("Другое");

    private final String title;

}
