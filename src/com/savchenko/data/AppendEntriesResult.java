package com.savchenko.data;

public class AppendEntriesResult implements Data {
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
