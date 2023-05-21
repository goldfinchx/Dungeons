package ru.goldfinch.dungeons.world;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.utils.BuildUtils;

import java.io.IOException;

public class WorldManager {

    @Getter private Dungeons plugin;
    @Getter private SlimePlugin slimePlugin;
    @Getter private SlimeLoader slimeLoader;
    @Getter private SlimeWorld slimeWorld;
    @Getter private SlimePropertyMap slimePropertyMap;

    public WorldManager(Dungeons plugin) {
        this.plugin = plugin;
        this.slimePlugin = (SlimePlugin) plugin.getServer().getPluginManager().getPlugin("SlimeWorldManager");
        this.slimeLoader = slimePlugin.getLoader("file");
        this.slimePropertyMap = new SlimePropertyMap();
        this.slimePropertyMap.setString(SlimeProperties.DIFFICULTY, "hard");
    }

    public void setUpWorld() {
        Bukkit.getScheduler().runTaskAsynchronously(Dungeons.getInstance(), () -> {
            try {
                this.slimeWorld = slimePlugin.createEmptyWorld(slimeLoader, plugin.getWorldName(), true, slimePropertyMap);
                Bukkit.getScheduler().runTask(Dungeons.getInstance(), () -> slimePlugin.generateWorld(slimeWorld));
                Bukkit.getScheduler().runTaskLaterAsynchronously(Dungeons.getInstance(), () -> {
                    buildGamemodeRooms();
                    setUpRules();
                }, 20 * 5);
            } catch (IOException ex) {
                ex.printStackTrace();
                plugin.getServer().spigot().restart();
            } catch (WorldAlreadyExistsException ex) {
                deleteWorld();
                setUpWorld();
            }
        });


    }

    public void deleteWorld() {
        try {
            slimeLoader.deleteWorld(plugin.getWorldName());
        } catch (IOException | UnknownWorldException ex) {
            ex.printStackTrace();
        }
    }

    public void setUpRules() {
        World world = Bukkit.getWorld(plugin.getWorldName());
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("doMobLoot", "false");
        world.setGameRuleValue("announceAdvancements", "false");
    }

    public void buildGamemodeRooms() {
        Location waitingRoom = new Location(Bukkit.getWorld(plugin.getWorldName()),
                plugin.getConfig().getDouble("settings.waiting-room.location.x"),
                plugin.getConfig().getDouble("settings.waiting-room.location.y"),
                plugin.getConfig().getDouble("settings.waiting-room.location.z"));

        Location afterDeathRoom = new Location(Bukkit.getWorld(plugin.getWorldName()),
                plugin.getConfig().getDouble("settings.after-death-room.location.x"),
                plugin.getConfig().getDouble("settings.after-death-room.location.y"),
                plugin.getConfig().getDouble("settings.after-death-room.location.z"));

        BuildUtils.pasteSchem(
                waitingRoom,
                plugin.getWaitingRoomSchemName(),
                "locations",
                0, 0, 0);

        BuildUtils.pasteSchem(
                afterDeathRoom,
                plugin.getAfterDeathSchemName(),
                "locations",
                0, 0, 0);
    }

}
