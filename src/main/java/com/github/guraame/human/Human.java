package com.github.guraame.human;

import com.github.guraame.human.concept.BaseConceptType;
import com.github.guraame.human.concept.Concept;
import com.github.guraame.human.concept.ConceptType;
import com.github.guraame.human.debug.Debugger;
import com.github.guraame.human.idea.Idea;
import com.github.guraame.human.idea.IdeaManager;
import com.github.guraame.human.input.Message;
import com.github.guraame.human.parse.MessageParser;
import com.github.guraame.human.parse.Token;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public final class Human {
    private static final File MEMORY_FILE = new File("memory.json");
    private final IdeaManager ideaManager;

    public Human() {
        try {
            this.ideaManager = IdeaManager.load(MEMORY_FILE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load memory", e);
        }
    }

    public static void main(String[] args) {
        new Human().run();
    }

    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ideaManager.save(MEMORY_FILE);
                System.out.println("\nMemory saved.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        MessageParser messageParser = new MessageParser(ideaManager);

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
        ideaManager.addRelation(Token.of("name"), Idea.of("human"));
        Debugger.setTrain(true);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                break;
            }
            messageParser.parseMessage(Message.of(input)).printIn(System.out);
        }
    }
}
