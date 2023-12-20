package com.savchenko.node;

public class StateMachine {
    private final StateMachineEngine engine;
    private final Log log;
    private Integer commitIndex = -1;
    private Integer lastApplied = -1;
    public StateMachine(Log log, StateMachineEngine engine){
        this.log = log;
        this.engine = engine;
    }

    public void updateCommitIndex(Integer leaderCommitIndex) {
        if(leaderCommitIndex > commitIndex){
            commitIndex = Math.min(leaderCommitIndex, log.lastIndex());
            commitIfGrater();
        }
    }

    private void commitIfGrater(){
        if(commitIndex > lastApplied){
            log.get()
                    .subList(lastApplied + 1, commitIndex + 1)
                    .forEach(e -> engine.apply(e.value()));
            lastApplied = commitIndex;
        }
    }

    public void setCommitIndex(Integer newCommitIndex){
        commitIndex = newCommitIndex;
        commitIfGrater();
    }

    public Integer getCommitIndex(){
        return commitIndex;
    }

    public StateMachineEngine getEngine(){
        return engine;
    }
}
