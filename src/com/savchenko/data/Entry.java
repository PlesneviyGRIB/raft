package com.savchenko.data;

import java.util.Map;

public class Entry implements Map.Entry<String, String> {
    private String key;
    private String value;

    public Entry(String key, String value){
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String s) {
        value = s;
        return value;
    }
}
