package ru.goldfinch.dungeons.generator.rooms.types;

import com.sk89q.worldedit.Vector;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.RoomSchematic;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.parameters.DoorsState;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)@Data
public class MobRoom extends Room {

    private List<Location> mobsSpawnersLocations;
    private Location leverLocation;
    private List<MythicMob> availableMobs = new ArrayList<>();

    public MobRoom(Point layoutPoint, RoomSchematic schematic, List<Direction> connections) {
        super(layoutPoint, RoomType.MOB, schematic, connections, Range.between(10, 30));
        this.mobsSpawnersLocations = new ArrayList<>();

        if (getAdaptedSchematic().getMobsSpawners() != null && !getAdaptedSchematic().getMobsSpawners().isEmpty())
            for (Vector spawner : getAdaptedSchematic().getMobsSpawners())
                mobsSpawnersLocations.add(new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                        spawner.getX(),
                        spawner.getY(),
                        spawner.getZ()));

        if (getAdaptedSchematic().getLever() != null)
            this.leverLocation = new Location(Bukkit.getWorld(Dungeons.getInstance().getWorldName()),
                    getAdaptedSchematic().getLever().getX(),
                    getAdaptedSchematic().getLever().getY(),
                    getAdaptedSchematic().getLever().getZ());

        Dungeons.getInstance().getDungeonSettings().getMobs().forEach(mobName -> {
            MythicMob mob = MythicMobs.inst().getMobManager().getMythicMob(mobName);
            if (mob == null) {
                System.out.println(getSchematic().getFileName());
                System.out.println("Mob " + mobName + " not found!");
                return;
            }


            availableMobs.add(mob);
        });
    }

    @Override
    public void openDoors(int openingTime) {
        super.openDoors(openingTime);

        Bukkit.getScheduler().runTaskLater(Dungeons.getInstance(), () -> {
            int mobsAmount = 12/availableMobs.size();
            mobsAmount -= getCenter().getNearbyEntities(Dungeons.getInstance().getRoomsSize()/2f, Dungeons.getInstance().getRoomsSize()/2f, Dungeons.getInstance().getRoomsSize()/2f)
                    .stream()
                    .filter(entity -> MythicMobs.inst().getMobManager().isActiveMob(entity.getUniqueId()))
                    .count();

            for (int i = 0; i != mobsAmount; i++)
                spawnMobs(1, availableMobs.get(MathUtils.getRandomInteger(0, availableMobs.size()-1)));

        }, 20L*2);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getDoorsState() == DoorsState.OPENED)
                    cancel();

                int mobsAmount = 12/availableMobs.size();
                mobsAmount -= getMobsInside().size();

                for (int i = 0; i != mobsAmount; i++)
                    spawnMobs(1, availableMobs.get(MathUtils.getRandomInteger(0, availableMobs.size()-1)));

            }
        }.runTaskTimer(Dungeons.getInstance(), 20L*2, 20L*5);

    }

    public void spawnMobs(int amount, MythicMob mob) {
        for (int i = 0; i != amount; i++)
            mob.spawn(BukkitAdapter.adapt(mobsSpawnersLocations.get(MathUtils.getRandomInteger(0, mobsSpawnersLocations.size() - 1))), 1);
    }

    public void spawnMobs(int amount) {
        for (int i = 0; i != amount; i++)
            availableMobs.get(MathUtils.getRandomInteger(0, availableMobs.size()-1)).spawn(BukkitAdapter.adapt(mobsSpawnersLocations.get(MathUtils.getRandomInteger(0, mobsSpawnersLocations.size() - 1))), 1);
    }

}
