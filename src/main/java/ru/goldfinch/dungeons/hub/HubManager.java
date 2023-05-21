package ru.goldfinch.dungeons.hub;

import org.bukkit.entity.Player;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.design.DungeonColor;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;
import ru.goldfinch.dungeons.match.parameteres.MatchMode;
import ru.goldfinch.dungeons.utils.inventoryservice.ClickableItem;
import ru.goldfinch.dungeons.utils.inventoryservice.GUI;

public class HubManager {

    private Dungeons plugin;

    public HubManager(Dungeons plugin) {
        this.plugin = plugin;
    }

    public void openDungeonsMenu(DungeonPlayer dungeonPlayer) {
        Player player = dungeonPlayer.toBukkitPlayer();

        GUI.builder()
                .title("Хранитель подземелий")
                .size(3, 9)
                .provider((inventoryUser, contents) -> {
                    int i = 1;
                    for (DungeonSettings dungeonSettings : DungeonSettings.values()) {
                        contents.set(1, i, ClickableItem.of(dungeonSettings.toIcon(), event -> {

                            if (dungeonPlayer.getLevel() >= dungeonSettings.getRequiredLevel()) {
                                openMatchModesMenu(dungeonPlayer);
                            } else {
                                player.sendMessage(DungeonColor.RED + "У вас недостаточно уровня для входа в данное подземелье!");
                            }

                        }));

                        i++;
                    }
                })
                .build().open(player);
    }

    public void openMatchModesMenu(DungeonPlayer dungeonPlayer) {
        Player player = dungeonPlayer.toBukkitPlayer();

        GUI.builder()
                .title("Выбор режима")
                .size(4, 9)
                .provider((inventoryUser, contents) -> {

                    int i = 3;
                    for (MatchMode matchMode : MatchMode.values()) {
                        if (matchMode.isTeamMode()) continue;
                        contents.set(1, i, ClickableItem.of(matchMode.toIcon(), event -> {
                            // TODO: 06.04.2023 поиск подходящего сервера и переход на него или же добавление в очередь
                        }));
                        i++;
                    }

                    i = 3;
                    for (MatchMode matchMode : MatchMode.values()) {
                        if (!matchMode.isTeamMode()) continue;
                        contents.set(2, i, ClickableItem.of(matchMode.toIcon(), event -> {
                            // TODO: 06.04.2023 поиск подходящего сервера и переход на него или же добавление в очередь
                        }));
                        i++;
                    }
                })
                .build().open(player);
    }
}
