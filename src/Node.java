import data.AppendEntries;
import data.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Node {
    public final String HOST = "127.0.0.1";
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private Server server;
    private List<Integer> slaves;
    private List<ServerConnection> connections;

    public Node(Integer port, List<Integer> slaves) throws IOException {
        server = new Server(port, queue);
        this.slaves = slaves;
    }

    public void start() {
        new Thread(server).start();
        connections = slaves.stream()
                .filter(clientPort -> clientPort < server.getPort())
                .map(clientPort -> {
                    try {
                        return new ServerConnection(new Socket(HOST, clientPort), queue);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .peek(Thread::start)
                .toList();
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

            Stream.of(
                            server.getConnections(),
                            connections
                    )
                    .flatMap(Collection::stream)
                    .forEach(c -> {
                        try {
                            c.write(info);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
