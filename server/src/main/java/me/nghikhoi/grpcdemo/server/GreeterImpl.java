package me.nghikhoi.grpcdemo.server;

import com.google.protobuf.util.JsonFormat;
import com.google.rpc.ErrorInfo;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static me.nghikhoi.grpcdemo.ApplicationArguments.HANDLE_WAIT;

@Slf4j
public class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    private static final JsonFormat.TypeRegistry TYPE_REGISTRY =
            JsonFormat.TypeRegistry.newBuilder().add(ErrorInfo.getDescriptor()).build();

    private static final JsonFormat.Printer PRINTER =
            JsonFormat.printer()
                    .omittingInsignificantWhitespace()
                    .includingDefaultValueFields()
                    .usingTypeRegistry(TYPE_REGISTRY);

    private final long handleWait;
    @Getter
    private final Queue<HandleProfile> executeProfiles = new ConcurrentLinkedQueue<>();

    public GreeterImpl(long handleWait) {
        this.handleWait = handleWait;
    }

    @Override
    @SneakyThrows
    public void sayHello(HelloRequest req, StreamObserver<HelloResponse> responseObserver) {
            HandleProfile handleProfile = new HandleProfile();
            handleProfile.setThreadId(Thread.currentThread().getId());
            handleProfile.setStartTime(System.currentTimeMillis());

            String handleId = Thread.currentThread().getId() + ":" + System.currentTimeMillis();
            log.debug("Handle request {} with message {}", handleId, PRINTER.print(req));
            HelloResponse reply = HelloResponse.newBuilder().setMessage("Hello " + req.getName() + ": " + RandomStringUtils.random(256)).build();
            Thread.sleep(handleWait);
            log.debug("Sending response {} with message {}", handleId, PRINTER.print(reply));
            responseObserver.onNext(reply);
            responseObserver.onCompleted();

            handleProfile.setEndTime(System.currentTimeMillis());
            executeProfiles.add(handleProfile);
    }

}
