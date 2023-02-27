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

    protected int messageAmount = 1000;

    protected Queue<String> messages = new ConcurrentLinkedQueue<>();
    protected GrpcClient client;
    protected GrpcServer server;

    protected String newMessage() {
        return RandomStringUtils.randomAlphanumeric(256);
    }

    protected synchronized void setupBeforeBenchmark(int messageAmount, int threadCount, long handleWait) {
        this.messageAmount = messageAmount;
        for (int i = 0; i < messageAmount; i++) {
            messages.add(RandomStringUtils.randomAlphanumeric(256));
        }

        if (client == null && server == null) {
            final int port = RandomUtils.nextInt(50000, 50050);

            server = GrpcServer.newInstance("-port", String.valueOf(port), "-thread", String.valueOf(threadCount), "-handlewait", String.valueOf(handleWait));
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
