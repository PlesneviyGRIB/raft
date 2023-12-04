package com.savchenko.data;

import com.savchenko.Utils;

public class VoteRequest implements Data {
    public Long term;
    public Integer candidateId;
    public Long lastLogIndex;
    public Long lastLogTerm;

    public VoteRequest(Long term, Integer candidateId, Long lastLogIndex, Long lastLogTerm) {
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
