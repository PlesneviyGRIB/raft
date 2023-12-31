package com.savchenko.node;

import com.savchenko.Constants;
import com.savchenko.connection.ConnectionManager;
import com.savchenko.connection.ServerConnection;
import com.savchenko.data.Message;
import com.savchenko.data.communication.*;
import com.savchenko.data.visitor.DataTraversal;
import com.savchenko.suportive.Entry;
import com.savchenko.suportive.Utils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Node {
    private static final Logger logger = Logger.getLogger(ServerConnection.class.getSimpleName());
    private final BlockingQueue<Message> queue;
    private final ConnectionManager connectionManager;
    private final Log log = new Log();
    private StateMachine stateMachine;
    private final NodeTerm nodeTerm = new NodeTerm();
    private NodeState state = NodeState.FOLLOWER;

    public Node(Integer port, List<Integer> slaves, StateMachineEngine engine) throws IOException {
        queue = new LinkedBlockingQueue<>();
        connectionManager = new ConnectionManager(port, slaves, queue);
        stateMachine = new StateMachine(log, engine);
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
                    .ifPresentOrElse(m -> m.data().accept(new DataTraversal() {
                        @Override
                        public Void accept(AppendEntries data) {
                            System.out.println(m);
                            var result = new AppendEntriesResult();
                            var entryOpt = log.getByIndex(data.prevLogIndex);
                            result.success = data.term >= nodeTerm.term() && entryOpt.map(e -> e.term().equals(data.prevLogTerm)).orElse(data.prevLogIndex == -1);
                            result.term = nodeTerm.term();
                            updateTerm(data.term);
                            nodeTerm.setLeaderId(data.leaderId);
                            connectionManager.send(nodeTerm.getLeaderId(), result);
                            if(result.success){
                                log.append(data.prevLogIndex, data.entries);
                                stateMachine.updateCommitIndex(data.leaderCommit);
                            }
                            return null;
                        }

                        @Override
                        public Void accept(ClientMessage data) {
                            connectionManager.send(m.source(), new RedirectMessage(nodeTerm.getLeaderId()));
                            return null;
                        }

                        @Override
                        public Void accept(VoteRequest data) {
                            var response = processVoteRequest(data);
                            connectionManager.send(m.source(), response);
                            updateTerm(data.term);
                            return null;
                        }
                    }), () -> state = NodeState.CANDIDATE);
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
            connectionManager.sendToOtherNodes(voteRequest);

            while (!round.requireReVote && state == NodeState.CANDIDATE) {
                var messageOpt = Optional.ofNullable(queue.poll(Constants.CANDIDATE_TIMEOUT_DURATION, TimeUnit.MILLISECONDS));
                var result = new Object() {
                    public boolean approved = false;
                };
                messageOpt.ifPresent(m -> m.data().accept(new DataTraversal() {
                            @Override
                            public Void accept(AppendEntries data) {
                                if (data.term >= nodeTerm.term()) {
                                    queue.add(m);
                                }
                                updateTerm(data.term);
                                return null;
                            }

                            @Override
                            public Void accept(VoteRequest data) {
                                var response = processVoteRequest(data);
                                connectionManager.send(m.source(), response);
                                updateTerm(data.term);
                                return null;
                            }

                            @Override
                            public Void accept(VoteResponse data) {
                                result.approved = data.voteGranted;
                                return null;
                            }

                            @Override
                            public Void accept(ClientMessage data) {
                                connectionManager.send(m.source(), new RedirectMessage(nodeTerm.getLeaderId()));
                                return null;
                            }
                        })
                );

                messageOpt.ifPresent(m -> round.votes.put(m.source(), result.approved));
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
                    .getNodeConnections()
                    .forEach(c -> c.send(controller.newAppendEntries(c.getResolvedPort(), nodeTerm.term(), stateMachine.getCommitIndex())));

            var timeout = System.currentTimeMillis() + Constants.APPEND_ENTRIES_TIMEOUT;

            while (timeout > System.currentTimeMillis()) {
                Optional
                        .ofNullable(queue.poll(20, TimeUnit.MILLISECONDS))
                        .ifPresent(m -> m.data().accept(new DataTraversal() {
                            @Override
                            public Void accept(ClientMessage data) {
                                log.add(nodeTerm.term(), data);
                                return null;
                            }

                            @Override
                            public Void accept(StateRequest data) {
                                var value = stateMachine.getEngine().stringify();
                                connectionManager.send(m.source(), new Response(value));
                                return null;
                            }

                            @Override
                            public Void accept(AppendEntriesResult data) {
                                controller.processResponse(m.source(), data);
                                return null;
                            }
                        }));
            }
            checkForConsensus(controller);
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

    private void checkForConsensus(SlavesController controller){
        var candidateIndex = controller.calculateNewCommitIndex();
        if(candidateIndex > stateMachine.getCommitIndex()){
            stateMachine.setCommitIndex(candidateIndex);
        }
    }
}
