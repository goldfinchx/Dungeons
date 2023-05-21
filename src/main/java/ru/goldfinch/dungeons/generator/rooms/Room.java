package ru.goldfinch.dungeons.generator.rooms;

import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.regions.CuboidRegion;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.parameters.DoorsState;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.generator.rooms.types.SpawnRoom;
import ru.goldfinch.dungeons.match.player.MatchPlayer;
import ru.goldfinch.dungeons.utils.BuildUtils;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Data@NoArgsConstructor
public abstract class Room {

    private Point layoutPoint;
    private RoomType type;
    private RoomSchematic schematic;
    private RoomSchematic adaptedSchematic;
    private CuboidRegion region;
    private List<CuboidRegion> doorsRegions = new ArrayList<>();

    private List<Direction> connections;
    private Range<Integer> doorsOpeningTime;
    private DoorsState doorsState;
    private List<Location> doorsLocations = new ArrayList<>();
    private Location leverLocation;
    private Location center;

    public Room(Point layoutPoint, RoomType type, RoomSchematic schematic, List<Direction> connections, Range<Integer> doorsOpeningTime) {
        this.layoutPoint = layoutPoint;
        this.type = type;
        this.schematic = schematic;
        this.adaptedSchematic = schematic.adaptToLocation(layoutPoint);

        World world = Bukkit.getWorld(Dungeons.getInstance().getWorldName());

        this.region = new CuboidRegion(adaptedSchematic.getMinPoint(), adaptedSchematic.getMaxPoint());
        this.region.setWorld(FaweAPI.getWorld(world.getName()));
        this.center = new Location(world, region.getCenter().getX(), region.getCenter().getY(), region.getCenter().getZ());

        this.connections = connections;
        this.doorsOpeningTime = doorsOpeningTime;
        this.doorsState = DoorsState.CLOSED;

        adaptedSchematic.getDoors().forEach(vector -> doorsLocations.add(new Location(world, vector.getX(), vector.getY(), vector.getZ())));
        if (this.adaptedSchematic.getLever() != null) leverLocation = new Location(world, adaptedSchematic.getLever().getX(), adaptedSchematic.getLever().getY(), adaptedSchematic.getLever().getZ());
        if (this.adaptedSchematic.getDoors() != null && !this.adaptedSchematic.getDoors().isEmpty()) {
            this.adaptedSchematic.getDoors().forEach(vector -> {
                Location doorLocation = new Location(world, vector.getX(), vector.getY(), vector.getZ());

                CuboidRegion doorRegion = new CuboidRegion(
                        new Vector(doorLocation.getBlockX(), doorLocation.getBlockY() - 3, doorLocation.getBlockZ()),
                        new Vector(doorLocation.getBlockX(), doorLocation.getBlockY() + 3, doorLocation.getBlockZ()));
                doorRegion.setWorld(FaweAPI.getWorld(Dungeons.getInstance().getWorldName()));

                Direction doorDirection = MathUtils.getDirectionBasedOnCenter(new Vector(doorLocation.getBlockX(), doorLocation.getBlockY(), doorLocation.getBlockZ()), this.getAdaptedSchematic().getCenter());

                if (doorDirection == null) return;

                switch (doorDirection) {
                    case LEFT:
                        doorRegion.expand(new Vector(0, 0, 2));
                        doorRegion.expand(new Vector(0, 0, -2));
                        break;
                    case RIGHT:
                        doorRegion.expand(new Vector(0, 0, -2));
                        doorRegion.expand(new Vector(0, 0, 2));
                        break;
                    case UP:
                        doorRegion.expand(new Vector(2, 0, 0));
                        doorRegion.expand(new Vector(-2, 0, 0));
                        break;
                    case DOWN:
                        doorRegion.expand(new Vector(-2, 0, 0));
                        doorRegion.expand(new Vector(2, 0, 0));
                        break;
                }

                this.doorsRegions.add(doorRegion);
            });
        }

    }

