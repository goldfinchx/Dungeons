package ru.goldfinch.dungeons.match.parameteres;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.ChatColor;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.match.Match;
import ru.goldfinch.dungeons.match.player.MatchPlayer;
import ru.goldfinch.dungeons.utils.RandomCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class MatchTeam {

    @AllArgsConstructor@Getter
    public enum TeamColor {
        WHITE("Белые", 0, ChatColor.WHITE),
        ORANGE("Оранжевые", 1, ChatColor.GOLD),
        MAGENTA("Пурпурные", 2, ChatColor.LIGHT_PURPLE),
        LIGHT_BLUE("Голубые", 3, ChatColor.AQUA),
        YELLOW("Желтые", 4, ChatColor.YELLOW),
        GREEN("Зеленые", 5, ChatColor.GREEN),
        PINK("Розовые", 6, ChatColor.LIGHT_PURPLE),
        GRAY("Серые", 7, ChatColor.DARK_GRAY),
        LIGHT_GRAY("Светло-серые", 8, ChatColor.GRAY),
        CYAN("Бирюзовые", 9, ChatColor.AQUA),
        PURPLE("Фиолетовые", 10, ChatColor.DARK_PURPLE),
        BLUE("Синие", 11, ChatColor.DARK_BLUE),
        BROWN("Коричневые", 12, ChatColor.GOLD),
        GREEN_DARK("Темно-зеленые", 13, ChatColor.DARK_GREEN),
        RED("Красные", 14, ChatColor.RED),
        BLACK("Черные", 15, ChatColor.BLACK);

        private final String title;
        private final int iconData;
        private ChatColor chatColor;

        private static TeamColor getRandom(Match match) {
            RandomCollection<TeamColor> randomCollection = new RandomCollection<>();

            Arrays.stream(TeamColor.values()).forEach(teamColor -> {
                if (match.getTeams().stream().noneMatch(matchTeam -> matchTeam.getColor() == teamColor))
                    randomCollection.add(1, teamColor);
            });

            return randomCollection.next();
        }
    }

    private TeamColor color;
    private List<MatchPlayer> players;
    private int keys;

    public MatchTeam(Match match) {
        this.color = TeamColor.getRandom(match);
        this.players = new ArrayList<>();
        this.keys = 1;
    }

    public MatchTeam(Match match, MatchPlayer matchPlayer) {
        this.color = TeamColor.getRandom(match);
        this.players = new ArrayList<>();
        this.players.add(matchPlayer);
        this.keys = 1;
    }

    public void sendMessage(String message) {
        players.forEach(matchPlayer -> matchPlayer.getBukkitPlayer().sendMessage(message));
    }

    public boolean isEnoughKeys() {
        return Dungeons.getInstance().getMatchMode().getTeamsAmount() == keys;
    }
}
