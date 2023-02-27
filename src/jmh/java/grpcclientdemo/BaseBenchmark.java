package grpcclientdemo;

import me.nghikhoi.grpcclientdemo.client.GrpcClient;
import me.nghikhoi.grpcclientdemo.server.GrpcServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class BaseBenchmark {

    protected final static int MESSAGE_AMOUNT = 1000;

    protected Queue<String> messages = new ConcurrentLinkedQueue<>();
    protected GrpcClient client;
    protected GrpcServer server;

    protected String newMessage() {
        return RandomStringUtils.randomAlphanumeric(256);
    }

    protected synchronized void setupBeforeBenchmark(int threadCount) {
        for (int i = 0; i < MESSAGE_AMOUNT; i++) {
            messages.add(RandomStringUtils.randomAlphanumeric(256));
        }

        if (client == null && server == null) {
            final int port = RandomUtils.nextInt(50000, 60000);

            server = GrpcServer.newInstance(port, threadCount);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    server.start();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            client = GrpcClient.newInstance(port);
        }
    }

    protected void clean() {
        client.shutdown();
        server.shutdown();
        server.summaryExecuteTimes();
    }

}
