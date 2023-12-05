package com.savchenko.data;

public interface DataVisitor<R> {
    R accept(InitMessage data);
    R accept(AppendEntries data);
    R accept(AppendEntriesResult data);
    R accept(VoteRequest data);
    R accept(VoteResponse data);
}