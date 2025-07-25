package com.github.guraame.human.debug;

public final class Debugger {
    public static class DebugException extends RuntimeException {
    }

    public static final boolean DEBUG = true;
    public static boolean train = false;

    public static void setTrain(boolean train) {
        if (!DEBUG) throw new DebugException();
        Debugger.train = train;
    }
}
