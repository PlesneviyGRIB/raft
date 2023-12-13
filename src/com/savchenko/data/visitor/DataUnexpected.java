package com.savchenko.data.visitor;

import com.savchenko.data.communication.*;
import com.savchenko.suportive.UnexpectedMessageException;

public interface DataUnexpected extends DataTraversal {

    @Override
    default Void accept(ClientMessage data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(RedirectMessage data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(StateRequest data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(StateResponse data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(AppendEntries data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(AppendEntriesResult data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(VoteRequest data){
        throw new UnexpectedMessageException();
    };

    @Override
    default Void accept(VoteResponse data){
        throw new UnexpectedMessageException();
    };
}
