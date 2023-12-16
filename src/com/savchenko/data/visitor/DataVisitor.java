package com.savchenko.data.visitor;

import com.savchenko.data.communication.*;

public interface DataVisitor<R> {
    R accept(ClientMessage data);
    R accept(RedirectMessage data);
    R accept(StateRequest data);
    R accept(Response data);
    R accept(AppendEntries data);
    R accept(AppendEntriesResult data);
    R accept(VoteRequest data);
    R accept(VoteResponse data);
}