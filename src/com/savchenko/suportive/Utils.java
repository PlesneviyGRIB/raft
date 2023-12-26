package com.savchenko.suportive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.savchenko.data.Data;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Utils {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String delimiter = "____";

    public static String writeObject(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Data readData(String object) {
        try {
            var obj = object.replaceAll(delimiter, " ");
            return objectMapper.readValue(obj, Data.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readObject(String object) {
        try {
            var obj = object.replaceAll(delimiter, " ");
            return objectMapper.readValue(obj, new TypeReference<T>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String shieldString(String string){
        return string.replaceAll(" ", delimiter);
    }

    public static String formatSuccess(String string, Object... objects) {
        return "\u001B[32m" + java.lang.String.format(string, objects) + "\u001B[0m";
    }

    public static String formatInfo(String string, Object... objects) {
        return "\u001B[34m" + java.lang.String.format(string, objects) + "\u001B[0m";
    }

    public static String formatError(String string, Object... objects) {
        return "\u001B[33m" + java.lang.String.format(string, objects) + "\u001B[0m";
    }

    public static Long randomize(Long value, Double aspect){
        if(aspect < 0 || aspect > 1){
            return value;
        }
        var delta = (long) (value * aspect);
        return value + ThreadLocalRandom.current().nextLong(-delta, delta);
    }

    public static void sleep(Integer milliseconds){
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
