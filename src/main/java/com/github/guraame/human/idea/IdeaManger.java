package com.github.guraame.human.idea;

import com.github.guraame.human.parse.Token;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class IdeaManger {
    private static final Map<Token, Idea> ideaMap = new HashMap<>();

    public static @NotNull Optional<Idea> lookupIdeaByToken(Token token) {
        Optional<Token> tokenLookupResult = ideaMap.keySet().stream().filter(tokenInMap -> tokenInMap.equals(token)).findAny();
        return tokenLookupResult.map(ideaMap::get);
    }

    public static void addAnIdeaByToken(Token token, Idea idea) {
        ideaMap.put(token, idea);
    }
}
