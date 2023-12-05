package com.savchenko.node;

public class NodeTerm {
    private Integer term = 0;
    private Integer voteFor = null;
    private Boolean alreadyVoted = false;

    public Integer Increment() {
        alreadyVoted = false;
        voteFor = null;
        return ++term;
    }

    public Integer setTerm(Integer newTerm) {
        if (newTerm <= term) {
            throw new RuntimeException("New term cun not be smaller than current!");
        }
        term = newTerm;
        alreadyVoted = false;
        voteFor = null;
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

}
