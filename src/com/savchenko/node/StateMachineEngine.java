package com.savchenko.node;

public interface StateMachineEngine {
    void apply(String string);
    String stringify();
}
