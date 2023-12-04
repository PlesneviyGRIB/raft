package com.savchenko.data;

public interface DataVisitor<R> {
    R accept(AppendEntries data);
    R accept(InitMessage data);
    R accept(VoteRequest data);
    R accept(VoteResponse data);
}