package me.nghikhoi.grpcclientdemo.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
@Slf4j
public class GrpcClientDemoApplication {

    @Getter(lazy = true)
    private static final GrpcClientDemoApplication instance = newInstance();

    public static void main(String[] args) {
        GrpcClientDemoApplication clientHolder = newInstance();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }

            clientHolder.sendMessage(input);
        }
    }

    private static GrpcClientDemoApplication newInstance() {
        ApplicationContext context = SpringApplication.run(GrpcClientDemoApplication.class);
        return context.getBean(GrpcClientDemoApplication.class);
    }

    private final ManagedChannel channel;
    @Getter
    private final BlockingGreeter blockingStub;
    @Getter
    private final AsyncGreeter asyncStub;
    @Getter
    private final MixedGreeter mixedStub;
    @Getter
    private final FutureGreeter futureStub;

    public GrpcClientDemoApplication() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        blockingStub = new BlockingGreeter(GreeterGrpc.newBlockingStub(channel));
        asyncStub = new AsyncGreeter(GreeterGrpc.newStub(channel));
        futureStub = new FutureGreeter(GreeterGrpc.newFutureStub(channel));
        mixedStub = new MixedGreeter(asyncStub, blockingStub);
    }

    @Getter
    private boolean started = false;

    public synchronized void startConsole() {
        if (started) {
            return;
        }

        log.info("Client started");
        started = true;

        Scanner scanner = new Scanner(System.in);
        while (started) {
            String input = scanner.nextLine();
            if (input.equals("exit")) {
                break;
            }

            sendMessage(input);
        }
    }

    public void shutdown() {
        log.info("Shutting down");
        started = false;
        channel.shutdown();
    }

    private final Pattern pattern = Pattern.compile("(?:([a-z])?(\\d+):)?(.*)");

    public void sendMessage(String input) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            String type = matcher.group(1);
            String countStr = matcher.group(2);
            String msg = matcher.group(3);

            GreetClient client = getGreetClient(type);

            int count = 1;

            if (countStr != null) {
                count = Integer.parseInt(countStr);
            }

            sendMessage(client, count, msg);
        }
    }

    public void sendMessage(String type, String input) {
        sendMessage(getGreetClient(type), 1, input);
    }

    public void sendMessage(GreetClient client, int count, String msg) {
        for (int i = 1; i <= count; i++) {
            log.debug("Sending message {}: {}", i, msg);
            client.greet(msg);
        }
    }

    public GreetClient getGreetClient(String type) {
        GreetClient client = this.getBlockingStub();
        if (type != null) {
            if (type.equals("a")) {
                client = this.getAsyncStub();
            } else if (type.equals("m")) {
                client = this.getMixedStub();
            } else if (type.equals("b")) {
                client = this.getBlockingStub();
            } else if (type.equals("f")) {
                client = this.getFutureStub();
            }
        }
        return client;
    }

    public interface GreetClient {
        void greet(String name);
    }

    class BlockingGreeter implements GreetClient {
        private final GreeterGrpc.GreeterBlockingStub blockingStub;

        public BlockingGreeter(GreeterGrpc.GreeterBlockingStub blockingStub) {
            this.blockingStub = blockingStub;
        }

        @Override
        public void greet(String name) {
            HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            HelloResponse response;
            try {
                response = blockingStub.sayHello(request);
            } catch (StatusRuntimeException e) {
                log.warn("RPC failed: {0}", e.getStatus());
                return;
            }
            log.debug("Blocking Greeting: " + response.getMessage());
        }
    }

    class AsyncGreeter implements GreetClient {
        private final GreeterGrpc.GreeterStub asyncStub;

        public AsyncGreeter(GreeterGrpc.GreeterStub asyncStub) {
            this.asyncStub = asyncStub;
        }

        @Override
        public void greet(String name) {
            HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            try {
                asyncStub.sayHello(request, new StreamObserver<>() {
                    @Override
                    public void onNext(HelloResponse response) {
                        log.debug("Async Greeting: " + response.getMessage());
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("RPC failed: {}", t);
                    }

                    @Override
                    public void onCompleted() {
                        log.debug("Async Greeting completed");
                    }
                });
            } catch (StatusRuntimeException e) {
                log.warn("RPC failed: {0}", e.getStatus());
            }
        }
    }

    class FutureGreeter implements GreetClient {

        private final GreeterGrpc.GreeterFutureStub futureStub;

        public FutureGreeter(GreeterGrpc.GreeterFutureStub futureStub) {
            this.futureStub = futureStub;
        }

        @Override
        public void greet(String name) {
            HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            try {
                final ListenableFuture<HelloResponse> future = futureStub.sayHello(request);
                future.addListener(() -> {
                    try {
                        HelloResponse response = future.get();
                        log.debug("Future Greeting: " + response.getMessage());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("RPC failed: {}", e);
                    }
                }, MoreExecutors.directExecutor());
            } catch (StatusRuntimeException e) {
                log.warn("RPC failed: {0}", e.getStatus());
            }
        }
    }

    class MixedGreeter implements GreetClient {
        private final GreetClient[] clients;
        private ExecutorService executorService;

        public MixedGreeter(GreetClient... clients) {
            this.clients = clients;
            executorService = java.util.concurrent.Executors.newFixedThreadPool(clients.length);
        }

        @Override
        public void greet(String name) {
            CompletableFuture.allOf(Arrays.stream(clients).map(client -> {
                CompletableFuture<Void> future = new CompletableFuture<>();
                executorService.submit(() -> {
                    client.greet(name);
                    future.complete(null);
                });
                return future;
            }).toArray(CompletableFuture[]::new)).join();
        }
    }

}
