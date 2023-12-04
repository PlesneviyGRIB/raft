package com.savchenko.data;

public record Message(Integer source, Data data) {
    @Override
    public String toString() {
        return source + " " + data;
    }
}
