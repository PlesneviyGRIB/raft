package com.savchenko.data.visitor;

import com.savchenko.data.communication.*;

public interface DataTraversal extends DataVisitor<Void> {
    @Override
    default Void accept(ClientMessage data){
        return null;
    };

    @Override
    default Void accept(RedirectMessage data){
        return null;
    };

    @Override
    default Void accept(StateRequest data){
        return null;
    };

    @Override
    default Void accept(Response data){
        return null;
    };

    @Override
    default Void accept(AppendEntries data){
        return null;
    };

    @Override
    default Void accept(AppendEntriesResult data){
        return null;
    };

    @Override
    default Void accept(VoteRequest data){
        return null;
    };

    @Override
    default Void accept(VoteResponse data){
        return null;
    };
}
