package ru.goldfinch.dungeons.utils;

import com.destroystokyo.paper.Title;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldfinch.dungeons.Dungeons;

import java.util.HashMap;

public class PlayerUtils {

    public static boolean isItemExists(ItemStack is) {
        return is != null && is.getType() != Material.AIR;
    }

    private static final JavaPlugin plugin = Dungeons.getInstance();

    @Getter private static HashMap<Player, Integer> playersInTeleportation = new HashMap<>();

    public static void removeAllPotionEffects(Player player) {
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
    }

    public static boolean hasSpace(Player player, ItemStack item) {
        Inventory inv = player.getInventory();
        int maxStackSize = item.getMaxStackSize();

        for (ItemStack i : inv.getContents()) {
            if (i == null) {
                return true;
            }
        }

        for (ItemStack i : inv.getContents()) {
            if (i != null && i.isSimilar(item) && i.getAmount() < maxStackSize) {
                return true;
            }
        }

        return false;
    }


    public static void teleportWithCooldown(Player player, Location location, int delay) {
        BukkitRunnable runnable = new BukkitRunnable() {
            int time = delay;
            @Override
            public void run() {
                if (time == 0) {
                    player.teleport(location);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*2, 3, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20*2, 3, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*2, 6, false, false));

                    cancel();
                    playersInTeleportation.remove(player);
                } else {
                    player.sendMessage("Вы будете телепортированы через " + time + " секунд!");
                    player.sendTitle(Title.builder().title(String.valueOf(time)).subtitle("Вы будете телепортированы через...").build());

                    time--;
                }
            }
        };

        runnable.runTaskTimer(plugin, 0, 20);
        playersInTeleportation.put(player, runnable.getTaskId());
    }

    public static boolean isInventoryHaveEmptySlots(Player player) {
        Inventory inventory = player.getInventory();

        return inventory.firstEmpty() != -1;
    }


    public static void hideAllPlayers(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> player.hidePlayer(plugin, onlinePlayer));
    }

    public static void hidePlayerFromOthers(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.hidePlayer(plugin, player));
    }

    public static void showAllPlayers(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> player.showPlayer(plugin, onlinePlayer));
    }

    public static void showPlayerForOthers(Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.showPlayer(plugin, player));
    }

    public static boolean isSurfaceUnderPlayer(Location location) {
        location.add(0, 1, 0);
        Location location2 = location.add(0, 1, 0);

        return (location.getBlock().getType().equals(Material.AIR) && location2.getBlock().getType().equals(Material.AIR))
                || (location.getBlock().getType().equals(Material.WATER) && location2.getBlock().getType().equals(Material.WATER))
                || (location.getBlock().getType().equals(Material.WATER) && location2.getBlock().getType().equals(Material.AIR));
    }

    public static Location getRandomLocation(World world, int minX, int maxX, int minZ, int maxZ) {
        double x = MathUtils.getRandomInteger(minX, maxX);
        double y = MathUtils.getRandomInteger(200, 250);
        double z = MathUtils.getRandomInteger(minZ, maxZ);

        Location randomLocation = new Location(world, x, y, z);

        while (!isSurfaceUnderPlayer(randomLocation))
            randomLocation = new Location(world, x, y+1, z);

        return randomLocation;
    }

    public static boolean isInRadius(int radius, Location location, Location playerLocation) {
        return location.distance(playerLocation) <= radius;
    }
}
