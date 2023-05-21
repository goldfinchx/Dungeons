package ru.goldfinch.dungeons.game.skills;

import lombok.Data;
import java.util.HashMap;
import java.util.HashSet;

@Data
public class SkillTree {

    private String title;
    private HashMap<Integer, HashSet<Skill>> skills;

}
