package com.savchenko.node;

import com.savchenko.suportive.Entry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class StateMachine {
    private Log log = new Log();
    public StateMachine(){
    }

    public void commit(List<Entry> entries){
        log.append(log.lastIndex(), entries);
    }

    public void commit(Entry entry){
        log.append(log.lastIndex(), List.of(entry));
    }

    public Log getLog(){
        return log;
    }
}
