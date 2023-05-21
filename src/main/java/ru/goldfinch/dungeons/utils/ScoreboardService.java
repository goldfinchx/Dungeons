package ru.goldfinch.dungeons.utils;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ScoreboardService {

    private static final HashMap<UUID, ScoreboardService> players = new HashMap<>();

    public static boolean hasScore(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    public static ScoreboardService createScore(Player player) {
        return new ScoreboardService(player);
    }

    public static ScoreboardService getByPlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public static ScoreboardService removeScore(Player player) {
        return players.remove(player.getUniqueId());
    }

    private final Scoreboard scoreboard;
    private final Objective sidebar;

    private ScoreboardService(Player player) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        for(int i=1; i<=15; i++) {
            Team team = scoreboard.registerNewTeam("SLOT_" + i);
            team.addEntry(genEntry(i));
        }
        player.setScoreboard(scoreboard);
        players.put(player.getUniqueId(), this);
    }

    public void setTitle(String title) {
        sidebar.setDisplayName(title);
    }

    public void setSlot(int slot, String text) {
        Team team = scoreboard.getTeam("SLOT_" + slot);
        String entry = genEntry(slot);

        if(!scoreboard.getEntries().contains(entry))
            sidebar.getScore(entry).setScore(slot);

        team.setPrefix(text);
    }

    public void removeSlot(int slot) {
        String entry = genEntry(slot);
        if(scoreboard.getEntries().contains(entry)) {
            scoreboard.resetScores(entry);
        }
    }

    public void setSlotsFromList(List<String> list) {
        while (list.size()>15) {
            list.remove(list.size()-1);
        }

        int slot = list.size();

        if (slot<15) {
            for(int i=(slot +1); i<=15; i++) {
                removeSlot(i);
            }
        }

        for(String line : list) {
            setSlot(slot, line);
            slot--;
        }
    }

    private String genEntry(int slot) {
        return ChatColor.values()[slot].toString();
    }

}
