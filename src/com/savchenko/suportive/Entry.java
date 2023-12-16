package com.savchenko.suportive;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Entry(@JsonProperty("term") Integer term, @JsonProperty("value") String value) {
    public static Entry of(Integer term, String value) {
        return new Entry(term, value);
    }
}
