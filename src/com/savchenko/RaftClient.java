package com.savchenko;

import com.savchenko.connection.ConnectionType;
import com.savchenko.connection.ServerConnection;
import com.savchenko.data.Message;
import com.savchenko.data.communication.ClientMessage;
import com.savchenko.data.communication.RedirectMessage;
import com.savchenko.data.communication.Response;
import com.savchenko.data.communication.StateRequest;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class RaftClient implements Runnable {
    public static void main(String[] args) {
        var client = new RaftClient();
        new Thread(client).start();
        client.waitForInput();
    }

    private static Logger logger = Logger.getLogger(ServerConnection.class.getSimpleName());
    private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private AtomicReference<ServerConnection> connectionRef = new AtomicReference<>();

    private RaftClient waitForInput() {
        var scanner = new Scanner(System.in);
        while (true) {
            var value = scanner.nextLine();
            if(value.toLowerCase().startsWith("reconnect")){
                createConnection(Integer.parseInt(value.split(" ")[1]));
                continue;
            }
            if(value.equals("state")){
                connectionRef.get().send(new StateRequest());
                continue;
            }
            if(connectionRef.get().isDead()){
                randomConnection();
                continue;
            }
            connectionRef.get().send(new ClientMessage(value));
        }
    }

    private void createConnection(Integer port) {
        try {
            Optional.ofNullable(connectionRef.get()).ifPresent(ServerConnection::kill);
            var socket = new Socket(Constants.HOST, port);
            var connection = ServerConnection.startNew(socket, queue, socket.getLocalPort(), ConnectionType.CLIENT);
            connectionRef.set(connection);
        } catch (IOException ioException) {
            randomConnection();
        }
    }

    private void randomConnection(){
        var targetPort = List.of(8080, 8081, 8082).get(ThreadLocalRandom.current().nextInt(0, 3));
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
                        if(data.leaderId != null) {
                            logger.info(Utils.formatError("REDIRECTED FROM [%s] TO [%s]", connectionRef.get().getResolvedPort(), data.leaderId));
                            createConnection(data.leaderId);
                        }
                        return null;
                    }

                    @Override
                    public Void accept(Response data) {
                        System.out.println(Utils.writeObject(data));
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
