import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class ServerConnection extends Thread {
    private final ObjectMapper objectMapper;
    private final Socket socket;
    BlockingQueue<Message> queue;

    public ServerConnection(Socket socket, BlockingQueue<Message> queue) {
        this.objectMapper = new ObjectMapper();
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
            try {
                if(socket.getInputStream().available() > 0) {
                    var message = new Message(socket.getRemoteSocketAddress().toString(), objectMapper.readValue(socket.getInputStream(), Data.class));
                    queue.add(message);
                }
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write(Data data) {
        if (socket.isConnected()) {
            try {
                socket.getOutputStream().write(objectMapper.writeValueAsBytes(data));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Integer port(){
        return socket.getPort();
    }
}
