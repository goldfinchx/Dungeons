package ru.goldfinch.dungeons.generator;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.generator.rooms.types.BossRoom;
import ru.goldfinch.dungeons.generator.rooms.types.MobRoom;
import ru.goldfinch.dungeons.generator.rooms.types.SpawnRoom;
import ru.goldfinch.dungeons.match.parameteres.MatchMode;
import ru.goldfinch.dungeons.utils.BuildUtils;
import ru.goldfinch.dungeons.utils.MathUtils;
import ru.goldfinch.dungeons.utils.RandomCollection;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Dungeon {

    private final Dungeons plugin;
    private final int roomsAmount;
    private final int spawnsAmount;
    private final DungeonSettings dungeonSettings;

    private List<Point> layout;
    @Getter private HashMap<Point, Room> scheme;

    public Dungeon(Dungeons plugin, DungeonSettings dungeonSettings, MatchMode matchMode) {
        this.plugin = plugin;
        this.dungeonSettings = dungeonSettings;
        this.spawnsAmount = matchMode.getTeamsAmount();
        this.roomsAmount = matchMode.getPlayersAmount() * 3;
        this.layout = generate();
        this.scheme = setRooms();

        while (scheme == null) {
            this.layout = generate();
            this.scheme = setRooms();
        }
    }

    private List<Point> generate() {
        List<Point> layout = new ArrayList<>();

        Point start = new Point(0, 0);
        layout.add(start);

        while (layout.size() != roomsAmount) {
            RandomCollection<Point> possiblePoints = new RandomCollection<>();

            for (Point point : layout) {
                if (Arrays.stream(Direction.values()).allMatch(direction -> layout.contains(new Point(point.x + direction.getX(), point.y + direction.getY()))))
                    continue;

                List<Direction> possibleDirections = new ArrayList<>();
                for (Direction direction : Direction.values()) {
                    if (layout.contains(new Point(point.x + direction.getX(), point.y + direction.getY()))) continue;

                    for (Direction dir : Direction.values()) {
                        if (dir == direction.getOpposite()) continue;
                        if (layout.contains(new Point(point.x + direction.getX() + dir.getX(), point.y + direction.getY() + dir.getY())))
                            continue;

                        possibleDirections.add(direction);
                        break;
                    }
                }

                if (possibleDirections.isEmpty()) continue;
                possiblePoints.add(1, point);
            }

            if (possiblePoints.isEmpty())
                return layout;

            Point possiblePoint = possiblePoints.next();

            RandomCollection<Direction> possibleDirections = new RandomCollection<>();
            for (Direction direction : Direction.values()) {
                if (layout.contains(new Point(possiblePoint.x + direction.getX(), possiblePoint.y + direction.getY())))
                    continue;

                if (Arrays.stream(Direction.values()).anyMatch(dir -> {
                    if (dir == direction.getOpposite()) return false;
                    return layout.contains(new Point(possiblePoint.x + direction.getX() + dir.getX(), possiblePoint.y + direction.getY() + dir.getY()));
                })) continue;

                possibleDirections.add(50f - (MathUtils.summarizeAxisLength(possiblePoint, direction.isXAxis(), layout, 2) * 5), direction);
            }

            if (possibleDirections.isEmpty() && possiblePoints.size() == 1) return layout;
            else if (possibleDirections.isEmpty()) continue;

            Direction direction = possibleDirections.next();
            layout.add(new Point(possiblePoint.x + direction.getX(), possiblePoint.y + direction.getY()));
        }

        return layout;
    }

    private LinkedHashMap<Point, Room> setRooms() {
        LinkedHashMap<Point, Room> layoutWithRooms = new LinkedHashMap<>();
        List<Point> spawns = MathUtils.getFarthestPoints(spawnsAmount, layout);

        int i = 0;
        for (Point spawn : spawns) {
            if (Dungeons.getInstance().getBuildManager().getRandomSchematic(getConnections(spawn, layout), dungeonSettings, RoomType.SPAWN) == null)
                return null;

            layoutWithRooms.put(spawn, new SpawnRoom(
                    spawn,
                    Dungeons.getInstance().getBuildManager().getRandomSchematic(getConnections(spawn, layout), dungeonSettings, RoomType.SPAWN),
                    getConnections(spawn, layout),
                    plugin.getMatch().getTeams().get(i)));
            i++;
        }

        Point bossRoom = MathUtils.getMiddleOfPoints(spawns, layout);

        if (Dungeons.getInstance().getBuildManager().getRandomSchematic(getConnections(bossRoom, layout), dungeonSettings, RoomType.BOSS) == null)
            return null;

        layoutWithRooms.put(bossRoom, new BossRoom(
                bossRoom,
                Dungeons.getInstance().getBuildManager().getRandomSchematic(getConnections(bossRoom, layout), dungeonSettings, RoomType.BOSS),
                getConnections(bossRoom, layout)
        ));

        for (Point point : layout) {

            if (Dungeons.getInstance().getBuildManager().getRandomSchematic(getConnections(point, layout), dungeonSettings, RoomType.MOB) == null)
                return null;

            layoutWithRooms.putIfAbsent(point, new MobRoom(
                    point,
                    Dungeons.getInstance().getBuildManager().getRandomSchematic(getConnections(point, layout), dungeonSettings, RoomType.MOB),
                    getConnections(point, layout)
            ));


        }
        return layoutWithRooms;
    }

    public List<Direction> getConnections(Point point, List<Point> points) {
        return new ArrayList<>(getConnectedPoints(point, points).values());
    }

    public HashMap<Point, Direction> getConnectedPoints(Point point, List<Point> points) {
        HashMap<Point, Direction> neighbors = new HashMap<>();

        for (Direction direction : Direction.values())
            if (points.contains(new Point((int) (point.getX() + direction.getX()), (int) (point.getY() + direction.getY()))))
                neighbors.put(new Point((int) (point.getX() + direction.getX()), (int) (point.getY() + direction.getY())), direction);

        return neighbors;
    }

    public HashMap<Room, Direction> getConnectedRooms(Room room, List<Room> rooms) {
        HashMap<Room, Direction> neighbors = new HashMap<>();

        getConnectedPoints(room.getLayoutPoint(), rooms.stream().map(Room::getLayoutPoint).collect(Collectors.toList())).forEach((point, direction) -> {
            neighbors.put(rooms.stream().filter(cRoom -> cRoom.getLayoutPoint().equals(point)).findFirst().orElse(null), direction);
        });

        return neighbors;
    }

    public void build() {
        Bukkit.getScheduler().runTaskAsynchronously(Dungeons.getInstance(), () -> this.getScheme().forEach((point, room) -> {
            Clipboard schematic = BuildUtils.getSchematic(room.getAdaptedSchematic().getFileName(), "rooms/" + dungeonSettings.getStyleFolder(), Bukkit.getWorld(Dungeons.getInstance().getWorldName()), true);

            int y = room.getDoorsLocations().isEmpty() ? 100 : room.getDoorsLocations().get(0).getBlockY() - room.getAdaptedSchematic().getCenter().getBlockY() + 100;

            Location pasteLocation = new Location(
                    Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                    room.getCenter().getBlockX(),
                    y,
                    room.getCenter().getBlockZ()
            );

            BuildUtils.pasteSchem(
                    pasteLocation,
                    schematic,
                    0, room.getSchematic().getRotation(), 0);

            System.out.println("     ");
            System.out.println("File: " + room.getAdaptedSchematic().getFileName());
            System.out.println("Directions: " + Arrays.toString(room.getConnections().toArray()));
            System.out.println("Point: " + room.getLayoutPoint().x + " " + room.getLayoutPoint().y);
            System.out.println("Type: " + room.getType().name());
            System.out.println("Rotation: " + room.getSchematic().getRotation());
            System.out.println("Center: " + room.getCenter().getBlockX() + " " + room.getCenter().getBlockY() + " " + room.getCenter().getBlockZ());
            System.out.println("Doors: " + room.getDoorsLocations().size());
            if (room.getSchematic().getSpawn() != null) System.out.println("Spawn: +");
            if (room.getSchematic().getMobsSpawners() != null) System.out.println("Mob Spawners: " + room.getSchematic().getMobsSpawners().size());
            System.out.println("     ");

            Bukkit.getScheduler().runTaskLaterAsynchronously(Dungeons.getInstance(), room::replaceMarkers, 50L);
        }));


    }

    public Room findRoom(Location location) {
        return getScheme().values().stream().filter(room -> room.getRegion().contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())).findFirst().orElse(null);
    }

    public BossRoom getBossRoom() {
        return (BossRoom) getScheme().values().stream().filter(room -> room.getType() == RoomType.BOSS).findFirst().orElse(null);
    }

}
