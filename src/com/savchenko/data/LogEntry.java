package com.savchenko.data;

import java.util.Map;

public class LogEntry implements Map.Entry<Integer, String> {
    private Integer term;
    private String command;

    public LogEntry(Integer term, String command){
        this.term = term;
        this.command = command;
    }

    @Override
    public Integer getKey() {
        return term;
    }

    @Override
    public String getValue() {
        return command;
    }

    @Override
    public String setValue(String value) {
        command = value;
        return command;
    }
}
