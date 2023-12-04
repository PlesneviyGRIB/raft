package com.savchenko.connection;

import com.savchenko.Constants;
import com.savchenko.data.InitMessage;
import com.savchenko.data.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ConnectionManager implements Runnable {

    private BlockingQueue<Message> queue;
    private Server server;
    private Collection<ServerConnection> connections = Collections.synchronizedCollection(new ArrayList<>());
    private List<Integer> deadConnections;

    public ConnectionManager(Integer port, List<Integer> slaves, BlockingQueue<Message> queue) throws IOException {
        this.queue = queue;
        server = new Server(port, queue);
        this.deadConnections = slaves;
    }

    @Override
    public void run() {
        new Thread(server).start();
        while (!Thread.currentThread().isInterrupted()) {
            renewConnections();
            try {
                TimeUnit.MILLISECONDS.sleep(Constants.RENEW_CONNECTION_TIMEOUT);
            } catch (InterruptedException ignore) {
            }
        }
    }

    private void renewConnections() {
        var preservedConnections = new ArrayList<>(connections.stream().filter(c -> !c.isDead()).toList());

        var targetConnections = new ArrayList<>(deadConnections);
        targetConnections.addAll(connections.stream().filter(ServerConnection::isDead).map(ServerConnection::getLocalPort).toList());
        deadConnections = new ArrayList<>();

        var newConnections = targetConnections.stream()
                .filter(clientPort -> clientPort < server.getPort())
                .map(clientPort -> {
                    try {
                        return new ServerConnection(new Socket(Constants.HOST, clientPort), queue);
                    } catch (IOException e) {
                        deadConnections.add(clientPort);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .peek(Thread::start)
                .peek(c -> c.send(new InitMessage(server.getPort())))
                .toList();

        preservedConnections.addAll(newConnections);
        connections = preservedConnections;
    }

    public List<ServerConnection> getConnections() {
        return Stream.of(server.getConnections(), connections).flatMap(Collection::stream).toList();
    }

    public Optional<ServerConnection> getConnection(Integer port) {
        return Stream.of(server.getConnections(), connections).flatMap(Collection::stream).filter(c -> c.getResolvedPort().equals(port)).findFirst();
    }

    public Integer getPort(){
        return server.getPort();
    }

}
