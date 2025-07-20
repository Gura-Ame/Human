package com.github.guraame.human;

import com.github.guraame.human.concept.BaseConceptType;
import com.github.guraame.human.concept.Concept;
import com.github.guraame.human.concept.ConceptType;
import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.idea.IdeaManger;
import com.github.guraame.human.input.Message;
import com.github.guraame.human.parse.MessageParser;
import com.github.guraame.human.parse.Token;

import java.util.HashMap;
import java.util.Scanner;

public final class Human {
    public static void main(String[] args) {
        var concept = new HashMap<ConceptType, Concept<BaseConceptType>>();
        concept.put(ConceptType.NULL, new Concept<>() {
            @Override
            public BaseConceptType toSomething() {
                return BaseConceptType.NOTHING;
            }
        });
        concept.put(ConceptType.EXIST, new Concept<>() {
            @Override
            public BaseConceptType toSomething() {
                return BaseConceptType.ANY;
            }
        });
        Scanner scanner = new Scanner(System.in);
        IdeaManger.addAnIdeaByToken(Token.of("name"), Idea.of("human"));
        MessageParser.parseMessage(Message.of(scanner.nextLine())).printIn(System.out);
    }
}
