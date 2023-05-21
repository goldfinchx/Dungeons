package ru.goldfinch.dungeons.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StylingUtils {

    public static List<String> parseColors(final List<String> list){
        return list.stream().map(StylingUtils::parseColors).collect(Collectors.toList());
    }

    public static List<String> parseColors(final String[] strings){
        List<String> parseStrings = new ArrayList<>();
        for (String s : strings) {
            String parseString = parseColors(s);
            parseStrings.add(parseString);
        }

        return parseStrings;
    }

    public static String parseColors(final String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }


    public static String stripColors(final String string){
        return ChatColor.stripColor(StylingUtils.parseColors(string));
    }
}
