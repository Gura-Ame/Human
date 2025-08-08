package com.github.guraame.human.parse;

import com.github.guraame.human.debug.Debugger;
import com.github.guraame.human.grammar.GrammarGenerator;
import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.idea.IdeaManager;
import com.github.guraame.human.input.Message;
import com.github.guraame.human.output.Answer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public final class MessageParser {
    private final IdeaManager ideaManager;

    public MessageParser(IdeaManager ideaManager) {
        this.ideaManager = ideaManager;
    }

    private boolean containsHanScript(@NotNull Message message) {
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

    private List<Token> parseToken(@NotNull Message message) {
        String messageString = message.get();
        String[] stringTokens = messageString.split(" ");

        Stream<String> stringTokensStream = Arrays.stream(stringTokens);
        return stringTokensStream.map(Token::of).toList();
    }

    private final Function<String, String> filter = (s -> {
        List<String> prefix = List.of(
                "What", "What's", "This", "This's", "That", "That's", "There", "There's",
                "When", "When's", "Who", "Who's", "Whose", "How", "How's", "Will",
                "I", "I'm", "You", "You're", "He", "He's", "She", "She's", "It", "It's",
                "Your", "Mine", "Her", "His", "Its");
        for (String prefixString : prefix) {
            if (s.startsWith(prefixString)) {
                s = s.substring(prefixString.length());
            }
        }
        List<String> conj = List.of("by", "in", "or", "");
        return s;
    });

    private List<Token> parseTokenForTrain(@NotNull Message message) {
        String messageString = message.get().substring(0, (message.get().charAt(message.get().length() - 1) == '.' ? message.get().length() - 1 : message.get().length()));
        String[] stringTokens = messageString.split(" ");

        Stream<String> stringTokensStream = Arrays.stream(stringTokens)
                .map(filter);

        return stringTokensStream.map(Token::of).toList();
    }

    public Answer parseMessage(Message message) {
        if (Debugger.DEBUG && Debugger.train) {
            parseMessageAndTrain(message);
            return Answer.of("");
        }
        return parseMessageForChat(message);
    }

    public Answer parseMessageForChat(Message message) {
        if (containsHanScript(message)) return Answer.of("Unsupported Message.");
        List<Token> tokens = parseToken(message);
        List<Idea> ideas = new ArrayList<>();
        for (Token token : tokens) {
            Set<Idea> ideaLookupResult = ideaManager.getCommonRelated(token);
            if (ideaLookupResult.isEmpty()) {
                return Answer.of("I don't know.");
            }
            ideas.addAll(ideaLookupResult);
        }
        return GrammarGenerator.createAnswerFromIdeas(ideas);
    }

    public void parseMessageAndTrain(Message message) {
        if (containsHanScript(message)) throw new IllegalArgumentException("Unsupported Message.");
        List<Token> tokens = parseTokenForTrain(message);
        ideaManager.linkAll(tokens);
        System.out.println("Trained.");
    }
}
