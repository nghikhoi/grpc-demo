package me.nghikhoi.grpcdemo.client;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

@Slf4j
public class BlockingGreeter implements GreetClient {
    @Getter
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    @Getter private final ExecutorService executor;

    public BlockingGreeter(GreeterGrpc.GreeterBlockingStub blockingStub, ExecutorService executor) {
        this.blockingStub = blockingStub;
        this.executor = executor;
    }

    @Override
    public String greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            throw e;
        }
        log.debug("Blocking Greeting: " + response.getMessage());
        return response.getMessage();
    }

    @Override
    public List<String> greet(String... messages) {
        List<ListenableFuture<String>> futures = greetFuture(messages);
        return futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                log.error("Error while getting future", e);
                return null;
            }
        }).toList();
    }

    @Override
    public ListenableFuture<String> greetFuture(String message) {
        return Futures.submit(() -> greet(message), executor);
    }

    @Override
    public List<ListenableFuture<String>> greetFuture(String... messages) {
        return Stream.of(messages).map(this::greetFuture).toList();
    }

    public String greetWithResponse(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            return null;
        }
        return response.getMessage();
    }
}
