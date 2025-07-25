package com.github.guraame.human.label;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public abstract class Label<T extends Label<T>> {
    protected final String value;

    protected Label(String value) {
        this.value = value;
    }

    @JsonValue
    public String get() {
        return value;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Label<?> other = (Label<?>) obj;
        return value.equals(other.value);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
