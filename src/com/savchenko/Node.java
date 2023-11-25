package com.savchenko;

import com.savchenko.connection.ConnectionManager;
import com.savchenko.data.AppendEntries;
import com.savchenko.data.Message;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Node {
    private final BlockingQueue<Message> queue;
    private final ConnectionManager connectionManager;

    public Node(Integer port, List<Integer> slaves) throws IOException {
        queue = new LinkedBlockingQueue<>();
        connectionManager = new ConnectionManager(port, slaves, queue);
    }

    public void start() {
        new Thread(connectionManager).start();
    }

    public void readData() throws InterruptedException {
        while (true) {
            var data = queue.poll();
            if (data != null) {
                System.out.println(data);
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    public void writeData() {
        var scanner = new Scanner(System.in);
        while (true) {
            var info = new AppendEntries();
            info.text = scanner.nextLine();

            connectionManager
                    .getConnections()
                    .forEach(c -> c.write(info));
        }
    }
}
