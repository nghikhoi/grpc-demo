package me.nghikhoi.grpcclientdemo.client;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class FutureGreeter implements GreetClient {

    @Getter
    private final GreeterGrpc.GreeterFutureStub futureStub;

    public FutureGreeter(GreeterGrpc.GreeterFutureStub futureStub) {
        this.futureStub = futureStub;
    }

    @Override
    @SneakyThrows
    public String greet(String name) {
        return send(name).get().getMessage();
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
        return Futures.transform(send(message), HelloResponse::getMessage, MoreExecutors.directExecutor());
    }

    @Override
    public List<ListenableFuture<String>> greetFuture(String... messages) {
        return Stream.of(messages).map(this::greetFuture).toList();
    }

    private ListenableFuture<HelloResponse> send(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        try {
            return futureStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {}", e.getStatus());
            throw e;
        }
    }

}
