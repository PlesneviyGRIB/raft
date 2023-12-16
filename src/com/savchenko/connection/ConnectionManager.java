package com.savchenko.connection;

import com.savchenko.Constants;
import com.savchenko.data.Data;
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

    private List<Integer> slaves;

    public ConnectionManager(Integer port, List<Integer> slaves, BlockingQueue<Message> queue) throws IOException {
        this.queue = queue;
        this.slaves = slaves;
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
                        return ServerConnection.startNew(new Socket(Constants.HOST, clientPort), queue, server.getPort(), ConnectionType.NODE);
                    } catch (IOException e) {
                        deadConnections.add(clientPort);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        preservedConnections.addAll(newConnections);
        connections = preservedConnections;
    }

    private Stream<ServerConnection> readyConnections() {
        return Stream.of(server.getConnections(), connections).flatMap(Collection::stream).filter(ServerConnection::isReady);
    }


    public List<ServerConnection> getNodeConnections() {
        return readyConnections().filter(c -> c.getType().equals(ConnectionType.NODE)).toList();
    }

    public List<ServerConnection> getClientConnections() {
        return readyConnections().filter(c -> c.getType().equals(ConnectionType.CLIENT)).toList();
    }

    public Optional<ServerConnection> getConnection(Integer port) {
        return readyConnections().filter(c -> c.getResolvedPort().equals(port)).findFirst();
    }

    public void send(Integer port, Data data) {
        getConnection(port).ifPresent(c -> c.send(data));
    }

    public void sendToOtherNodes(Data data) {
        getNodeConnections().stream().filter(c -> !getPort().equals(c.getLocalPort())).forEach(c -> c.send(data));
    }

    public Integer getPort() {
        return server.getPort();
    }

    public List<Integer> getSlavesIds() {
        return slaves;
    }

}
