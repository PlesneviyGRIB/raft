package com.savchenko.connection;

import com.savchenko.suportive.Utils;
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
    private Integer resolvedPort;
    private Boolean isDead = false;

    public ServerConnection(Socket socket, BlockingQueue<Message> queue) throws IOException {
        this.socket = socket;
        this.writer = new OutputStreamWriter(socket.getOutputStream());
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            var scanner = new Scanner(socket.getInputStream());
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                var rawData = scanner.next();
                var data = Utils.readObject(rawData).accept(new DataVisitor<Data>() {
                    @Override
                    public AppendEntries accept(AppendEntries data) {
                        return data;
                    }
                    @Override
                    public Data accept(AppendEntriesResult data) {
                        return data;
                    }
                    @Override
                    public VoteResponse accept(VoteResponse data) {
                        return data;
                    }
                    @Override
                    public VoteRequest accept(VoteRequest data) {
                        return data;
                    }
                    @Override
                    public AppendEntries accept(InitMessage data) {
                        resolvedPort = data.targetPort;
                        return null;
                    }
                });
                if(data != null) {
                    var message = new Message(resolvedPort, data);
                    queue.add(message);
                } else {
                    logger.info(Utils.formatSuccess("NEW CLIENT %s", resolvedPort));
                }
            }
        } catch (Exception ignore) {}
        finally {
            isDead = true;
            logger.info(Utils.formatError("DISCONNECTED %s", resolvedPort));
        }
    }

    public void send(Data data) {
        try {
            writer.write(Utils.writeObject(data).concat(" "));
            writer.flush();
        } catch (IOException ignore) {
            isDead = true;
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public Integer getLocalPort(){
        return socket.getPort();
    }

    public Integer getResolvedPort(){
        return resolvedPort;
    }
}
