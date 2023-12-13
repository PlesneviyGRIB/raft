package com.savchenko.connection;

import com.savchenko.data.Data;
import com.savchenko.data.Message;
import com.savchenko.data.communication.InitMessage;
import com.savchenko.suportive.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class ServerConnection extends Thread {
    private static Logger logger = Logger.getLogger(ServerConnection.class.getSimpleName());
    private final Socket socket;
    private final OutputStreamWriter writer;
    private final BlockingQueue<Message> queue;
    private InitMessage resolvedConnection;

    public ServerConnection(Socket socket, BlockingQueue<Message> queue) {
        try {
            this.socket = socket;
            this.writer = new OutputStreamWriter(socket.getOutputStream());
            this.queue = queue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            var scanner = new Scanner(socket.getInputStream());
            // handshake
            resolvedConnection = (InitMessage) Utils.readObject(scanner.next());
            logger.info(Utils.formatSuccess("NEW CLIENT %s", resolvedConnection.port));

            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                var rawData = scanner.next();
                var data = Utils.readObject(rawData);
                queue.add(new Message(resolvedConnection.port, data));
            }
        } catch (Exception ignore) {
        } finally {
            kill();
            logger.info(Utils.formatError("DISCONNECTED %s", resolvedConnection.port));
        }
    }

    public void send(Data data) {
        try {
            writer.write(Utils.writeObject(data).concat(" "));
            writer.flush();
        } catch (IOException ignore) {
            kill();
        }
    }

    public boolean isDead() {
        return socket.isClosed();
    }

    public Integer getLocalPort() {
        return socket.getPort();
    }

    public Integer getResolvedPort() {
        return resolvedConnection.port;
    }

    public ConnectionType getType() {
        return resolvedConnection.type;
    }

    public void kill() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ServerConnection startNew(Socket socket, BlockingQueue<Message> queue, Integer localPort, ConnectionType connectionType) {
        var connection = new ServerConnection(socket, queue);
        connection.send(new InitMessage(localPort, connectionType));
        connection.start();
        return connection;
    }
}
