package ru.goldfinch.dungeons.players;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.match.parameteres.MatchState;
import ru.goldfinch.dungeons.match.parameteres.PlayerRemoveReason;

public class PlayerListener implements Listener {

    @Getter private Dungeons plugin;
    private Dungeons.PluginMode mode;

    public PlayerListener(Dungeons plugin) {
        this.plugin = plugin;
        this.mode = plugin.getMode();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(AsyncPlayerPreLoginEvent e) {
        if (mode == Dungeons.PluginMode.LOBBY) return;

        if (plugin.getMatch().isFull() || plugin.getMatch().getState() != MatchState.WAITING)
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Игра уже началась!");
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        DungeonPlayer dungeonPlayer = DungeonPlayer.find(player.getUniqueId());

        if (dungeonPlayer == null)
            dungeonPlayer = new DungeonPlayer(player.getUniqueId());

        dungeonPlayer.setHealth(dungeonPlayer.getMaxHealth());
        player.setGameMode(GameMode.ADVENTURE);
        e.setJoinMessage(null);
    }

    @EventHandler
    public void on(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage(null);

        if (mode == Dungeons.PluginMode.GAME)
            if (plugin.getMatch().getAllPlayers().stream().filter(matchPlayer1 -> matchPlayer1.getBukkitPlayer().getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null) != null)
                plugin.getMatch().removePlayer(player, PlayerRemoveReason.DISCONNECTED, null);

        DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());
        if (dungeonPlayer != null) dungeonPlayer.unload();

    }

}
