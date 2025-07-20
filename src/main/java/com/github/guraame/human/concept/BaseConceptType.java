package com.github.guraame.human.concept;

public enum BaseConceptType {
    NOTHING("nothing"),
    ANY("any"),
    MSG;

    private String msg;

    BaseConceptType() {
        this.msg = "";
    }

    BaseConceptType(String msg) {
        this.msg = msg;
    }

    public final void setMsg(String msg) {
        this.msg = msg;
    }

    public final String toMsg() {
        return this.msg;
    }
}
