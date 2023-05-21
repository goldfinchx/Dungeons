package ru.goldfinch.dungeons;

import com.sk89q.worldedit.blocks.BlockType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.goldfinch.dungeons.game.battle.BattleListener;
import ru.goldfinch.dungeons.game.battle.BattleManager;
import ru.goldfinch.dungeons.data.Mongo;
import ru.goldfinch.dungeons.game.GameManager;
import ru.goldfinch.dungeons.game.items.ItemsManager;
import ru.goldfinch.dungeons.generator.Dungeon;
import ru.goldfinch.dungeons.generator.rooms.parameters.DungeonSettings;
import ru.goldfinch.dungeons.hub.DungeonsCommand;
import ru.goldfinch.dungeons.hub.HubManager;
import ru.goldfinch.dungeons.hub.StorageCommand;
import ru.goldfinch.dungeons.match.Match;
import ru.goldfinch.dungeons.match.parameteres.MatchMode;
import ru.goldfinch.dungeons.match.parameteres.PlayerRemoveReason;
import ru.goldfinch.dungeons.match.player.MatchListener;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.players.PlayerListener;
import ru.goldfinch.dungeons.players.PlayersManager;
import ru.goldfinch.dungeons.utils.ReflectionUtils;
import ru.goldfinch.dungeons.utils.inventoryservice.GUIManager;
import ru.goldfinch.dungeons.world.BuildManager;
import ru.goldfinch.dungeons.world.WorldListener;
import ru.goldfinch.dungeons.world.WorldManager;

public final class Dungeons extends JavaPlugin {

    @Getter private static Dungeons instance;

    @Getter private Mongo mongo;
    @Getter private GUIManager guiManager;

    @Getter private WorldManager worldManager;
    @Getter private GameManager gameManager;
    @Getter private ItemsManager itemsManager;
    @Getter private BuildManager buildManager;
    @Getter private PlayersManager playersManager;
    @Getter private BattleManager battleManager;
    @Getter private HubManager hubManager;
    @Getter private Match match;
    @Getter private DungeonSettings dungeonSettings;
    @Getter private Dungeon dungeon;

    @Getter private PluginMode mode;
    @Getter private String worldName;

    @Getter private int roomsSize;
    @Getter private Location waitingRoomLocation;
    @Getter private Location afterDeathLocation;
    @Getter private String waitingRoomSchemName;
    @Getter private String afterDeathSchemName;

    @Getter private MatchMode matchMode;
    @Getter private int endGameTime;
    @Getter private int countdownTime;
    @Getter private int matchTime;
    @Getter private BlockType doorsMaterial;
    @Getter private int doorsMaterialData;
    @Getter private Material doorOpenerMaterial;
    @Getter private Material dungeonLockMaterial;

    @Getter private BlockType markerMaterial;
    @Getter private int doorsMarkerId;
    @Getter private int spawnMarkerId;
    @Getter private int bossMarkerId;
    @Getter private int leverMarkerId;
    @Getter private int mobSpawnerMarkerId;
    @Getter private int dungeonLockMarkerId;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();
        this.saveConfig();

        mongo = new Mongo();
        guiManager = new GUIManager(this);
        guiManager.init();

        loadConfig();
        setUp();
    }

    @Override
    public void onDisable() {
        this.saveConfig();

        if (mode == PluginMode.GAME) {
            this.match.getAllPlayers().forEach(matchPlayer -> this.match.removePlayer(matchPlayer, PlayerRemoveReason.ESCAPED, null));
            worldManager.deleteWorld();
        } else {
            this.playersManager.getPlayers().forEach(DungeonPlayer::unload);
        }


        mongo.getMongoClient().close();
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void loadConfig() {
        this.mode = PluginMode.valueOf(this.getConfig().getString("plugin-mode").toUpperCase());
        this.roomsSize = this.getConfig().getInt("settings.rooms-size");
        this.worldName = this.getConfig().getString("settings.world-name");
        this.waitingRoomSchemName = this.getConfig().getString("settings.waiting-room.schem-name");
        this.afterDeathSchemName = this.getConfig().getString("settings.after-death-room.schem-name");
        this.endGameTime = this.getConfig().getInt("settings.end-game-time");
        this.countdownTime = this.getConfig().getInt("settings.countdown-time");
        this.matchTime = this.getConfig().getInt("settings.match-time");
        this.matchMode = MatchMode.valueOf(this.getConfig().getString("settings.match-mode").toUpperCase());
        this.doorsMaterial = BlockType.valueOf(this.getConfig().getString("settings.doors-material"));
        this.doorsMaterialData = this.getConfig().getInt("settings.doors-material-data");
        this.doorOpenerMaterial = Material.valueOf(this.getConfig().getString("settings.door-opener-material"));
        this.dungeonLockMaterial = Material.valueOf(this.getConfig().getString("settings.dungeon-lock-material"));
        this.dungeonSettings = DungeonSettings.valueOf(this.getConfig().getString("settings.dungeon-settings").toUpperCase());

        this.markerMaterial = BlockType.lookup(this.getConfig().getString("settings.schem-decoding-markers.marker-material"));
        this.doorsMarkerId = this.getConfig().getInt("settings.schem-decoding-markers.door-id");
        this.spawnMarkerId = this.getConfig().getInt("settings.schem-decoding-markers.spawn-id");
        this.bossMarkerId = this.getConfig().getInt("settings.schem-decoding-markers.boss-id");
        this.leverMarkerId = this.getConfig().getInt("settings.schem-decoding-markers.lever-id");
        this.mobSpawnerMarkerId = this.getConfig().getInt("settings.schem-decoding-markers.mob-spawner-id");
        this.dungeonLockMarkerId = this.getConfig().getInt("settings.schem-decoding-markers.dungeon-lock-id");

    }

    public void setUp() {
        playersManager = new PlayersManager(this);
        itemsManager = new ItemsManager(this);
        gameManager = new GameManager(this);
        battleManager = new BattleManager(this);

        ReflectionUtils.registerCommand("test", new TestCommand());
        new PlayerListener(this);
        new WorldListener(this);
        new BattleListener(this);

        if (mode == PluginMode.GAME) {
            worldManager = new WorldManager(this);
            worldManager.setUpWorld();

            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                buildManager = new BuildManager(this);
                match = new Match(this, matchMode, countdownTime, matchTime, endGameTime);
            }, 20 * 5);

            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                this.dungeon = new Dungeon(this, dungeonSettings, matchMode);
                this.dungeon.build();

                this.waitingRoomLocation = new Location(Bukkit.getWorld(worldName),
                        this.getConfig().getDouble("settings.waiting-room.location.x"),
                        this.getConfig().getDouble("settings.waiting-room.location.y"),
                        this.getConfig().getDouble("settings.waiting-room.location.z"));

                this.afterDeathLocation = new Location(Bukkit.getWorld(worldName),
                        this.getConfig().getDouble("settings.after-death-room.location.x"),
                        this.getConfig().getDouble("settings.after-death-room.location.y"),
                        this.getConfig().getDouble("settings.after-death-room.location.z"));
            }, 20 * 15);


            new MatchListener(this);
        } else {
            hubManager = new HubManager(this);

            ReflectionUtils.registerCommand("dungeons", new DungeonsCommand());
            ReflectionUtils.registerCommand("storage", new StorageCommand());

            /* TODO: 06.04.2023
                - загрузка всех данжей
                - телепорт на спавн
                - загрузка нпс
                - загрузка меню
             */
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Getter
    public enum PluginMode {
        GAME, LOBBY
    }

}
