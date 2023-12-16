package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Entry;

import java.util.List;

public class AppendEntries extends Data {
    public Integer term;
    public Integer leaderId;
    public Integer prevLogIndex;
    public Integer prevLogTerm;
    public List<Entry> entries;
    public Integer leaderCommit;

    public AppendEntries(Integer term, Integer leaderId, Integer prevLogIndex, Integer prevLogTerm, List<Entry> entries, Integer leaderCommit) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }

    public AppendEntries(){}

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }

}
