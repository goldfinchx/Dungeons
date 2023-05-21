package ru.goldfinch.dungeons.game;

import lombok.Data;
import ru.goldfinch.dungeons.Dungeons;


@Data
public class GameManager {

    private Dungeons plugin;

    public GameManager(Dungeons plugin) {
        this.plugin = plugin;
        this.loadEquipmentBalance();
    }

    public void loadEquipmentBalance() {

    }

}
