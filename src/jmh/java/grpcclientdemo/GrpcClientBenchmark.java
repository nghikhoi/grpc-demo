package grpcclientdemo;

import me.nghikhoi.grpcclientdemo.WatchDog;
import me.nghikhoi.grpcclientdemo.client.GrpcClientDemoApplication;
import me.nghikhoi.grpcclientdemo.server.GrpcServerDemoApplication;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

@State(Scope.Benchmark)
public class GrpcClientBenchmark {

    private final List<String> MESSAGES;

    public GrpcClientBenchmark() {
        MESSAGES = new LinkedList<>();

        int strLength = 100;
        int strAmount = 10;

        for (int i = 0; i < strAmount; i++) {
            MESSAGES.add(RandomStringUtils.randomAlphanumeric(strLength));
        }
    }

    private String msg;
    private GrpcClientDemoApplication client;
    private GrpcServerDemoApplication server;
    private WatchDog watchDog;

    @Setup(Level.Invocation)
    public void setup() {
        msg = RandomStringUtils.randomAlphanumeric(256);
    }

    @Setup(Level.Trial)
    public void setupServer() {
        this.server = GrpcServerDemoApplication.getInstance();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                this.server.start();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        while (!GrpcServerDemoApplication.getInstance().isStarted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        watchDog = new WatchDog();
//        Executors.newSingleThreadExecutor().execute(watchDog::start);
        this.client = GrpcClientDemoApplication.getInstance();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        client.shutdown();
        server.shutdown();

        while (!server.isShutdown()) {
            Thread.sleep(100);
        }

        watchDog.shutdown();
    }

    @Benchmark
    public void asyncStubBenchmark() {
        client.sendMessage(String.format("%s%s:%s", "a", 1, msg));
    }

    @Benchmark
    public void blockingStubBenchmark() {
        client.sendMessage(String.format("%s%s:%s", "b", 1, msg));
    }

    @Benchmark
    public void futureStubBenchmark() {
        client.sendMessage(String.format("%s%s:%s", "f", 1, msg));
    }

}
