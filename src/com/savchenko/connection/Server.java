package com.savchenko.connection;

import com.savchenko.data.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable {
    private final ServerSocket serverSocket;
    private final Collection<ServerConnection> connections;
    private final BlockingQueue<Message> queue;

    public Server(Integer port, BlockingQueue<Message> queue) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.connections = Collections.synchronizedCollection(new ArrayList<>());
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !serverSocket.isClosed()) {
                var socket = serverSocket.accept();
                var connection = new ServerConnection(socket, queue);
                connection.start();
                connections.add(connection);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ServerConnection> getConnections() {
        return connections.stream().filter(c -> !c.isDead()).toList();
    }

    public Integer getPort() {
        return serverSocket.getLocalPort();
    }
}
