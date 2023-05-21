package ru.goldfinch.dungeons.hub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.goldfinch.dungeons.players.DungeonPlayer;

public class StorageCommand extends Command {
    public StorageCommand() {
        super("storage");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player player = (Player) sender;
        DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());

        dungeonPlayer.openStorageMenu();
        return false;
    }
}
