package ru.goldfinch.dungeons.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import ru.goldfinch.dungeons.generator.rooms.Room;

public class PlayerChangeRoomEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter@Setter private Room previousRoom;
    @Getter@Setter private Room newRoom;

    public PlayerChangeRoomEvent(Player player, Room previousRoom, Room newRoom) {
        super(player);
        this.previousRoom = previousRoom;
        this.newRoom = newRoom;
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }


}
