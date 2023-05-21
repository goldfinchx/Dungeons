package ru.goldfinch.dungeons.generator.rooms.types;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.RoomSchematic;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.match.parameteres.MatchTeam;

import java.awt.*;
import java.util.List;

@Data@EqualsAndHashCode(callSuper = true)
public class SpawnRoom extends Room {

    @Getter private MatchTeam team;
    @Getter private Location spawnLocation;
    @Getter private Location leverLocation;
    @Getter private Location dungeonLockLocation;

    public SpawnRoom(Point layoutPoint, RoomSchematic schematic, List<Direction> connections, MatchTeam team) {
        super(layoutPoint, RoomType.SPAWN, schematic, connections, Range.between(5, 5));

        this.team = team;
        if (schematic.getSpawn() != null) this.spawnLocation = new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                getAdaptedSchematic().getSpawn().getX(),
                getAdaptedSchematic().getSpawn().getY(),
                getAdaptedSchematic().getSpawn().getZ());

        if (schematic.getLever() != null) this.leverLocation = new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                getAdaptedSchematic().getLever().getX(),
                getAdaptedSchematic().getLever().getY(),
                getAdaptedSchematic().getLever().getZ());

        if (schematic.getDungeonLock() != null) this.dungeonLockLocation = new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                getAdaptedSchematic().getDungeonLock().getX(),
                getAdaptedSchematic().getDungeonLock().getY(),
                getAdaptedSchematic().getDungeonLock().getZ());

    }

}
