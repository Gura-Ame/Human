package com.github.guraame.human.idea;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.guraame.human.label.Label;
import com.github.guraame.human.parse.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class Idea extends Label<Idea> {
    private Idea(String value) {
        super(value);
    }

    @JsonCreator
    public static @NotNull Idea of(String value) {
        return new Idea(value);
    }

    public @NotNull Token toToken() {
        return Token.of(value);
    }

    public static List<Idea> of(String... values) {
        return Arrays.stream(values).map(Idea::of).toList();
    }
}