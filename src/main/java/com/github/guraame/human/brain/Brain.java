package com.github.guraame.human.brain;

public final class Brain {
    private static final Brain INSTANCE = new Brain();

    public static Brain getInstance() {
        return Brain.INSTANCE;
    }

    private double happy;

}
