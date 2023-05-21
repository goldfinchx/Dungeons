package ru.goldfinch.dungeons.game.battle;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.events.DungeonDamageEvent;
import ru.goldfinch.dungeons.game.items.parameters.DamageType;
import ru.goldfinch.dungeons.game.items.types.ArmorItem;
import ru.goldfinch.dungeons.game.items.types.WeaponItem;
import ru.goldfinch.dungeons.players.DungeonPlayer;
import ru.goldfinch.dungeons.utils.MathUtils;

import java.util.HashMap;

public class BattleManager {

    private Dungeons plugin;

    public BattleManager(Dungeons plugin) {
        this.plugin = plugin;
    }

    public int applyDamage(DungeonDamageEvent e) {
        Entity damaged = e.getDamaged();
        Entity damager = e.getDamager();
        EntityDamageEvent.DamageCause cause = e.getCause();
        WeaponItem weapon = e.getWeapon();
        HashMap<EquipmentSlot, ArmorItem> armor = e.getArmor();

        int damage;
        DamageType damageType;

        switch (cause) {
            case FIRE:
            case FIRE_TICK:
                damageType = DamageType.FIRE;
                break;
            case POISON:
                damageType = DamageType.POISON;
                break;
            case WITHER:
                damageType = DamageType.FREEZE;
                break;
            case MAGIC:
                damageType = DamageType.MAGIC;
                break;
            case PROJECTILE:
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case ENTITY_EXPLOSION:
            default:
                damageType = DamageType.PHYSICAL;
                break;
        }

        if (weapon == null) {
            if (damager != null && MythicMobs.inst().getMobManager().isActiveMob(BukkitAdapter.adapt(damager))) {
                damage = (int) MythicMobs.inst().getMobManager().getActiveMob(damager.getUniqueId()).get().getDamage();
            } else {
                damage = 1;
            }
        } else {
            if (damager instanceof Player) {
                DungeonPlayer dungeonPlayer = DungeonPlayer.get(damager.getUniqueId());
                if (weapon.getLevel() > dungeonPlayer.getLevel()) {
                    damage = 1;
                    damager.sendMessage("§cВаш уровень слишком низок для использования этого предмета!");
                } else {
                    damage = MathUtils.getRandomInteger(weapon.getDamage().getMinimum(), weapon.getDamage().getMaximum());
                }
            } else {
                damage = MathUtils.getRandomInteger(weapon.getDamage().getMinimum(), weapon.getDamage().getMaximum());
            }

        }


        int protection = 0;

        for (ArmorItem armorItem : armor.values()) {
            if (armorItem == null) continue;
            if (armorItem.getProtection().get(damageType) == null) continue;

            if (damaged instanceof Player) {
                DungeonPlayer dungeonPlayer = DungeonPlayer.get(damaged.getUniqueId());
                if (armorItem.getLevel() > dungeonPlayer.getLevel()) continue;
            }

            protection = armorItem.getProtection().get(damageType);
        }

        if (damaged instanceof Player) {
            DungeonPlayer dungeonPlayer = DungeonPlayer.get(damaged.getUniqueId());

            if (armor.values().stream().anyMatch(armorItem -> armorItem != null && armorItem.getLevel() > dungeonPlayer.getLevel()))
                damaged.sendMessage("§cОдин из элементов вашей брони имеет слишком высокий уровень!");
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) damage /= 2;

        System.out.println("damage: " + damage);
        System.out.println("damageType: " + damageType.name());
        System.out.println("protection: " + protection);
        System.out.println("final damage: " + (damage - (damage * (protection / 100))));

        damage = damage - (damage * (protection / 100));
        return damage;
    }


}
