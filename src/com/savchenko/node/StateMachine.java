package com.savchenko.node;

import com.savchenko.data.LogEntry;

import java.util.List;

public class StateMachine {
    private Log log = new Log();
    public StateMachine(){
    }

    public void commit(List<LogEntry> entries){
        log.append(log.lastIndex(), entries);
    }

    public void commit(LogEntry entry){
        log.append(log.lastIndex(), List.of(entry));
    }

    public Log getLog(){
        return log;
    }
}
