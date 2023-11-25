package com.savchenko.connection;

import com.savchenko.Utils;
import com.savchenko.data.*;

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
    private Boolean isDead = false;

    public ServerConnection(Socket socket, BlockingQueue<Message> queue) throws IOException {
        this.socket = socket;
        this.writer = new OutputStreamWriter(socket.getOutputStream());
        this.queue = queue;
    }

    @Override
    public void run() {
        logger.info(Utils.formatSuccess("NEW CLIENT %s", socket.getRemoteSocketAddress().toString()));
        try {
            var scanner = new Scanner(socket.getInputStream());
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                var rawData = scanner.next();
                var message = new Message(socket.getRemoteSocketAddress().toString(), Utils.readObject(rawData));
                queue.add(message);
            }
        } catch (Exception ignore) {}
        finally {
            isDead = true;
            logger.info(Utils.formatError("DISCONNECTED %s", socket.getRemoteSocketAddress().toString()));
        }
    }

    public void write(Data data) {
        try {
            writer.write(Utils.writeObject(data).concat(" "));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public Integer getPort(){
        return socket.getPort();
    }
}
