package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;

public class AppendEntriesResult extends Data {
    public Integer term;
    public boolean success;

    public AppendEntriesResult(Integer term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public AppendEntriesResult() {
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }
}
