package com.github.guraame.human.grammar;

import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.output.Answer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import java.util.List;

public final class GrammarGenerator {
    private static final Realiser realiser;
    private static final NLGFactory nlgFactory;

    static {
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        nlgFactory = new NLGFactory(lexicon);
        realiser = new Realiser(lexicon);
    }

    @Contract(pure = true)
    public static @NotNull Answer createAnswerFromIdeas(@NotNull List<Idea> ideas) {
        // Only For Test
        StringBuilder builder = new StringBuilder();
        ideas.stream().map(Idea::get).forEach(builder::append);

        SPhraseSpec sentence = nlgFactory.createClause();
        sentence.setSubject("cat");
        sentence.setVerb("chase");
        sentence.setObject(builder.toString());

        return Answer.of(realiser.realiseSentence(sentence));
    }
}
