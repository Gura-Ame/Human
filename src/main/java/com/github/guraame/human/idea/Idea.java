package com.github.guraame.human.idea;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Idea {
    private final String idea;

    private Idea(String idea) {
        this.idea = idea;
    }

    public String get() {
        return this.idea;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Idea of(String idea) {
        return new Idea(idea);
    }
}
