package ru.goldfinch.dungeons.match;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.generator.rooms.types.MobRoom;
import ru.goldfinch.dungeons.generator.rooms.types.SpawnRoom;
import ru.goldfinch.dungeons.match.parameteres.MatchMode;
import ru.goldfinch.dungeons.match.parameteres.MatchState;
import ru.goldfinch.dungeons.match.parameteres.MatchTeam;
import ru.goldfinch.dungeons.match.parameteres.PlayerRemoveReason;
import ru.goldfinch.dungeons.match.player.DummyPlayer;
import ru.goldfinch.dungeons.match.player.MatchPlayer;
import ru.goldfinch.dungeons.utils.RandomCollection;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Match {

    private final Dungeons plugin;
    @Getter@Setter private MatchState state;
    @Getter@Setter private List<MatchTeam> teams;

    @Getter private final MatchMode mode;
    @Getter private final int totalEndGameTime;
    @Getter private final int totalCountdownTime;
    @Getter private final int totalMatchTime;

    @Getter private int currentTimer;
    @Getter private int countdownTaskId;
    @Getter private boolean isBossKilled;

    public Match(Dungeons plugin, MatchMode mode, int totalCountdownTime, int totalMatchTime, int totalEndGameTime) {
        this.plugin = plugin;
        this.mode = mode;
        this.totalCountdownTime = totalCountdownTime;
        this.totalMatchTime = totalMatchTime;
        this.totalEndGameTime = totalEndGameTime;
        this.currentTimer = totalCountdownTime;
        this.isBossKilled = false;

        state = MatchState.WAITING;

        this.teams = new ArrayList<>();
        for (int i = 0; i != this.mode.getTeamsAmount(); i++)
            this.teams.add(new MatchTeam(this));

        /*
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            System.out.println("==========");
            System.out.println("Match state: " + state);
            System.out.println("Teams:");
            teams.forEach(team -> {
                System.out.println(team.getColor().getTitle() + ": ");
                System.out.println("- Players: " + Arrays.toString(team.getPlayers().stream().map(matchPlayer -> matchPlayer.getBukkitPlayer().getName()).toArray()));
                System.out.println("- Keys: " + team.getKeys());
            });

            System.out.println("==========");
        }, 0L, 20L);

         */
    }

    public void setBossKilled() {
        isBossKilled = true;
        broadcast("Босс повержен! Двери подземелья открыты!");
        plugin.getDungeon().getScheme().values().forEach(room -> room.openDoors(0));

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            plugin.getDungeon().getScheme().values().stream().filter(room -> room instanceof MobRoom).forEach(room -> {
                MobRoom mobRoom = (MobRoom) room;

                if (mobRoom.getMobsInside().size() < 20)
                    mobRoom.spawnMobs(5);
            });
        }, 100, 100);

    }

    public void addPlayer(Player player) {
        addPlayer(new MatchPlayer(player));
    }

    public void addPlayer(MatchPlayer player) {
        RandomCollection<MatchTeam> randomCollection = new RandomCollection<>();
        teams.forEach(team -> {
            if (team.getPlayers().size() >= mode.getPlayersPerTeam())
                return;

            randomCollection.add(20-team.getPlayers().size()*3, team);
        });

        MatchTeam matchTeam = randomCollection.next();
        this.teams.get(this.teams.indexOf(matchTeam)).getPlayers().add(player);

        broadcast("[" + getAllPlayers().size() + "/" + mode.getPlayersAmount() + "] " + player.getBukkitPlayer().getName() + " присоединился к рейду");

        if (getAllPlayers().size() == mode.getPlayersAmount())
            runCountdown();

    }

    public void removePlayer(MatchPlayer player, PlayerRemoveReason reason, Player killer) {
        MatchTeam team = teams.stream().filter(matchTeam -> matchTeam.getPlayers().contains(player)).findFirst().orElse(null);
        if (team == null)
            throw new NullPointerException("Team is null");

        team.getPlayers().remove(player);

        switch (state) {
            case WAITING:
                broadcast("[" + getAllPlayers().size() + "/" + mode.getPlayersAmount() + "] " + player.getBukkitPlayer().getName() + " покинул рейд");
                break;
            case COUNTDOWN:
                state = MatchState.WAITING;
                Bukkit.getScheduler().cancelTask(countdownTaskId);

                broadcast("[" + getAllPlayers().size() + "/" + getAllPlayers().size() + "] " + player.getBukkitPlayer().getName() + " покинул рейд");
                broadcast("Отсчет времени остановлен");
                break;
            default:
                if (reason == PlayerRemoveReason.ESCAPED) {
                    broadcast("Игрок " + player.getBukkitPlayer().getName() + " смог сбежать из подземелья!");
                    player.getBukkitPlayer().kickPlayer("Вы смогли сбежать из подземелья!");
                    player.getGameData().setEscapes(player.getGameData().getEscapes() + 1);
                    if (getAllPlayers().size() == 0 || teams.size() == 0) restart();
                    return;
                }

                player.getGameData().kill();

                if (team.getPlayers().size() == 0) {
                    teams.remove(team);
                    broadcast("Команда " + team.getColor().getTitle() + " была уничтожена");

                    team.getPlayers().forEach(matchPlayer -> {
                        if (matchPlayer.getBukkitPlayer().isOnline()) matchPlayer.getBukkitPlayer().teleport(Dungeons.getInstance().getAfterDeathLocation());
                    });

                    MatchTeam killerTeam = teams.stream().filter(matchTeam -> matchTeam.getPlayers().stream().anyMatch(matchPlayer -> matchPlayer.getBukkitPlayer().equals(killer))).findFirst().orElse(null);
                    if (reason == PlayerRemoveReason.KILLED_BY_PLAYER && killerTeam != null) transferKeys(team, killerTeam);
                    else transferKeys(team, teams.stream().min(Comparator.comparingInt(MatchTeam::getKeys)).get());

                } else {
                    if (reason != PlayerRemoveReason.DISCONNECTED) {
                        MatchPlayer randomPlayer = team.getPlayers().get(new Random().nextInt(team.getPlayers().size()));

                        if (!(randomPlayer instanceof DummyPlayer)) {
                            player.getBukkitPlayer().setGameMode(GameMode.SPECTATOR);
                            player.getBukkitPlayer().setSpectatorTarget(randomPlayer.getBukkitPlayer());
                        }
                    }
                }

                switch (reason) {
                    case DISCONNECTED:
                        broadcast("Игрок " + player.getBukkitPlayer().getName() + " потерялся в подземелье...");
                        break;
                    case KILLED_BY_MOB:
                        broadcast("Игрок " + player.getBukkitPlayer().getName() + " был съеден монстрами...");
                        break;
                    case KILLED_BY_PLAYER:
                        broadcast("Игрок " + player.getBukkitPlayer().getName() + " был убит");
                        break;
                }

                if (getAllPlayers().size() == 0 || teams.size() == 0) restart();
                break;
        }

        if (player instanceof DummyPlayer) return;
        DungeonPlayer.get(player.getBukkitPlayer().getUniqueId()).unload();
    }

    public void removePlayer(Player player, PlayerRemoveReason reason, Player killer) {
        removePlayer(getAllPlayers().stream().filter(matchPlayer -> matchPlayer.getBukkitPlayer().equals(player)).findFirst().orElse(null), reason, killer);
    }

    public void runCountdown() {
        state = MatchState.COUNTDOWN;
        broadcast("Начало отсчета до игры!");
        currentTimer = this.totalCountdownTime;

        BukkitTask countdownTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (currentTimer == 0) {
                    start();
                    this.cancel();
                } else {
                    if (currentTimer % 10 == 0 || currentTimer < 10)
                        broadcast("Рейд начнется через " + currentTimer + " секунд");

                    currentTimer--;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
        countdownTaskId = countdownTask.getTaskId();
    }

    public void runMatchTimer() {
        currentTimer = totalMatchTime;

        new BukkitRunnable() {

            @Override
            public void run() {
                if (currentTimer == 0) {
                    startEndgame(false);
                    this.cancel();
                } else {
                    if ((currentTimer > 120 && currentTimer % 60 == 0) || (currentTimer < 120 && currentTimer > 60 && currentTimer % 30 == 0) || (currentTimer < 60 && currentTimer > 10 && currentTimer % 10 == 0) || currentTimer < 10)
                        broadcast("Осталось " + currentTimer + " секунд до конца рейда!");

                    currentTimer--;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void start() {
        state = MatchState.LIVE;

        runMatchTimer();

        for (Map.Entry<Point, Room> entry : plugin.getDungeon().getScheme().entrySet()) {
            Room room = entry.getValue();

            if (!room.getType().equals(RoomType.SPAWN)) continue;
            SpawnRoom spawnRoom = (SpawnRoom) room;

            getTeams().forEach(matchTeam -> matchTeam.getPlayers().forEach(matchPlayer -> {
                matchPlayer.getBukkitPlayer().teleport(spawnRoom.getSpawnLocation());
                matchPlayer.getGameData().setRaids(matchPlayer.getGameData().getRaids() + 1);
            }));
        }
    }

    public void startEndgame(boolean isBossKilled) {
        if (isBossKilled) setBossKilled();

        state = MatchState.DESTROYING;
        currentTimer = totalEndGameTime;

        new BukkitRunnable() {

            @Override
            public void run() {
                if (currentTimer == 0) {
                    this.cancel();
                    restart();

                } else {
                    if (currentTimer % 10 == 0 || currentTimer < 10)
                        broadcast("Осталось " + currentTimer + " секунд до разрушения подземелья!");

                    currentTimer--;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void restart() {
        state = MatchState.RESTART;

        getAllPlayers().forEach(matchPlayer -> {
            matchPlayer.getGameData().kill();
            matchPlayer.getBukkitPlayer().kickPlayer("Вы остались под завалами подземелья...");
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getServer().spigot().restart(), 20 * 3);
    }

    public void openExits() {
        // TODO: 08.04.2023
    }

    public void broadcast(String message) {
        getAllPlayers().forEach(matchPlayer -> matchPlayer.getBukkitPlayer().sendMessage(message));
    }

    public boolean isFull() {
        return teams.stream().allMatch(matchTeam -> matchTeam.getPlayers().size() == mode.getPlayersPerTeam());
    }

    public void transferKeys(MatchTeam from, MatchTeam to) {
        to.setKeys(to.getKeys()+from.getKeys());
        to.sendMessage("Ваша команда получила " + from.getKeys() + " ключей");
        to.sendMessage("Осталось собрать: " + (mode.getTeamsAmount() - to.getKeys()) + " ключей");

        if (to.getKeys() == mode.getTeamsAmount()) {
            openExits();

            to.sendMessage("Ваша команда собрала все ключи!");
            to.sendMessage("Теперь вы можете открыть выход из подземелья и сбежать!");
            to.sendMessage("Для этого найдите комнату-выход!");
        }

        from.setKeys(0);
    }

    public List<MatchPlayer> getAllPlayers() {
        List<MatchPlayer> players = new ArrayList<>();
        this.teams.forEach(matchTeam -> players.addAll(matchTeam.getPlayers()));
        return players;
    }


    @Deprecated public void removeDummy() {
        teams.stream().filter(matchTeam -> matchTeam.getPlayers().stream().anyMatch(matchPlayer -> matchPlayer instanceof DummyPlayer)).findAny().get().getPlayers().removeIf(matchPlayer -> matchPlayer instanceof DummyPlayer);

        if (Objects.requireNonNull(state) == MatchState.COUNTDOWN) {
            state = MatchState.WAITING;
            Bukkit.getScheduler().cancelTask(countdownTaskId);

            broadcast("[" + getAllPlayers().size() + "/" + mode.getPlayersAmount() + "] " + "Дамми покинул рейд");
            broadcast("Отсчет времени остановлен");
        } else {
            broadcast("[" + getAllPlayers().size() + "/" + mode.getPlayersAmount() + "] " + "Дамми покинул рейд");
        }


    }
    @Deprecated public void addDummy(MatchPlayer player) {
        addPlayer(player);
    }
}

