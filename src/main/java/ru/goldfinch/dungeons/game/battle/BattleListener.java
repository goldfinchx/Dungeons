package ru.goldfinch.dungeons.game.battle;


import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.events.DungeonDamageEvent;
import ru.goldfinch.dungeons.game.items.types.ArmorItem;
import ru.goldfinch.dungeons.game.items.types.WeaponItem;
import ru.goldfinch.dungeons.match.player.MatchPlayer;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.util.HashMap;
import java.util.List;

public class BattleListener implements Listener {

    private Dungeons plugin;
    private Dungeons.PluginMode mode;

    public BattleListener(Dungeons plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.mode = plugin.getMode();
    }

    @EventHandler
    public void on(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Damageable)) return;
        e.setCancelled(true);

        if (e.getEntity() instanceof Player && mode == Dungeons.PluginMode.LOBBY) return;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) || e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) return;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) ||
                e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) ||
                e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) ||
                e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) return;

        HashMap<EquipmentSlot, ArmorItem> armorItems = new HashMap<>();
        Entity damaged = e.getEntity();

        if (damaged instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) damaged;

            if (livingEntity.getEquipment().getHelmet() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getHelmet())) {
                    armorItems.put(EquipmentSlot.HEAD, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getHelmet()));
                } else {
                    armorItems.put(EquipmentSlot.HEAD, null);
                }
            }
            if (livingEntity.getEquipment().getChestplate() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getChestplate())) {
                    armorItems.put(EquipmentSlot.CHEST, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getChestplate()));
                } else {
                    armorItems.put(EquipmentSlot.CHEST, null);
                }
            }
            if (livingEntity.getEquipment().getLeggings() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getLeggings())) {
                    armorItems.put(EquipmentSlot.LEGS, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getLeggings()));
                } else {
                    armorItems.put(EquipmentSlot.LEGS, null);
                }
            }
            if (livingEntity.getEquipment().getBoots() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getBoots())) {
                    armorItems.put(EquipmentSlot.FEET, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getBoots()));
                } else {
                    armorItems.put(EquipmentSlot.FEET, null);
                }
            }
        }

        DungeonDamageEvent dungeonDamageEvent = new DungeonDamageEvent(e.getEntity(), null, (int) e.getDamage(), null, armorItems, e.getCause());
        Bukkit.getPluginManager().callEvent(dungeonDamageEvent);

        System.out.println("common damage");

        e.setDamage(dungeonDamageEvent.getFinalDamage());
        System.out.println("final damage " + e.getDamage());
        e.setCancelled(false);

    }


    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        Bukkit.getPlayer("Goldfinchx").sendMessage("EntityDamageByEntityEvent " + e.getEntity().getName() + " " + e.getDamager().getName() + " " + e.getDamage() + " " + e.getCause().name());
        e.setCancelled(true);

        if (e.getEntity() instanceof Player) {
            if (mode == Dungeons.PluginMode.LOBBY || !plugin.getMatch().getState().isPvpOn()) return;
            if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL) || e.getCause().equals(EntityDamageEvent.DamageCause.VOID))
                return;
            if (((Player) e.getEntity()).getKiller() != null) {
                Player damaged = (Player) e.getEntity();
                Player damager = ((Player) e.getEntity()).getKiller();

                MatchPlayer damagedData = MatchPlayer.get(damaged.getUniqueId());
                MatchPlayer damagerData = MatchPlayer.get(damager.getUniqueId());

                if (damagedData.getTeam() == damagerData.getTeam()) return;
            }
        }

        List<EntityDamageEvent.DamageCause> passCauses = List.of(
                EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                EntityDamageEvent.DamageCause.PROJECTILE,
                EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);

        if (!passCauses.contains(e.getCause())) return;

        WeaponItem weapon = null;
        HashMap<EquipmentSlot, ArmorItem> armorItems = new HashMap<>();

        Entity damaged = e.getEntity();
        Entity damager = e.getDamager();

        if (damaged instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) damaged;

            if (livingEntity.getEquipment().getHelmet() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getHelmet())) {
                    armorItems.put(EquipmentSlot.HEAD, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getHelmet()));
                } else {
                    armorItems.put(EquipmentSlot.HEAD, null);
                }
            }
            if (livingEntity.getEquipment().getChestplate() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getChestplate())) {
                    armorItems.put(EquipmentSlot.CHEST, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getChestplate()));
                } else {
                    armorItems.put(EquipmentSlot.CHEST, null);
                }
            }
            if (livingEntity.getEquipment().getLeggings() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getLeggings())) {
                    armorItems.put(EquipmentSlot.LEGS, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getLeggings()));
                } else {
                    armorItems.put(EquipmentSlot.LEGS, null);
                }
            }
            if (livingEntity.getEquipment().getBoots() != null) {
                if (plugin.getItemsManager().isDungeonItem(livingEntity.getEquipment().getBoots())) {
                    armorItems.put(EquipmentSlot.FEET, (ArmorItem) plugin.getItemsManager().getItem(livingEntity.getEquipment().getBoots()));
                } else {
                    armorItems.put(EquipmentSlot.FEET, null);
                }
            }
        }

        if (damager instanceof LivingEntity)
            if (plugin.getItemsManager().isDungeonItem(((LivingEntity) damager).getEquipment().getItemInMainHand()) && plugin.getItemsManager().getItem(((LivingEntity) damager).getEquipment().getItemInMainHand()) instanceof WeaponItem)
                weapon = (WeaponItem) plugin.getItemsManager().getItem(((LivingEntity) damager).getEquipment().getItemInMainHand());


        DungeonDamageEvent dungeonDamageEvent = new DungeonDamageEvent(damaged, damager, (int) e.getDamage(), weapon, armorItems, e.getCause());
        plugin.getServer().getPluginManager().callEvent(dungeonDamageEvent);

        e.setDamage(dungeonDamageEvent.getFinalDamage());
        e.setCancelled(false);

    }

    @EventHandler
    public void on(EntityRegainHealthEvent e) { e.setCancelled(true); }

    @EventHandler
    public void on(DungeonDamageEvent e) {
        System.out.println("DungeonDamageEvent " + e.getFinalDamage() + " " + e.getDamager() + " " + e.getDamaged().getName());

        if (e.getDamaged() instanceof Player) {
            Player player = (Player) e.getDamaged();
            DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());
            dungeonPlayer.setHealth(dungeonPlayer.getHealth() - e.getFinalDamage());

            int playersHealthInPercent = MathUtils.getPercentFromInteger(dungeonPlayer.getHealth(), dungeonPlayer.getMaxHealth());

            if (dungeonPlayer.getHealth() <= 0) player.setHealth(0);
            else player.setHealth(20 * (playersHealthInPercent / 100f));
        }

        Hologram damageHologram = HolographicDisplaysAPI.get(plugin).createHologram(e.getDamaged().getLocation().add(0, 2, 0));
        damageHologram.getLines().appendText(ChatColor.RED + "-" + e.getFinalDamage() + " ");

        Bukkit.getScheduler().runTaskLater(plugin, damageHologram::delete, 20);


    }
}
