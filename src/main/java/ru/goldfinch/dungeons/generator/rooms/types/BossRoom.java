package ru.goldfinch.dungeons.generator.rooms.types;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.RoomSchematic;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.utils.BuildUtils;
import ru.goldfinch.dungeons.utils.MathUtils;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BossRoom extends Room {

    private Location bossSpawnerLocation;
    private List<Location> mobsSpawnersLocations;
    private MythicMob boss;

    public BossRoom(Point layoutPoint, RoomSchematic schematic,  List<Direction> connections) {
        super(layoutPoint, RoomType.BOSS, schematic, connections, Range.between(0, 0));
        this.mobsSpawnersLocations = new ArrayList<>();

        if (getAdaptedSchematic().getBossSpawner() != null) this.bossSpawnerLocation = new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                getAdaptedSchematic().getBossSpawner().getX(),
                getAdaptedSchematic().getBossSpawner().getY(),
                getAdaptedSchematic().getBossSpawner().getZ());

        if (getAdaptedSchematic().getMobsSpawners() != null && !getAdaptedSchematic().getMobsSpawners().isEmpty())
            for (Vector spawner : getAdaptedSchematic().getMobsSpawners())
                mobsSpawnersLocations.add(new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                        spawner.getX(),
                        spawner.getY(),
                        spawner.getZ()));

        this.boss = MythicMobs.inst().getMobManager().getMythicMob(Dungeons.getInstance().getDungeonSettings().getBosses().get(MathUtils.getRandomInteger(0, Dungeons.getInstance().getDungeonSettings().getBosses().size() - 1)));
    }

    public void spawnBoss() {
        boss.spawn(BukkitAdapter.adapt(bossSpawnerLocation), 1);
    }

    public boolean isBossAlive() {
        return getMobsInside().stream().anyMatch(mob -> mob.getType().equals(boss));
    }

}

