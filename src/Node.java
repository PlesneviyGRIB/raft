import messages.Info;
import messages.Message;

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
    private final BlockingQueue<Message> queue;
    private Server server;
    private List<ServerConnection> connections;

    public Node(Integer port, List<Integer> slaves) {
        queue = new LinkedBlockingQueue<>();
        server = new Server(port, queue);
        new Thread(server).start();

        connections = slaves.stream()
                .filter(clientPort -> clientPort < port)
                .map(clientPort -> {
                    try {
                        return new ServerConnection(new Socket("127.0.0.1", clientPort), queue);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .peek(Thread::start)
                .toList();
    }

    public void readData() {
        while (true) {
            var data = queue.poll();
            if (data != null) {
                System.out.println(data);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void writeData() {
        var scanner = new Scanner(System.in);

        while (true) {
            var info = new Info();
            info.text = scanner.nextLine();

            Stream.of(
                            server.getConnections(),
                            connections
                    )
                    .flatMap(Collection::stream)
                    .forEach(c -> c.write(info));
        }
    }
}
