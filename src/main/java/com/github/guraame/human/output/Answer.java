package com.github.guraame.human.output;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

public final class Answer {
    private final String answer;

    private Answer(String answer) {
        this.answer = answer;
    }

    public String get() {
        return this.answer;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Answer of(String answer) {
        return new Answer(answer);
    }

    public void printIn(PrintStream printStream) {
        printStream.println(this.answer);
    }
}
