package ru.goldfinch.dungeons.utils.inventoryservice;

import java.util.function.Consumer;

public class GUIListener<T> {

    private Class<T> type;
    private Consumer<T> consumer;

    public GUIListener(Class<T> type, Consumer<T> consumer) {
        this.type = type;
        this.consumer = consumer;
    }

    public void accept(T t) { consumer.accept(t); }

    public Class<T> getType() { return type; }

}
