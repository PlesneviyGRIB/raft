package com.savchenko.node;

import com.savchenko.Constants;
import com.savchenko.connection.ConnectionManager;
import com.savchenko.connection.ServerConnection;
import com.savchenko.data.*;
import com.savchenko.suportive.UnexpectedMessageException;
import com.savchenko.suportive.Utils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Node {
    private static Logger logger = Logger.getLogger(ServerConnection.class.getSimpleName());
    private final BlockingQueue<Message> queue;
    private final ConnectionManager connectionManager;
    private NodeState state = NodeState.FOLLOWER;

    private NodeTerm nodeTerm = new NodeTerm();
    private Log log = new Log();
//    private StateMachine stateMachine = new StateMachine();
//    private Long commitIndex = 0L;
//    private Integer lastApplied = 0;
    public Node(Integer port, List<Integer> slaves) throws IOException {
        queue = new LinkedBlockingQueue<>();
        connectionManager = new ConnectionManager(port, slaves, queue);
    }

    public void start() {
        new Thread(connectionManager).start();
        logger.info(Utils.formatSuccess("""
                
                █▄ █ █▀█ █▀▄ █▀▀
                █ ▀█ █▄█ █▄▀ ██▄ [%s]""",
                connectionManager.getPort()));
        try {
            while (true) {
                switch (state) {
                    case FOLLOWER -> {
                        logger.info(Utils.formatInfo("FOLLOWER"));
                        beFollower();
                    }
                    case CANDIDATE -> {
                        logger.info(Utils.formatInfo("CANDIDATE"));
                        beCandidate();
                    }
                    case LEADER -> {
                        logger.info(Utils.formatInfo("LEADER"));
                        beLeader();
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

    private void beFollower() throws InterruptedException {
        while (state == NodeState.FOLLOWER) {
            Optional
                    .ofNullable(queue.poll(Utils.randomize(Constants.ELECTION_TIMEOUT, 0.2), TimeUnit.MILLISECONDS))
                    .ifPresentOrElse(message -> {
                        System.out.println(message);
                        message.data().accept(new DataVisitor<Void>() {
                            @Override
                            public Void accept(AppendEntries data) {
                                var result = new AppendEntriesResult();
                                var entry = log.getByIndex(data.prevLogIndex);
                                result.success = data.term >= nodeTerm.term() && Objects.nonNull(entry) && entry.getKey().equals(data.prevLogTerm);
                                result.term = nodeTerm.term();
                                connectionManager.send(data.leaderId, result);
                                updateTerm(data.term);
                                return null;
                            }

                            @Override
                            public Void accept(AppendEntriesResult data) {
                                throw new UnexpectedMessageException();
                            }

                            @Override
                            public Void accept(InitMessage data) {
                                throw new UnexpectedMessageException();
                            }

                            @Override
                            public Void accept(VoteRequest data) {
                                var response = processVoteRequest(data);
                                connectionManager.send(message.source(), response);
                                updateTerm(data.term);
                                return null;
                            }

                            @Override
                            public Void accept(VoteResponse data) {
                                return null;
                            }
                        });
                    }, () -> state = NodeState.CANDIDATE);
        }
    }

    private void beCandidate() throws InterruptedException {
        while (state == NodeState.CANDIDATE) {
            var round = new Object() {
                final HashMap<Integer, Boolean> votes = new HashMap<>(Map.of(connectionManager.getPort(), true));
                final Long time = System.currentTimeMillis();
                boolean requireReVote = false;
            };

            var pair = log.getStateForVoting();
            var voteRequest = new VoteRequest(nodeTerm.Increment(), connectionManager.getPort(), pair.getLeft(), pair.getRight());
            nodeTerm.setVoteFor(connectionManager.getPort());
            connectionManager.sendToAll(voteRequest);

            while (!round.requireReVote && state == NodeState.CANDIDATE) {
                var message = Optional
                        .ofNullable(queue.poll(Constants.CANDIDATE_TIMEOUT_DURATION, TimeUnit.MILLISECONDS))
                        .orElse(new Message(connectionManager.getPort(), new Data() {
                        }));
                var approved = message.data().accept(new DataVisitor<Boolean>() {
                    @Override
                    public Boolean accept(AppendEntries data) {
                        if (data.term >= nodeTerm.term()) {
                            queue.add(message);
                        }
                        updateTerm(data.term);
                        return null;
                    }

                    @Override
                    public Boolean accept(AppendEntriesResult data) {
                        throw new UnexpectedMessageException();
                    }

                    @Override
                    public Boolean accept(InitMessage data) {
                        throw new UnexpectedMessageException();
                    }

                    @Override
                    public Boolean accept(VoteRequest data) {
                        var response = processVoteRequest(data);
                        connectionManager.send(message.source(), response);
                        updateTerm(data.term);
                        return null;
                    }

                    @Override
                    public Boolean accept(VoteResponse data) {
                        return data.voteGranted;
                    }
                });

                Optional.ofNullable(approved).ifPresent(v -> round.votes.put(message.source(), v));
                var approvedCount = round.votes.entrySet().stream().filter(Map.Entry::getValue).count();
                var totalCount = connectionManager.getSlavesIds().size() + 1;

                if (approvedCount > totalCount / 2) {
                    state = NodeState.LEADER;
                } else {
                    if (round.votes.entrySet().size() == totalCount) {
                        state = NodeState.FOLLOWER;
                    }
                    if (round.time + Utils.randomize(Constants.CANDIDATE_TIMEOUT_DURATION, 0.2) < System.currentTimeMillis()) {
                        logger.info(Utils.formatError("REVOTE BY TIMEOUT. Current term [%S]. Votes %s", nodeTerm.term(), approvedCount));
                        round.requireReVote = true;
                    }
                }
            }
        }
    }

    private void beLeader() throws InterruptedException {
        var controller = new SlavesController(log, connectionManager.getSlavesIds(), connectionManager.getPort());

        while (state == NodeState.LEADER) {
            connectionManager
                    .getConnections()
                    .forEach(c -> c.send(controller.newAppendEntries(c.getResolvedPort(), nodeTerm.term())));

            var timeout = System.currentTimeMillis() + Constants.APPEND_ENTRIES_TIMEOUT;

            while (timeout < System.currentTimeMillis()){
                Optional
                        .ofNullable(queue.poll(20, TimeUnit.MILLISECONDS))
                        .ifPresent(m -> m.data().accept(new DataVisitor<Void>() {
                            @Override
                            public Void accept(InitMessage data) {
                                throw new UnexpectedMessageException();
                            }

                            @Override
                            public Void accept(AppendEntries data) {
                                throw new UnexpectedMessageException();
                            }

                            @Override
                            public Void accept(AppendEntriesResult data) {
                                controller.processResponse(m.source(), data);
                                return null;
                            }

                            @Override
                            public Void accept(VoteRequest data) {
                                throw new UnexpectedMessageException();
                            }

                            @Override
                            public Void accept(VoteResponse data) {
                                throw new UnexpectedMessageException();
                            }
                        }));
            }
        }
    }

    private void updateTerm(Integer term) {
        if (term > nodeTerm.term()) {
            nodeTerm.setTerm(term);
            state = NodeState.FOLLOWER;
        }
    }

    private VoteResponse processVoteRequest(VoteRequest request) {
        var voteGranted = nodeTerm.canVote() && request.term >= nodeTerm.term() && log.isOlderThan(request.lastLogIndex, request.lastLogTerm);
        if (voteGranted) {
            nodeTerm.setVoteFor(request.candidateId);
        }
        return new VoteResponse(nodeTerm.term(), voteGranted);
    }
}
