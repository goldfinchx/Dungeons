package ru.goldfinch.dungeons.hub;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.goldfinch.dungeons.Dungeons;
import ru.goldfinch.dungeons.players.DungeonPlayer;

public class DungeonsCommand extends Command {
    public DungeonsCommand() {
        super("dungeons");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Player player = (Player) sender;
        DungeonPlayer dungeonPlayer = DungeonPlayer.get(player.getUniqueId());

        Dungeons.getInstance().getHubManager().openDungeonsMenu(dungeonPlayer);
        return false;
    }
}
