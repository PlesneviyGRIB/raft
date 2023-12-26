package com.savchenko.client;

import com.savchenko.Constants;
import com.savchenko.connection.ConnectionType;
import com.savchenko.connection.ServerConnection;
import com.savchenko.data.Data;
import com.savchenko.data.Message;
import com.savchenko.data.communication.RedirectMessage;
import com.savchenko.data.communication.Response;
import com.savchenko.data.visitor.DataUnexpected;
import com.savchenko.suportive.Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class RaftClient implements Runnable {
    final static Logger logger = Logger.getLogger(ServerConnection.class.getSimpleName());
    final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    final AtomicReference<ServerConnection> connectionRef = new AtomicReference<>();
    private final List<Integer> nodeIds;
    private final Function<String, Data> userInputProcessor;
    protected final Consumer<Response> responseProcessor;

    public RaftClient(List<Integer> nodeIds, Function<String, Data> userInputProcessor, Consumer<Response> responseProcessor) {
        this.nodeIds = nodeIds;
        this.userInputProcessor = userInputProcessor;
        this.responseProcessor = responseProcessor;
    }

    public RaftClient waitForInput() {
        var scanner = new Scanner(System.in);
        while (true) {
            var value = scanner.nextLine();
            if (value.toLowerCase().startsWith("reconnect")) {
                createConnection(Integer.parseInt(value.split(" ")[1]));
                continue;
            }
            Optional
                    .ofNullable(userInputProcessor.apply(value))
                    .ifPresent(this::send);
        }
    }

    void send(Data data) {
        connectionRef.get().send(data);
    }

    void createConnection(Integer port) {
        try {
            TimeUnit.MILLISECONDS.sleep(1000L);
            Optional.ofNullable(connectionRef.get()).ifPresent(ServerConnection::kill);
            var socket = new Socket(Constants.HOST, port);
            var connection = ServerConnection.startNew(socket, queue, socket.getLocalPort(), ConnectionType.CLIENT);
            connectionRef.set(connection);
        } catch (IOException | InterruptedException ioException) {
            randomConnection();
        }
    }

    void randomConnection() {
        var targetPort = nodeIds.get(ThreadLocalRandom.current().nextInt(0, nodeIds.size()));
        logger.info(Utils.formatError("Random connection to [%s]. Tip: type 'reconnect 0001' to bind with port 0001", targetPort));
        createConnection(targetPort);
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
                        responseProcessor.accept(data);
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RaftClient createNew(List<Integer> nodeIds, Function<String, Data> inputProcessor, Consumer<Response> responseProcessor) {
        var client = new RaftClient(nodeIds, inputProcessor, responseProcessor);
        new Thread(client).start();
        client.waitForInput();
        return client;
    }
}
