package me.nghikhoi.grpcclientdemo.client;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AsyncGreeter implements GreetClient {
    @Getter
    private final GreeterGrpc.GreeterStub asyncStub;

    public AsyncGreeter(GreeterGrpc.GreeterStub asyncStub) {
        this.asyncStub = asyncStub;
    }

    @Override
    public String greet(String name) {
        /*HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        AtomicReference<String> result = new AtomicReference<>();
        try {
            asyncStub.sayHello(request, new StreamObserver<>() {
                @Override
                public void onNext(HelloResponse response) {
                    result.set(response.getMessage());
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
        return result;*/
        return null;
    }

    @Override
    public List<String> greet(String... messages) {
        return null;
    }

    @Override
    public ListenableFuture<String> greetFuture(String message) {
        return null;
    }

    @Override
    public List<ListenableFuture<String>> greetFuture(String... messages) {
        return null;
    }
}
