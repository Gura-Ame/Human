package com.github.guraame.human.parse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.label.Label;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class Token extends Label<Token> {
    private Token(String value) {
        super(value);
    }

    @JsonCreator
    public static @NotNull Token of(String value) {
        return new Token(value);
    }

    public @NotNull Idea toIdea() {
        return Idea.of(value);
    }

    public static List<Token> of(String... values) {
        return Arrays.stream(values).map(Token::of).toList();
    }
}
