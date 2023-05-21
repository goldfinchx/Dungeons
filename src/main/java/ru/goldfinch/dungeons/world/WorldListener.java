package ru.goldfinch.dungeons.world;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.match.parameteres.MatchState;

public class WorldListener implements Listener {

    private Dungeons plugin;
    private Dungeons.PluginMode mode;

    public WorldListener(Dungeons plugin) {
        this.plugin = plugin;
        this.mode = plugin.getMode();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(BlockBreakEvent e) { e.setCancelled(true);}

    @EventHandler
    public void on(WeatherChangeEvent e) { e.setCancelled(true);}

    @EventHandler
    public void on(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }

    @EventHandler
    public void on(FoodLevelChangeEvent e) { e.setCancelled(true); }

    @EventHandler
    public void on(PlayerExpChangeEvent e) { e.setAmount(0); }

    @EventHandler
    public void on(PlayerPickupExperienceEvent e) { e.setCancelled(true); }

}
