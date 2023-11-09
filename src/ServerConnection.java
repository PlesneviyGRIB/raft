import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import messages.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerConnection extends Thread {
    private final ObjectMapper objectMapper;
    private final Socket socket;
    private final OutputStreamWriter writer;
    private final BlockingQueue<Message> queue;

    public ServerConnection(Socket socket, BlockingQueue<Message> queue) {
        try {
            this.objectMapper = new ObjectMapper();
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
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    var rawData = scanner.next();
                    var data = readMessage(rawData);
                    var message = new Message(socket.getRemoteSocketAddress().toString(), data);
                    queue.add(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Data readMessage(String token) throws JsonProcessingException {
        var wrapper = objectMapper.readValue(token, DataWrapper.class);
        switch (wrapper.type()){
            case INFO -> {
                return objectMapper.readValue(wrapper.rawData(), Info.class);
            }
            case BIT_REQUEST -> {
                return objectMapper.readValue(wrapper.rawData(), BitRequest.class);
            }
            default -> {
                return null;
            }
        }
    }

    public void write(Data data) {
        try {
            if (socket.isConnected()) {
                DataType type = data.accept(new Data.DataVisitor<>() {
                    @Override
                    public DataType visit(BitRequest data) {
                        return DataType.BIT_REQUEST;
                    }
                    @Override
                    public DataType visit(Info data) {
                        return DataType.INFO;
                    }
                });
                var wrapper = new DataWrapper(type, objectMapper.writeValueAsString(data));
                writer.write(objectMapper.writeValueAsString(wrapper) + " ");
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
