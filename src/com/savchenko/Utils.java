package com.savchenko;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savchenko.data.Data;

public class Utils {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static String writeObject(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Data readObject(String object) {
        try {
            return objectMapper.readValue(object, Data.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatSuccess(String string, Object... objects) {
        return "\u001B[32m" + String.format(string, objects) + "\u001B[0m";
    }

    public static String formatError(String string, Object... objects) {
        return "\u001B[33m" + String.format(string, objects) + "\u001B[0m";
    }

}
