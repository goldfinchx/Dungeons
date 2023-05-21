package ru.goldfinch.dungeons.match.parameteres;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor@Getter
public enum MatchState {

    WAITING("Ожидание", false),
    COUNTDOWN("Отсчет", false),
    LIVE("Рейд", true),
    DESTROYING("Разрушение", true),
    RESTART("Перезапуск", false);

    private final String title;
    private final boolean isPvpOn;

}
