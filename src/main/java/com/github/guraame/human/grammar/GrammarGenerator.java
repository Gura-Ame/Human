package com.github.guraame.human.grammar;

import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.output.Answer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class GrammarGenerator {
    @Contract(pure = true)
    public static @NotNull Answer createAnswerFromIdeas(@NotNull List<Idea> ideas) {
        StringBuilder builder = new StringBuilder();
        ideas.stream().map(Idea::get).forEach(builder::append);
        return Answer.of(builder.toString());
    }
}
