package com.savchenko;

import com.savchenko.connection.ConnectionManager;
import com.savchenko.data.*;
import com.savchenko.logic.NodeState;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Node {
    private final BlockingQueue<Message> queue;
    private final ConnectionManager connectionManager;
    private NodeState state = NodeState.FOLLOWER;
    private Long term = 0L;

    public Node(Integer port, List<Integer> slaves) throws IOException {
        queue = new LinkedBlockingQueue<>();
        connectionManager = new ConnectionManager(port, slaves, queue);
    }

    public void start() {
        new Thread(connectionManager).start();
        try {
            while (true) {
                beFollower();
                beCandidate();
                beLeader();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

    private void beFollower() throws InterruptedException {
        while (state == NodeState.FOLLOWER) {
            var message = queue.poll(Constants.ELECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            Optional
                    .ofNullable(message)
                    .ifPresentOrElse(m -> {
                        System.out.println(m);
                        m.data().accept(new DataVisitor<Void>() {
                            @Override
                            public Void accept(AppendEntries data) {
                                return null;
                            }
                            @Override
                            public Void accept(InitMessage data) {
                                return null;
                            }
                            @Override
                            public Void accept(VoteRequest data) {
                                connectionManager
                                        .getConnection(message.source())
                                        .ifPresent(c -> c.send(new VoteResponse(term, data.term >= term)));
                                return null;
                            }
                            @Override
                            public Void accept(VoteResponse data) {
                                return null;
                            }
                        });
                    }, this::election);
        }
    }

    private void beCandidate() throws InterruptedException {
        var connectionsCount = connectionManager.getConnections().size();
        var time = System.currentTimeMillis();
        var votes = new HashMap<>(Map.of(connectionManager.getPort(), true));

        while (state == NodeState.CANDIDATE) {
            var message = Optional
                    .ofNullable(queue.poll(Constants.CANDIDATE_TIMEOUT_DURATION, TimeUnit.MILLISECONDS))
                    .orElse(new Message(connectionManager.getPort(), new Data(){}));
            var approved = message.data().accept(new DataVisitor<Boolean>() {
                @Override
                public Boolean accept(AppendEntries data) {
                    return null;
                }
                @Override
                public Boolean accept(InitMessage data) {
                    return null;
                }
                @Override
                public Boolean accept(VoteRequest data) {
                    connectionManager
                            .getConnection(message.source())
                            .ifPresent(c -> c.send(new VoteResponse(term, data.term >= term)));
                    return null;
                }
                @Override
                public Boolean accept(VoteResponse data) {
                    return data.voteGranted;
                }
            });

            Optional.ofNullable(approved).ifPresent(v -> votes.put(message.source(), v));
            var approvedCount = votes.entrySet().stream().filter(Map.Entry::getValue).count();

            if (approvedCount > connectionsCount / 2 + 1) {
                state = NodeState.LEADER;
            } else {
                if (votes.entrySet().size() == connectionsCount || time + Constants.CANDIDATE_TIMEOUT_DURATION < System.currentTimeMillis()) {
                    state = NodeState.FOLLOWER;
                }
            }
        }
    }

    private void beLeader() {
        while (state == NodeState.LEADER) {
            connectionManager.getConnections().forEach(c -> c.send(new AppendEntries()));
            sleep();
        }
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(Constants.NODE_SLEEP_TIMEOUT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void election() {
        state = NodeState.CANDIDATE;
        var vote = new VoteRequest(term++, connectionManager.getPort(), 0L, 0L);
        connectionManager.getConnections().forEach(c -> c.send(vote));
    }

}
