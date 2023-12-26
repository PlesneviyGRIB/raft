package com.savchenko;

import com.savchenko.node.Node;
import com.savchenko.node.StateMachineEngine;
import com.savchenko.suportive.Utils;

import java.util.*;

public class ServerMain {
    private final static StateMachineEngine listEngine = new StateMachineEngine() {
        private final List<String> state = new LinkedList<>();

        @Override
        public void apply(String string) {
            state.add(string);
        }

        @Override
        public String stringify() {
            return Utils.writeObject(state);
        }
    };

    private final static StateMachineEngine mapEngine = new StateMachineEngine() {
        private final HashMap<String, String> state = new HashMap<>();

        @Override
        public void apply(String string) {
            var tokens = string.split(" ");
            if (tokens[0].equals("put")) {
                state.put(tokens[1], tokens[2]);
            }
            if (tokens[0].equals("remove")) {
                state.remove(tokens[1]);
            }
        }

        @Override
        public String stringify() {
            return Utils.writeObject(state);
        }
    };

    private final static StateMachineEngine CASMapEngine = new StateMachineEngine() {
        private final HashMap<String, String> state = new HashMap<>();

        @Override
        public void apply(String string) {
            var tokens = string.split(" ");
            if (tokens[0].equals("put")) {
                Optional.ofNullable(state.get(tokens[2]))
                        .ifPresentOrElse(value -> {
                            if (value.equals(tokens[1])) {
                                state.put(tokens[2], tokens[3]);
                            }
                        }, () -> {
                            if (tokens[1].equals("null")) {
                                state.put(tokens[2], tokens[3]);
                            }
                        });
            }
            if (tokens[0].equals("remove")) {
                Optional.ofNullable(state.get(tokens[2]))
                        .or(() -> Optional.of("null"))
                        .ifPresent(value -> {
                            if (value.equals(tokens[1])) {
                                state.remove(tokens[2]);
                            }
                        });
            }
        }

        @Override
        public String stringify() {
            return Utils.writeObject(state);
        }
    };

    public static void main(String[] args) throws Exception {
        var ports = Arrays.stream(args).map(Integer::parseInt).toList();
        var node = new Node(ports.get(0), ports.subList(1, ports.size()), CASMapEngine);
        node.start();
    }
}