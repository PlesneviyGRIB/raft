package com.savchenko.suportive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.savchenko.data.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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

    public static <T> T readAnyObject(String object) {
        try {
            return objectMapper.readValue(object, new TypeReference<T>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatSuccess(String string, Object... objects) {
        return "\u001B[32m" + String.format(string, objects) + "\u001B[0m";
    }

    public static String formatInfo(String string, Object... objects) {
        return "\u001B[34m" + String.format(string, objects) + "\u001B[0m";
    }

    public static String formatError(String string, Object... objects) {
        return "\u001B[33m" + String.format(string, objects) + "\u001B[0m";
    }

    public static Long randomize(Long value, Double aspect){
        if(aspect < 0 || aspect > 1){
            return value;
        }
        var delta = (long) (value * aspect);
        return value + ThreadLocalRandom.current().nextLong(-delta, delta);
    }
}
