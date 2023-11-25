package com.savchenko.data;

public record Message(String source, Data data) {
    @Override
    public String toString() {
        return "source='" + source + "' " + data;
    }
}
