package com.savchenko.node;

import com.savchenko.data.LogEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Log {
    private List<LogEntry> log = new ArrayList<>();

    public Log() {
    }

    public LogEntry getByIndex(Integer index) {
        return index < log.size() ? log.get(index) : null;
    }

    public Integer lastIndex() {
        return log.size() - 1;
    }

    public void append(Integer fromIndex, List<LogEntry> entries) {
        var newLog = log.subList(0, fromIndex);
        newLog.addAll(entries);
        log = newLog;
    }

    public Pair<Integer, Integer> getStateForVoting(){
        if(log.isEmpty()){
            return Pair.of(null, null);
        }
        var lastIndex = lastIndex();
        return Pair.of(lastIndex, log.get(lastIndex).getKey());
    }

    public boolean isOlderThan(Integer index, Integer term){
        if(index == null || log.size() < index + 1){
            return true;
        }
        var entry = log.get(index);
        return log.size() == index + 1 && entry.getKey() <= term;
    }

    public List<LogEntry> get(){
        return new ArrayList<>(log);
    }
}
