package ru.goldfinch.dungeons.generator.rooms.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Direction {

    UP(1, 0),
    DOWN(-1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    @Getter private final int x;
    @Getter private final int y;

    public Direction getOpposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            default:
                return LEFT;
        }
    }

    public boolean isXAxis() {
        return this == LEFT || this == RIGHT;
    }

    public Direction getRotated(int rotation) {
        switch (rotation) {
            case 90: {
                switch (this) {
                    case UP: return LEFT;
                    case DOWN: return RIGHT;
                    case LEFT: return DOWN;
                    case RIGHT: return UP;
                }
                break;
            }
            case 180: {
                return this.getOpposite();
            }
            case 270: {
                switch (this) {
                    case UP: return RIGHT;
                    case DOWN: return LEFT;
                    case LEFT: return UP;
                    case RIGHT: return DOWN;
                }

                break;
            }
        }

        return this;
    }

}