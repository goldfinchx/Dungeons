package ru.goldfinch.dungeons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtils {

    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>(){{
        put(1000, "M");
        put(900, "CM");
        put(500, "D");
        put(400, "CD");
        put(100, "C");
        put(90, "XC");
        put(50, "L");
        put(40, "XL");
        put(10, "X");
        put(9, "IX");
        put(5, "V");
        put(4, "IV");
        put(1, "I");
        put(0, "×");
    }};

    public static String toRoman(int number) {
        int l =  map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

    public static String getProgressString(int percent) {
        int scoresToPaint = percent/10;

        StringBuilder progressLineBuilder = new StringBuilder("§f" + percent + "% " + (scoresToPaint != 0 ? "§a" : "§7"));

        for (int i = 0; i < 10; i++) {
            progressLineBuilder.append("-");

            if (i+1 == scoresToPaint)
                progressLineBuilder.append("§7");
        }

        return progressLineBuilder.toString();
    }

    public static List<String> divideString(String string, int maxLength) {
        List<String> dividedString = new ArrayList<>();
        String[] words = string.split(" ");

        StringBuilder currentString = new StringBuilder();

        for (String word : words) {
            if (currentString.length() + word.length() + 1 > maxLength) {
                dividedString.add(currentString.toString());
                currentString = new StringBuilder();
            }

            currentString.append(word).append(" ");
        }

        if (currentString.length() != 0)
            dividedString.add(currentString.toString());

        return dividedString;
    }


    public static List<String> alignStrings(List<String> stringList) {
        int longestString = 0;

        for (String string : stringList) {
            if (string.length() > longestString)
                longestString = string.length();
        }

        List<String> alignedStringList = new ArrayList<>();

        for (String string : stringList) {
            String alignedString = IntStream
                    .range(0, Math.max(0, (longestString - string.length()) / 2))
                    .mapToObj(i -> " ")
                    .collect(Collectors.joining()) + string;

            alignedStringList.add(alignedString);
        }

        return alignedStringList;
    }

}

