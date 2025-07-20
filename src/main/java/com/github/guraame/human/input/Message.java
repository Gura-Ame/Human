package com.github.guraame.human.input;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Message {
    private final String message;

    private Message(String message) {
        this.message = message;
    }

    public String get() {
        return this.message;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Message of(String message) {
        return new Message(message);
    }
}
