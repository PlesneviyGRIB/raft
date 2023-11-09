import data.*;

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

    public ServerConnection(Socket socket, BlockingQueue<Message> queue) throws IOException {
        this.socket = socket;
        this.writer = new OutputStreamWriter(socket.getOutputStream());
        this.queue = queue;
    }

    @Override
    public void run() {
        logger.info(String.format("NEW CLIENT %s", socket.getRemoteSocketAddress().toString()));
        try {
            var scanner = new Scanner(socket.getInputStream());
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                var rawData = scanner.next();
                var message = new Message(socket.getRemoteSocketAddress().toString(), readData(rawData));
                queue.add(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Data readData(String token) {
        var wrapper = Utils.<DataWrapper>readObject(token);
        if (wrapper.type() == DataType.APPEND_ENTRIES) {
            return Utils.<AppendEntries>readObject(wrapper.rawData());
        }
        return null;
    }

    public void write(Data data) throws IOException {
        var type = data.accept(new DataVisitor<DataType>() {
            @Override
            public DataType visit(AppendEntries entries){
                return DataType.APPEND_ENTRIES;
            }
        });
        writer.write(Utils.writeObject(new DataWrapper(type, Utils.writeObject(data))).concat(" "));
        writer.flush();
    }
}
