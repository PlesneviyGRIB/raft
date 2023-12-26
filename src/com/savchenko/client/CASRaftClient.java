package com.savchenko.client;

import com.savchenko.Constants;
import com.savchenko.data.Data;
import com.savchenko.data.communication.ClientMessage;
import com.savchenko.data.communication.RedirectMessage;
import com.savchenko.data.communication.Response;
import com.savchenko.data.communication.StateRequest;
import com.savchenko.data.visitor.DataUnexpected;
import com.savchenko.suportive.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class CASRaftClient extends RaftClient {
    private final AtomicReference<String> command = new AtomicReference<>(null);

    public CASRaftClient(List<Integer> nodeIds) {
        super(
                nodeIds,
                input -> {
                    try {
                        if (input.equalsIgnoreCase("state")) {
                            return new StateRequest();
                        }
                        var tokens = input.split(" ");
                        if (tokens[0].equals("put")) {
                            return new ClientMessage(String.format("put %s %s %s", tokens[1], tokens[2], tokens[3]));
                        }
                        if (tokens[0].equals("remove")) {
                            return new ClientMessage(String.format("remove %s %s", tokens[1], tokens[2]));
                        }
                        System.out.println(Utils.formatInfo("Ignore: %s", input));
                    } catch (Exception e) {
                        System.out.println(Utils.formatError("Unable to parse: %s", input));
                    }
                    return null;
                },
                response -> System.out.println(response.value));
    }

    @Override
    void send(Data data) {
        connectionRef.get().send(data);
        if(data instanceof ClientMessage message) {
            if (!message.value.equals("state")) {
                command.set(message.value);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        connectionRef.get().send(new StateRequest());
                    }
                }, Constants.CLIENT_CAS_CHECK_TIMEOUT);
            }
        }
        while (Objects.nonNull(command.get())) {
            Utils.sleep(50);
        }
    }

    @Override
    public void run() {
        try {
            randomConnection();
            while (!Thread.currentThread().isInterrupted()) {
                queue.take().data().accept(new DataUnexpected() {
                    @Override
                    public Void accept(RedirectMessage data) {
                        if (data.leaderId != null) {
                            logger.info(Utils.formatError("REDIRECTED FROM [%s] TO [%s]", connectionRef.get().getResolvedPort(), data.leaderId));
                            createConnection(data.leaderId);
                        }
                        return null;
                    }

                    @Override
                    public Void accept(Response data) {
                        var map = Utils.<HashMap<String, String>>readObject(data.value);
                        var cmnd = command.get();
                        if(Objects.nonNull(cmnd)) {
                            var tokens = cmnd.split(" ");
                            if (tokens[0].equals("put")) {
                                var currentValue = map.get(tokens[2]);
                                System.out.printf("successful: %b\n", Objects.nonNull(currentValue) && currentValue.equals(tokens[3]));
                                command.set(null);
                                return null;
                            }
                            if (tokens[0].equals("remove")) {
                                var currentValue = map.get(tokens[2]);
                                System.out.printf("successful: %b\n", Objects.isNull(currentValue));
                                command.set(null);
                                return null;
                            }
                        }
                        responseProcessor.accept(data);
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
