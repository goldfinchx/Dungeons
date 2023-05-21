package ru.goldfinch.dungeons.events;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.game.items.types.ArmorItem;
import ru.goldfinch.dungeons.game.items.types.WeaponItem;

import java.util.HashMap;

public class DungeonDamageEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter private Entity damaged;
    @Getter private Entity damager;
    @Getter private int minecraftDamage;
    @Getter private WeaponItem weapon;
    @Getter private HashMap<EquipmentSlot, ArmorItem> armor;
    @Getter private EntityDamageEvent.DamageCause cause;
    @Getter private int finalDamage;

    public DungeonDamageEvent(Entity damaged, Entity damager, int minecraftDamage, WeaponItem weapon, HashMap<EquipmentSlot, ArmorItem> armor, EntityDamageEvent.DamageCause cause) {
        this.damaged = damaged;
        this.damager = damager;
        this.minecraftDamage = minecraftDamage;
        this.weapon = weapon;
        this.armor = armor;
        this.cause = cause;
        this.finalDamage = Dungeons.getInstance().getBattleManager().applyDamage(this);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
