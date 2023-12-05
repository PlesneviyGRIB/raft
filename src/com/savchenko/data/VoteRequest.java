package com.savchenko.data;

import com.savchenko.suportive.Utils;

public class VoteRequest implements Data {
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

    @Override
    public String toString() {
        return Utils.writeObject(this);
    }
}
