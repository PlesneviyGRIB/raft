package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

public class VoteRequest extends Data {
    public Integer term;
    public Integer candidateId;
    public Integer lastLogIndex;
    public Integer lastLogTerm;

    public VoteRequest(Integer term, Integer candidateId, Integer lastLogIndex, Integer lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    public VoteRequest(){
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }
}
