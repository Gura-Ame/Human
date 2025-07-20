package com.github.guraame.human.parse;

import com.github.guraame.human.grammar.GrammarGenerator;
import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.idea.IdeaManger;
import com.github.guraame.human.input.Message;
import com.github.guraame.human.output.Answer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public final class MessageParser {
    private static boolean containsHanScript(@NotNull Message message) {
        String messageString = message.get();
        for (int i = 0; i < messageString.length(); ) {
            int codepoint = messageString.codePointAt(i);

            i += Character.charCount(codepoint);
            if (Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private static List<Token> parseToken(@NotNull Message message) {
        String messageString = message.get();
        String[] stringTokens = messageString.split(" ");

        Stream<String> stringTokensStream = Arrays.stream(stringTokens);
        return stringTokensStream.map(Token::of).toList();
    }

    public static Answer parseMessage(Message message) {
        if (containsHanScript(message)) return Answer.of("Unsupported Message.");
        List<Token> tokens = parseToken(message);
        List<Idea> ideas = new ArrayList<>();
        for (Token token : tokens) {
            Optional<Idea> ideaLookupResult = IdeaManger.lookupIdeaByToken(token);
            if (ideaLookupResult.isEmpty()) {
                return Answer.of("I don't know.");
            }
            ideas.add(ideaLookupResult.get());
        }
        return GrammarGenerator.createAnswerFromIdeas(ideas);
    }
}
