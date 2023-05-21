package ru.goldfinch.dungeons.match.player;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.match.parameteres.MatchTeam;
import ru.goldfinch.dungeons.utils.ItemBuilder;
import ru.goldfinch.dungeons.utils.inventoryservice.ClickableItem;
import ru.goldfinch.dungeons.utils.inventoryservice.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchPlayer {

    @Getter private Player bukkitPlayer;
    @Getter private DungeonPlayer gameData;
    @Getter private int keys;

    public MatchPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
        this.gameData = DungeonPlayer.get(bukkitPlayer.getUniqueId());
        this.keys = 1;
    }

    public static MatchPlayer get(UUID uuid) {
        List<MatchTeam> matchTeams = Dungeons.getInstance().getMatch().getTeams();

        if (matchTeams.stream().anyMatch(matchTeam -> matchTeam.getPlayers().stream().anyMatch(matchPlayer -> matchPlayer.getBukkitPlayer().getUniqueId().equals(uuid))))
            return matchTeams.stream().filter(matchTeam -> matchTeam.getPlayers().stream().anyMatch(matchPlayer -> matchPlayer.getBukkitPlayer().getUniqueId().equals(uuid))).findFirst().get().getPlayers().stream().filter(matchPlayer -> matchPlayer.getBukkitPlayer().getUniqueId().equals(uuid)).findFirst().get();
        else
            return null;
    }

    public MatchTeam getTeam() {
        return Dungeons.getInstance().getMatch().getTeams().stream().filter(matchTeam -> matchTeam.getPlayers().contains(this)).findFirst().orElse(null);
    }
    public void switchTeam(MatchTeam matchTeam) {
        List<MatchTeam> matchTeams = Dungeons.getInstance().getMatch().getTeams();

        if (getTeam() == matchTeam) {
            this.getBukkitPlayer().sendMessage("Вы уже находитесь в этой команде");
            return;
        }

        if (matchTeams.get(matchTeams.indexOf(matchTeam)).getPlayers().size() == Dungeons.getInstance().getMatch().getMode().getPlayersPerTeam()) {
            this.getBukkitPlayer().sendMessage("Команда " + matchTeam.getColor().getTitle() + " переполнена! Выберите другую команду");
            return;
        }

        matchTeams.get(matchTeams.indexOf(this.getTeam())).getPlayers().remove(this);
        matchTeams.get(matchTeams.indexOf(matchTeam)).getPlayers().add(this);

        this.getBukkitPlayer().sendMessage("Вы выбрали команду " + matchTeam.getColor().getTitle());
    }

    public void openTeamMenu() {
        GUI.builder()
                .title("Выбор команды")
                .size(3, 9)
                .provider((user, inventoryContents) -> {
                    List<MatchTeam> matchTeams = Dungeons.getInstance().getMatch().getTeams();

                    for (int i = 0; i < matchTeams.size(); i++) {
                        MatchTeam matchTeam = matchTeams.get(i);

                        List<String> lore = new ArrayList<>();
                        lore.add("§fИгроки:");
                        matchTeam.getPlayers().forEach(matchPlayer -> lore.add("§f" + matchPlayer.getBukkitPlayer().getName()));

                        ItemStack teamIcon = new ItemBuilder(Material.STAINED_GLASS, matchTeam.getColor().getIconData())
                                .displayName("§fКоманда " + matchTeam.getColor().getTitle())
                                .lore(lore)
                                .build();

                        inventoryContents.set(1, 2 + i, ClickableItem.of(teamIcon, e -> {
                            switchTeam(matchTeam);
                            user.closeInventory();
                        }));
                    }
                })
                .build().open(bukkitPlayer);
    }

    public boolean isAlive() {
        return getTeam() != null;
    }

    public boolean isEnoughKeys() {
        if (Dungeons.getInstance().getMatch().isBossKilled()) {
            return true;
        } else {
            if (getTeam() != null) {
                return getTeam().isEnoughKeys();
            } else {
                return this.keys >= Dungeons.getInstance().getMatch().getMode().getPlayersAmount()/2;
            }
        }

    }
}
