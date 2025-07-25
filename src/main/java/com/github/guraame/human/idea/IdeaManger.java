package com.github.guraame.human.idea;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.guraame.human.parse.Token;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class IdeaManger {
    public static void main(String[] args) throws JsonProcessingException {
        IdeaManger g = new IdeaManger();
        g.addRelation(Token.of("apple"), Idea.of("juicy", "red","circle").toArray(Idea[]::new));
        g.addRelation(Token.of("red"), Idea.of("blood"));
        g.addRelation(Token.of("juicy"), Idea.of("orange", "lemon").toArray(Idea[]::new));

        System.out.println(g.getCommonRelated(Token.of("apple")).stream().findAny().orElseThrow().get()); // -> [apple]
        System.out.println(g.toJson());
    }

    private final Map<Token, Set<Idea>> graph = new HashMap<>();

    public void addRelation(Token key, Idea @NotNull ... related) {
        for (Idea rel : related) {
            graph.computeIfAbsent(key, _ -> new HashSet<>()).add(rel);
            graph.computeIfAbsent(rel.toToken(), _ -> new HashSet<>()).add(key.toIdea()); // 雙向關聯
        }
    }

    public Set<Idea> getRelated(Token key) {
        return graph.getOrDefault(key, Set.of());
    }

    public @NotNull Set<Idea> getCommonRelated(Token @NotNull ... keywords) {
        Set<Idea> result = null;
        for (Token k : keywords) {
            Set<Idea> rel = getRelated(k);
            if (result == null) {
                result = new HashSet<>(rel);
            } else {
                result.retainAll(rel);
            }
        }
        return result != null ? result : Set.of();
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(graph);
    }

    public void linkAll(@NotNull List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);
            for (int j = i + 1; j < tokens.size(); j++) {
                Token other = tokens.get(j);
                addRelation(current, other.toIdea()); // 雙向建立
            }
        }
    }
}
