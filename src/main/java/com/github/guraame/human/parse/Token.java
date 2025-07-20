package com.github.guraame.human.parse;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Token {
    private final String token;

    private Token(String token) {
        this.token = token;
    }

    public String get() {
        return this.token;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Token of(String token) {
        return new Token(token);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Token compareToken)) return false;
        if (compareToken.get().equals(this.token)) return true;
        return super.equals(obj);
    }
}
