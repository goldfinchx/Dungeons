package ru.goldfinch.dungeons.match.player;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.events.PlayerChangeRoomEvent;
import ru.goldfinch.dungeons.generator.rooms.Room;
import ru.goldfinch.dungeons.generator.rooms.parameters.RoomType;
import ru.goldfinch.dungeons.generator.rooms.types.BossRoom;
import ru.goldfinch.dungeons.match.parameteres.MatchState;
import ru.goldfinch.dungeons.match.parameteres.MatchTeam;
import ru.goldfinch.dungeons.match.parameteres.PlayerRemoveReason;
import ru.goldfinch.dungeons.utils.MathUtils;

public class MatchListener implements Listener {

    private Dungeons plugin;

    public MatchListener(Dungeons plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Dungeons.PluginMode mode = plugin.getMode();

        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setGameMode(GameMode.ADVENTURE);

        if (mode == Dungeons.PluginMode.GAME) {
            player.teleport(plugin.getWaitingRoomLocation());
            plugin.getMatch().addPlayer(player);
        }
    }

    @EventHandler
    public void on(PlayerDeathEvent e) {
        if (plugin.getMatch().getState() != MatchState.LIVE && plugin.getMatch().getState() != MatchState.DESTROYING) return;
        Player player = e.getEntity();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.spigot().respawn();

            if (player.getKiller() == null && player.getKiller() != player) {
                plugin.getMatch().removePlayer(player, PlayerRemoveReason.KILLED_BY_MOB, null);
            } else {
                plugin.getMatch().removePlayer(player, PlayerRemoveReason.KILLED_BY_MOB, player.getKiller());
                DungeonPlayer killerData = DungeonPlayer.get(player.getKiller().getUniqueId());
                killerData.setKills(killerData.getKills() + 1);
            }
        }, 1L);


    }


    @EventHandler
    public void on(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        MatchPlayer matchPlayer = MatchPlayer.get(e.getPlayer().getUniqueId());
        MatchTeam matchTeam = matchPlayer.getTeam();

        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (plugin.getMatch().getState() != MatchState.LIVE && plugin.getMatch().getState() != MatchState.DESTROYING) return;

        Material clickedMaterial = e.getClickedBlock().getType();

        if (clickedMaterial == plugin.getDungeonLockMaterial()) {
            if (matchPlayer.isEnoughKeys()) {
                plugin.getMatch().removePlayer(player, PlayerRemoveReason.ESCAPED, null);
            } else {
                player.sendMessage("У вас недостаточно ключей для открытия этой двери!");
                player.sendMessage("Убейте босса или уничтожьте остальные команды, чтобы получить ключи!");
                player.sendMessage("Ключей: " + matchTeam.getKeys() + "/" + plugin.getMatchMode().getTeamsAmount());
            }
        } else if (clickedMaterial == Material.LEVER) {
            Room room = plugin.getDungeon().findRoom(e.getClickedBlock().getLocation());

            switch (room.getDoorsState()) {
                case OPENING: {
                    e.setCancelled(true);
                    player.sendMessage("Двери уже открываются!");
                    break;
                }
                case CLOSED: {
                    int openingTime = room.getType() == RoomType.SPAWN ? 5 : MathUtils.getRandomInteger(40, 90);
                    room.openDoors(openingTime);

                    if (room.getConnectedRooms().stream().anyMatch(cRoom -> cRoom.getType() == RoomType.BOSS))
                        room.getConnectedRooms().stream().filter(cRoom -> cRoom.getType() == RoomType.BOSS).forEach(cRoom -> cRoom.openDoors(openingTime));
                    break;
                }
                case OPENED: {
                    e.setCancelled(true);
                    break;
                }

            }


        }
    }

    @EventHandler
    public void on(MythicMobDeathEvent e) {
        MythicMob mythicMob = e.getMobType();
        BossRoom bossRoom = plugin.getDungeon().getBossRoom();

        if (mythicMob.equals(bossRoom.getBoss())) {
            if (plugin.getMatch().getState() == MatchState.LIVE) {
                plugin.getMatch().startEndgame(true);
            } else {
                plugin.getMatch().setBossKilled();
            }
        }

        plugin.getItemsManager().getRandomLoot(plugin.getDungeonSettings(), 0, 1).forEach(abstractItem -> {
            Location location = BukkitAdapter.adapt(e.getMob().getLocation());
            location.getWorld().dropItemNaturally(location, abstractItem.toItem());
        });

    }

    @EventHandler
    public void on(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        if (plugin.getMatch().getState() != MatchState.LIVE && plugin.getMatch().getState() != MatchState.DESTROYING) return;

        Player player = e.getPlayer();
        MatchPlayer matchPlayer = MatchPlayer.get(player.getUniqueId());

        if (matchPlayer == null || !matchPlayer.isAlive()) return;

        Room previousRoom = plugin.getDungeon().findRoom(e.getFrom());
        Room newRoom = plugin.getDungeon().findRoom(e.getTo());

        if (previousRoom == null || newRoom == null) return;
        if (previousRoom.equals(newRoom)) return;

        Bukkit.getPluginManager().callEvent(new PlayerChangeRoomEvent(player, previousRoom, newRoom));
    }

    @EventHandler
    public void on(PlayerChangeRoomEvent e) {
        if (e.getNewRoom().getType() != RoomType.BOSS)
            return;

        BossRoom bossRoom = (BossRoom) e.getNewRoom();
        if (bossRoom.isBossAlive() || plugin.getMatch().isBossKilled()) return;

        bossRoom.spawnBoss();
    }


}
