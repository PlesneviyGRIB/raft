package com.savchenko.node;

public class StateMachine {

    private final Log state = new Log();
    private final Log log;
    private Integer commitIndex = -1;
    private Integer lastApplied = -1;
    public StateMachine(Log log){
        this.log = log;
    }

    public void updateCommitIndex(Integer leaderCommitIndex) {
        if(leaderCommitIndex > commitIndex){
            commitIndex = Math.min(leaderCommitIndex, log.lastIndex());
            commitIfGrater();
        }
    }

    private void commitIfGrater(){
        if(commitIndex > lastApplied){
            var entries = log.get().subList(lastApplied + 1, commitIndex + 1);
            lastApplied = commitIndex;
            state.get().addAll(entries);
        }
    }

    public void setCommitIndex(Integer newCommitIndex){
        commitIndex = newCommitIndex;
        commitIfGrater();
    }

    public Integer getCommitIndex(){
        return commitIndex;
    }

    public Log getLog(){
        return state;
    }

    @Override
    public String toString() {
        return String.format("%s, commitIndex: %d", state, commitIndex);
    }
}
