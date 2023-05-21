package ru.goldfinch.dungeons.players;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.match.Match;
import ru.goldfinch.dungeons.match.player.MatchPlayer;
import ru.goldfinch.dungeons.match.parameteres.MatchState;
import ru.goldfinch.dungeons.utils.ScoreboardService;
import ru.goldfinch.dungeons.utils.StylingUtils;
import ru.goldfinch.dungeons.utils.linkedset.LinkedSet;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

public class PlayersManager {

    private Dungeons plugin;
    @Getter private LinkedSet<DungeonPlayer> players = new LinkedSet<>();

    public PlayersManager(Dungeons plugin) {
        this.plugin = plugin;
        this.enableInterfaces();
    }

    public void enableInterfaces() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Dungeons.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
                updateHealthBar(player);

            }
        }, 0, 10);
    }

    public void updateScoreboard(Player player) {
        if (!ScoreboardService.hasScore(player)) {
            Bukkit.getScheduler().runTask(Dungeons.getInstance(), () -> {
                ScoreboardService scoreboard = ScoreboardService.createScore(player);
                scoreboard.setTitle("Dungeons");
            });
        }

        List<String> scores = new ArrayList<>();
        Format df = new DecimalFormat("#.##");

        scores.add("");
        if (plugin.getMode() == Dungeons.PluginMode.GAME) {
            Match match = Dungeons.getInstance().getMatch();

            if (MatchPlayer.get(player.getUniqueId()) != null && match.getState() == MatchState.LIVE || match.getState() == MatchState.DESTROYING) {
                MatchPlayer matchPlayer = MatchPlayer.get(player.getUniqueId());

                scores.add("Игрок");
                if (match.getMode().isTeamMode()) {
                    scores.add("Команда: " + matchPlayer.getTeam().getColor().getChatColor() + "■");
                    scores.add("Ключей: §f" + matchPlayer.getTeam().getKeys() + "/" + match.getMode().getTeamsAmount());
                    matchPlayer.getTeam().getPlayers().forEach(teammate -> {
                        String teammateInfo = (matchPlayer.isAlive() ? "&a♥ " : "&c✘ &s") + teammate.getBukkitPlayer().getName();
                        scores.add(teammateInfo);
                    });

                } else {
                    scores.add("Ключей: §f" + matchPlayer.getKeys() + "/" + (match.getMode().getPlayersAmount()/2));
                }


                scores.add(" ");
            }

            scores.add("Игра");
            scores.add("Данж: " + plugin.getDungeonSettings().getTitle());
            scores.add("Режим: " + plugin.getMatch().getMode().getTeamsAmount() + "x" + plugin.getMatch().getMode().getPlayersAmount() / plugin.getMatch().getMode().getTeamsAmount());
            scores.add("Таймер: " + plugin.getMatch().getCurrentTimer() / 60 + ":" + new DecimalFormat("00").format(Dungeons.getInstance().getMatch().getCurrentTimer() % 60));
            scores.add(" ");
        } else {
            DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());

            scores.add("Игрок");
            scores.add("Золото: " + dungeonPlayer.getGold());
            scores.add("Уровень: " + dungeonPlayer.getLevel());
            scores.add("Опыт: " + dungeonPlayer.getExperience());
            scores.add(" ");
            scores.add("Статистика");
            scores.add("К/Д: " + dungeonPlayer.getKills() + "/" + dungeonPlayer.getDeaths() + "(" + df.format(((double) dungeonPlayer.getKills()+1)/((double) dungeonPlayer.getDeaths()+1)) + ")");
            scores.add("Рейдов: " + dungeonPlayer.getRaids());
            scores.add("Побегов: " + dungeonPlayer.getEscapes());
            scores.add("  ");

        }

        if (ScoreboardService.hasScore(player))
            ScoreboardService.getByPlayer(player).setSlotsFromList(StylingUtils.parseColors(scores));

    }

    public void updateHealthBar(Player player) {
        DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());
        player.sendActionBar("§c❤ " + dungeonPlayer.getHealth() + "/" + dungeonPlayer.getMaxHealth());
    }

}
