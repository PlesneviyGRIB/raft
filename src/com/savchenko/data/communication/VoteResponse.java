package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

public class VoteResponse extends Data {
    public Integer term;
    public boolean voteGranted;
    public VoteResponse(Integer term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public VoteResponse(){
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }
}