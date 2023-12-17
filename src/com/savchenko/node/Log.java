package com.savchenko.node;

import com.savchenko.data.communication.ClientMessage;
import com.savchenko.suportive.Entry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Log {
    private List<Entry> log = new LinkedList<>();

    public Log() {
    }

    public Optional<Entry> getByIndex(Integer index) {
        return index < log.size() && index > -1 ? Optional.of(log.get(index)) : Optional.empty();
    }

    public Integer lastIndex() {
        return log.size() - 1;
    }

    public void append(Integer fromIndex, List<Entry> entries) {
        if(fromIndex >= 0 && fromIndex < log.size()) {
            var newLog = log.subList(0, fromIndex);
            newLog.addAll(entries);
            log = newLog;
        }
    }

    public Pair<Integer, Integer> getStateForVoting(){
        if(log.isEmpty()){
            return Pair.of(null, null);
        }
        var lastIndex = lastIndex();
        return Pair.of(lastIndex, log.get(lastIndex).term());
    }

    public boolean isOlderThan(Integer index, Integer term){
        if(index == null || log.size() < index + 1){
            return true;
        }
        var entry = log.get(index);
        return log.size() == index + 1 && entry.term() <= term;
    }

    public List<Entry> get(){
        return log;
    }

    public void add(Integer term, ClientMessage message){
        log.add(Entry.of(term, message.value));
    }

    public void print() {
        var values = log.stream().map(e -> String.format("{ %s : %s }", e.term(), e.value())).collect(Collectors.joining(", "));
        System.out.printf("[%s]\n", values);
    }
}
