package com.savchenko.node;

import java.util.Objects;

public class NodeTerm {
    private Integer term = 0;
    private Integer voteFor = null;
    private Integer leaderId = null;
    private Boolean alreadyVoted = false;

    public Integer Increment() {
        alreadyVoted = false;
        voteFor = null;
        leaderId = null;
        return ++term;
    }

    public Integer setTerm(Integer newTerm) {
        if (newTerm <= term) {
            throw new RuntimeException("New term cun not be smaller than current!");
        }
        term = newTerm;
        alreadyVoted = false;
        voteFor = null;
        leaderId = null;
        return term;
    }

    public Integer term() {
        return term;
    }

    public void setVoteFor(Integer vote) {
        if (alreadyVoted) {
            throw new RuntimeException("Already voted in this term!");
        }
        voteFor = vote;
        alreadyVoted = true;
    }

    public Integer votedFor() {
        return voteFor;
    }

    public boolean canVote() {
        return !alreadyVoted;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        if(Objects.nonNull(this.leaderId) && !this.leaderId.equals(leaderId)){
            throw new RuntimeException("Several leaders in single term!");
        }
        this.leaderId = leaderId;
    }
}
