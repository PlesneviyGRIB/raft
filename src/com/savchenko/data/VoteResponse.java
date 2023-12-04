package com.savchenko.data;

import com.savchenko.Utils;

public class VoteResponse implements Data {
    public Long term;
    public boolean voteGranted;
    public VoteResponse(Long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public VoteResponse(){
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