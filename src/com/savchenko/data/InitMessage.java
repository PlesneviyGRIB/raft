package com.savchenko.data;

public class InitMessage implements Data {
    public Integer targetPort;

    public InitMessage(Integer targetPort) {
        this.targetPort = targetPort;
    }

    public InitMessage() {
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }
}
