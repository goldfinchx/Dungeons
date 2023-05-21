package ru.goldfinch.dungeons.world;

import com.sk89q.worldedit.Vector;
import lombok.Getter;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.generator.rooms.parameters.Direction;
import ru.goldfinch.dungeons.generator.rooms.RoomSchematic;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class BuildManager {

    private final Dungeons plugin;
    @Getter private List<RoomSchematic> roomsSchematics;

    public BuildManager(Dungeons plugin) {
        this.plugin = plugin;
        loadSchematics();

        System.out.println("Loaded " + roomsSchematics.size() + " schematics!");
        roomsSchematics.forEach(roomSchematic -> System.out.println(roomSchematic.getRoomType().name() + " " + Arrays.toString(roomSchematic.getConnections().toArray())));
    }
    public void updateFiles() {
        File file = new File(plugin.getDataFolder() + "/rooms");
        if (!file.exists()) file.mkdirs();

        System.out.println("updating schematics...");
    }

    public RoomSchematic getRandomSchematic(List<Direction> connections) {
        List<RoomSchematic> variants = filterSchematics(connections);
        return variants.get((int) (Math.random() * variants.size()));
    }

    public RoomSchematic getRandomSchematic(List<Direction> connections, DungeonSettings settings, RoomType roomType) {
        List<RoomSchematic> variants = filterSchematics(connections, settings, roomType);
        if (variants == null || variants.isEmpty()) return null;
        else return variants.get((int) (Math.random() * variants.size()));
    }


    public void loadSchematics() {
        updateFiles();

        for (DungeonSettings settings : DungeonSettings.values()) {
            File folder = new File(Dungeons.getInstance().getDataFolder() + "/rooms/" + settings.getStyleFolder());

            if (!folder.isDirectory()) return;

            roomsSchematics = new ArrayList<>();

            for (File file : folder.listFiles()) {
                String finalName = file.getName().replace(".schematic", "").toLowerCase();

                if (RoomSchematic.decodeFile(finalName, settings) == null) {
                    System.out.println(file.getName() + " cannot be translated!");
                    continue;
                }

                RoomSchematic roomSchematic = RoomSchematic.decodeFile(finalName, settings);
                roomsSchematics.add(roomSchematic);

                List<RoomSchematic> otherVariants = new ArrayList<>();
                for (int i = 1; i != 4; i++) {
                    otherVariants.add(roomSchematic.getRotated(i * 90));
                }

                roomsSchematics.addAll(otherVariants);
            }
        }
    }

    public List<RoomSchematic> filterSchematics(List<Direction> connections) {
        List<RoomSchematic> schematics = new ArrayList<>();
        roomsSchematics.stream()
                .filter(roomSchematic ->
                        new HashSet<>(roomSchematic.getConnections()).containsAll(connections) &&
                                roomSchematic.getConnections().size() == connections.size())
                .forEach(schematics::add);

        if (schematics.size() == 0) {
            System.out.println("No schematic was found with these parameters: ");
            System.out.println("Sides: " + Arrays.toString(connections.toArray()));
            return null;
        }
        return schematics;
    }
    public List<RoomSchematic> filterSchematics(List<Direction> connections, DungeonSettings settings, RoomType type) {
        List<RoomSchematic> schematics = new ArrayList<>();
        roomsSchematics.stream()
                .filter(roomSchematic ->
                        new HashSet<>(roomSchematic.getConnections()).containsAll(connections) &&
                                roomSchematic.getConnections().size() == connections.size() &&
                                settings == roomSchematic.getSettings() && type == roomSchematic.getRoomType())
                .forEach(schematics::add);

        if (schematics.size() == 0) {
            System.out.println("No schematic was found with these parameters: ");
            System.out.println("Sides: " + Arrays.toString(connections.toArray()));
            System.out.println("Settings: " + settings.name());
            System.out.println("Type: " + type);
            return null;
        }
        return schematics;
    }
    public List<RoomSchematic> filterSchematics(List<Direction> connections, DungeonSettings settings) {
        List<RoomSchematic> schematics = new ArrayList<>();
        roomsSchematics.stream()
                .filter(roomSchematic ->
                        new HashSet<>(roomSchematic.getConnections()).containsAll(connections) &&
                                roomSchematic.getConnections().size() == connections.size() &&
                        settings == roomSchematic.getSettings())
                .forEach(schematics::add);


        if (schematics.size() == 0) {
            System.out.println("No schematic was found with these parameters: ");
            System.out.println("Sides: " + Arrays.toString(connections.toArray()));
            System.out.println("Settings: " + settings.name());
            return null;
        }
        return schematics;
    }

}
