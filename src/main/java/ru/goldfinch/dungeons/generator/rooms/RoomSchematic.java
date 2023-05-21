package ru.goldfinch.dungeons.generator.rooms;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import lombok.Data;
import org.bukkit.Bukkit;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.utils.BuildUtils;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RoomSchematic {

    private String fileName;
    private RoomType roomType;
    private DungeonSettings settings;
    private List<Direction> connections;
    private int rotation;
    private Vector minPoint;
    private Vector maxPoint;
    private Vector center;
    private List<Vector> doors;
    private List<Vector> mobsSpawners;
    private Vector bossSpawner;
    private Vector lever;
    private Vector spawn;
    private Vector dungeonLock;

    public RoomSchematic(String fileName, int rotation, Vector minPoint, Vector maxPoint, Vector center, DungeonSettings settings, List<Direction> connections, List<Vector> doors, Vector lever, List<Vector> mobsSpawners) {
        this.fileName = fileName;
        this.roomType = RoomType.MOB;
        this.settings = settings;
        this.connections = connections;
        this.rotation = rotation;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        this.center = center;
        this.doors = doors;
        this.lever = lever;
        this.mobsSpawners = mobsSpawners;
    }

    public RoomSchematic(String fileName, int rotation, Vector minPoint, Vector maxPoint, Vector center, DungeonSettings settings, List<Direction> connections, List<Vector> doors, Vector spawn, Vector lever, Vector dungeonLock) {
        this.fileName = fileName;
        this.roomType = RoomType.SPAWN;
        this.settings = settings;
        this.connections = connections;
        this.rotation = rotation;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        this.center = center;
        this.doors = doors;
        this.spawn = spawn;
        this.lever = lever;
        this.dungeonLock = dungeonLock;
    }

    public RoomSchematic(String fileName, int rotation, Vector minPoint, Vector maxPoint, Vector center, DungeonSettings settings, List<Direction> connections, List<Vector> doors, List<Vector> mobsSpawners, Vector bossSpawner) {
        this.fileName = fileName;
        this.roomType = RoomType.BOSS;
        this.settings = settings;
        this.connections = connections;
        this.rotation = rotation;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        this.center = center;
        this.doors = doors;
        this.bossSpawner = bossSpawner;
        this.mobsSpawners = mobsSpawners;
    }

    public static RoomSchematic decodeFile(String fileName, DungeonSettings dungeonSettings) {
        String[] args = fileName.split("_");

        List<Direction> connections = new ArrayList<>();

        for (char c : args[0].toUpperCase().toCharArray()) {
            switch (c) {
                case 'N': connections.add(Direction.LEFT);
                break;

                case 'S': connections.add(Direction.RIGHT);
                break;

                case 'E': connections.add(Direction.UP);
                break;

                case 'W': connections.add(Direction.DOWN);
                break;

                default: System.out.println("Unknown connection code: " + c);
                break;

            }
        }

        RoomType roomType;
        switch (args[1].toUpperCase()) {
            case "BOSS": roomType = RoomType.BOSS;
            break;

            case "SPAWN": roomType = RoomType.SPAWN;
            break;

            default: roomType = RoomType.MOB;
            break;

        }

        if (connections.size() == 0) return null;

        HashMap<BlockVector, BaseBlock> blocks = BuildUtils.getSchematicBlocks(fileName, "/rooms/" + dungeonSettings.getStyleFolder(), Bukkit.getWorlds().get(0));
        List<Vector> doors = new ArrayList<>();
        BlockVector spawn = null;
        BlockVector boss = null;
        BlockVector lever = null;
        BlockVector dungeonLock = null;
        List<Vector> mobSpawners = new ArrayList<>();


        for (Map.Entry<BlockVector, BaseBlock> entry : blocks.entrySet()) {
            BlockVector blockVector = entry.getKey();
            BaseBlock baseBlock = entry.getValue();

            if (BlockType.fromID(baseBlock.getId()) != Dungeons.getInstance().getMarkerMaterial()) continue;

            if (baseBlock.getData() == Dungeons.getInstance().getDoorsMarkerId()) doors.add(blockVector);
            if (baseBlock.getData() == Dungeons.getInstance().getSpawnMarkerId()) spawn = blockVector;
            if (baseBlock.getData() == Dungeons.getInstance().getBossMarkerId()) boss = blockVector;
            if (baseBlock.getData() == Dungeons.getInstance().getLeverMarkerId()) lever = blockVector;
            if (baseBlock.getData() == Dungeons.getInstance().getDungeonLockMarkerId()) dungeonLock = blockVector;
            if (baseBlock.getData() == Dungeons.getInstance().getMobSpawnerMarkerId()) mobSpawners.add(blockVector);

        }

        Vector minPoint = BuildUtils.getSchematicRegion(fileName, "/rooms/" + dungeonSettings.getStyleFolder(), Bukkit.getWorlds().get(0)).getMinimumPoint();
        Vector maxPoint = BuildUtils.getSchematicRegion(fileName, "/rooms/" + dungeonSettings.getStyleFolder(), Bukkit.getWorlds().get(0)).getMaximumPoint();
        Vector center = new Vector((maxPoint.getBlockX() - minPoint.getBlockX()) / 2, (maxPoint.getBlockY() - minPoint.getBlockY()) / 2, (maxPoint.getBlockZ() - minPoint.getBlockZ()) / 2);
        Vector centerDifference;

        if (spawn != null) {
            centerDifference = spawn.subtract(center);
            spawn = new BlockVector(centerDifference.getBlockX(), spawn.getBlockY(), centerDifference.getBlockZ());
        }

        if (boss != null) {
            centerDifference = boss.subtract(center);
            boss = new BlockVector(centerDifference.getBlockX(), boss.getBlockY(), centerDifference.getBlockZ());
        }

        if (lever != null) {
            centerDifference = lever.subtract(center);
            lever = new BlockVector(centerDifference.getBlockX(), lever.getBlockY(), centerDifference.getBlockZ());
        }

        if (dungeonLock != null) {
            centerDifference = dungeonLock.subtract(center);
            dungeonLock = new BlockVector(centerDifference.getBlockX(), dungeonLock.getBlockY(), centerDifference.getBlockZ());
        }

        for (int i = 0; i < mobSpawners.size(); i++) {
            Vector mobSpawner = mobSpawners.get(i);
            centerDifference = mobSpawner.subtract(center);
            mobSpawners.set(i, new BlockVector(centerDifference.getBlockX(), mobSpawner.getBlockY(), centerDifference.getBlockZ()));
        }

        for (int i = 0; i < doors.size(); i++) {
            Vector door = doors.get(i);
            centerDifference = door.subtract(center);
            doors.set(i, new BlockVector(centerDifference.getBlockX(), door.getBlockY(), centerDifference.getBlockZ()));
        }



        switch (roomType) {
            case BOSS: return new RoomSchematic(fileName, 0, minPoint, maxPoint, center, dungeonSettings, connections, doors, mobSpawners, boss);
            case SPAWN: return new RoomSchematic(fileName, 0, minPoint, maxPoint, center, dungeonSettings, connections, doors, spawn, lever, dungeonLock);
            case MOB: return new RoomSchematic(fileName, 0, minPoint, maxPoint, center, dungeonSettings, connections, doors, lever, mobSpawners);
            default: return null;
        }

    }
    public RoomSchematic getRotated(int rotation) {
        List<Vector> rotatedDoors = new ArrayList<>();
        List<Direction> connections = new ArrayList<>();
        Vector rotatedSpawn = null;
        Vector rotatedLever = null;
        Vector rotatedDungeonLock = null;
        Vector rotatedBossSpawner = null;
        List<Vector> rotatedMobSpawners = new ArrayList<>();

        for (Direction direction : this.connections)
            connections.add(direction.getRotated(rotation));

        if (doors != null && !doors.isEmpty())
            for (Vector door : this.doors)
                rotatedDoors.add(MathUtils.getRotatedVector(door, rotation));

        if (spawn != null)
            rotatedSpawn = MathUtils.getRotatedVector(spawn, rotation);

        if (lever != null)
            rotatedLever = MathUtils.getRotatedVector(lever, rotation);

        if (dungeonLock != null)
            rotatedDungeonLock = MathUtils.getRotatedVector(dungeonLock, rotation);

        if (mobsSpawners != null && !mobsSpawners.isEmpty())
            for (Vector mobSpawner : this.mobsSpawners)
                rotatedMobSpawners.add(MathUtils.getRotatedVector(mobSpawner, rotation));

        if (bossSpawner != null)
            rotatedBossSpawner = MathUtils.getRotatedVector(bossSpawner, rotation);

        switch (roomType) {
            case BOSS: return new RoomSchematic(fileName, rotation, minPoint, maxPoint, center, settings, connections, rotatedDoors, rotatedMobSpawners, rotatedBossSpawner);
            case SPAWN: return new RoomSchematic(fileName, rotation, minPoint, maxPoint, center, settings, connections, rotatedDoors, rotatedSpawn, rotatedLever, rotatedDungeonLock);
            case MOB: return new RoomSchematic(fileName, rotation, minPoint, maxPoint, center, settings, connections, rotatedDoors, rotatedLever, rotatedMobSpawners);
            default: return null;
        }
    }

    public RoomSchematic adaptToLocation(Point point) {
        List<Vector> adaptedDoors = new ArrayList<>();

        Vector newMinPoint = new Vector(minPoint.getBlockX()+(Dungeons.getInstance().getRoomsSize()*point.x)-Dungeons.getInstance().getRoomsSize()/2f, minPoint.getY()+100, minPoint.getBlockZ()+(Dungeons.getInstance().getRoomsSize()*point.y)-Dungeons.getInstance().getRoomsSize()/2f);
        Vector newMaxPoint = new Vector(maxPoint.getBlockX()+(Dungeons.getInstance().getRoomsSize()*point.x)-Dungeons.getInstance().getRoomsSize()/2f, maxPoint.getY()+100, maxPoint.getBlockZ()+(Dungeons.getInstance().getRoomsSize()*point.y)-Dungeons.getInstance().getRoomsSize()/2f);
        Vector newCenter = new Vector((newMinPoint.getBlockX()+newMaxPoint.getBlockX())/2f, (newMinPoint.getBlockY()+newMaxPoint.getBlockY())/2f, (newMinPoint.getBlockZ()+newMaxPoint.getBlockZ())/2f);

        Vector adaptedSpawn = spawn;
        Vector adaptedBoss = bossSpawner;
        Vector adaptedLever = lever;
        Vector adaptedDungeonLock = dungeonLock;
        List<Vector> adaptedMobSpawners = new ArrayList<>();

        if (spawn != null)
            adaptedSpawn = new BlockVector(newCenter.getBlockX()+spawn.getBlockX(), spawn.getBlockY()+100, newCenter.getBlockZ()+spawn.getBlockZ());

        for (Vector door : this.doors)
            adaptedDoors.add(new BlockVector(newCenter.getBlockX()+door.getBlockX(), door.getBlockY()+100, newCenter.getBlockZ()+door.getBlockZ()));

        if (bossSpawner != null)
            adaptedBoss = new BlockVector(newCenter.getBlockX()+bossSpawner.getBlockX(), bossSpawner.getBlockY()+100, newCenter.getBlockZ()+bossSpawner.getBlockZ());

        if (lever != null)
            adaptedLever = new BlockVector(newCenter.getBlockX()+lever.getBlockX(), lever.getBlockY()+100, newCenter.getBlockZ()+lever.getBlockZ());

        if (dungeonLock != null)
            adaptedDungeonLock = new BlockVector(newCenter.getBlockX()+dungeonLock.getBlockX(), dungeonLock.getBlockY()+100, newCenter.getBlockZ()+dungeonLock.getBlockZ());

        if (mobsSpawners != null && !mobsSpawners.isEmpty())
            for (Vector mobSpawner : this.mobsSpawners)
                adaptedMobSpawners.add(new BlockVector(newCenter.getBlockX()+mobSpawner.getBlockX(), mobSpawner.getBlockY()+100, newCenter.getBlockZ()+mobSpawner.getBlockZ()));

        switch (roomType) {
            case BOSS: return new RoomSchematic(fileName, rotation, newMinPoint, newMaxPoint, newCenter, settings, connections, adaptedDoors, adaptedMobSpawners, adaptedBoss);
            case SPAWN: return new RoomSchematic(fileName, rotation, newMinPoint, newMaxPoint, newCenter, settings, connections, adaptedDoors, adaptedSpawn, adaptedLever, adaptedDungeonLock);
            case MOB: return new RoomSchematic(fileName, rotation, newMinPoint, newMaxPoint, newCenter, settings, connections, adaptedDoors, adaptedLever, adaptedMobSpawners);
            default: return null;
        }
    }

}
