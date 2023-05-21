package ru.goldfinch.dungeons.game.cases;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.goldfinch.dungeons.game.items.AbstractItem;

import java.util.HashMap;

@Data@AllArgsConstructor
public class Case {

    private int id;
    private String title;
    private HashMap<AbstractItem, Integer> drop;

}