    public void replaceMarkers() {
        List<Vector> markers = new ArrayList<>();

        if (this.getAdaptedSchematic().getMobsSpawners() != null && !this.getAdaptedSchematic().getMobsSpawners().isEmpty()) markers.addAll(this.getAdaptedSchematic().getMobsSpawners());
        if (this.getAdaptedSchematic().getDoors() != null && !this.getAdaptedSchematic().getDoors().isEmpty()) markers.addAll(this.getAdaptedSchematic().getDoors());
        if (this.getAdaptedSchematic().getLever() != null) markers.add(this.getAdaptedSchematic().getLever());
        if (this.getAdaptedSchematic().getBossSpawner() != null) markers.add(this.getAdaptedSchematic().getBossSpawner());
        if (this.getAdaptedSchematic().getSpawn() != null) markers.add(this.getAdaptedSchematic().getSpawn());
        if (this.getAdaptedSchematic().getDungeonLock() != null) markers.add(this.getAdaptedSchematic().getDungeonLock());

        markers.forEach(vector -> BuildUtils.setBlock(
                new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()), vector.getX(), vector.getY(), vector.getZ()),
                BlockType.AIR));

        if (this.getAdaptedSchematic().getLever() != null) BuildUtils.setBlock(leverLocation, new BaseBlock(BlockType.LEVER.getID(), 5));
        if (this.getAdaptedSchematic().getDungeonLock() != null) BuildUtils.setBlock(((SpawnRoom) this).getDungeonLockLocation(), BlockType.lookup(Dungeons.getInstance().getDungeonLockMaterial().name()));
        if (this.getAdaptedSchematic().getDoors() != null && !this.getAdaptedSchematic().getDoors().isEmpty()) {
            doorsRegions.forEach(region -> BuildUtils.replaceBlocks(
                    region,
                    BlockType.AIR,
                    BlockType.HARDENED_CLAY));
        }

    }

    public void openDoors(int openingTime) {
        this.doorsState = DoorsState.OPENING;

        if (getLeverLocation() != null && getLeverLocation().getNearbyPlayers(10).size() > 0) getLeverLocation().getNearbyPlayers(10).forEach(player -> player.sendMessage("Открываю двери следующей комнаты..."));
        new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                time++;

                if (time >= openingTime) {
                    this.cancel();

                    Bukkit.getScheduler().runTaskAsynchronously(Dungeons.getInstance(), () -> {
                        doorsState = DoorsState.OPENED;
                        getPlayersInside().forEach(matchPlayer -> matchPlayer.getBukkitPlayer().sendMessage("Двери открыты!"));

                        doorsRegions.forEach(region -> {
                            region.expand(new Vector(1, 1, 1));
                            region.expand(new Vector(-1, -1, -1));

                            BuildUtils.replaceBlocks(
                                    region,
                                    BlockType.HARDENED_CLAY,
                                    BlockType.AIR);
                        });
                    });

                    return;
                }


            }
        }.runTaskTimer(Dungeons.getInstance(), 20L, 20L);

    }
    public List<Room> getConnectedRooms() {
        List<Room> connectedRooms = new ArrayList<>();
        List<Point> connectedPoints = new ArrayList<>(Dungeons.getInstance().getDungeon().getConnectedPoints(
                this.layoutPoint,
                new ArrayList<>(Dungeons.getInstance().getDungeon().getScheme().keySet())).keySet());

        Dungeons.getInstance().getDungeon().getScheme().forEach((point, room) -> {
            if (connectedPoints.contains(point)) connectedRooms.add(room);
        });

        return connectedRooms;
    }

    public List<MatchPlayer> getPlayersInside() {
        List<MatchPlayer> players = new ArrayList<>();

        center.getNearbyPlayers(Dungeons.getInstance().getRoomsSize()/2f).forEach(player -> {
            if (player.getGameMode() == GameMode.SPECTATOR) return;
            if (MatchPlayer.get(player.getUniqueId()) == null) return;

            players.add(MatchPlayer.get(player.getUniqueId()));
        });

        return players;
    }

    public List<ActiveMob> getMobsInside() {
        List<ActiveMob> mobs = new ArrayList<>();

        center.getNearbyEntities(Dungeons.getInstance().getRoomsSize()/2f, Dungeons.getInstance().getRoomsSize()/2f, Dungeons.getInstance().getRoomsSize()/2f).forEach(entity -> {
            if (!(MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId()))) return;
            if (MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).orElse(null) == null) return;
            mobs.add(MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).get());
        });

        return mobs;
    }

}
