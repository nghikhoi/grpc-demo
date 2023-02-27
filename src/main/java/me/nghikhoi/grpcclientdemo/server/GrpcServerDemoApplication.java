package me.nghikhoi.grpcclientdemo.server;

import com.google.protobuf.util.JsonFormat;
import com.google.rpc.ErrorInfo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.HelloResponse;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class GrpcServerDemoApplication {

    @Getter(lazy = true)
    private static final GrpcServerDemoApplication instance = newInstance();

    private static GrpcServerDemoApplication newInstance() {
        ApplicationContext context = SpringApplication.run(GrpcServerDemoApplication.class);
        return context.getBean(GrpcServerDemoApplication.class);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GrpcServerDemoApplication server = getInstance();
        server.start();
    }

    private int port = 50051;
    private Server server;
    @Getter
    private boolean started = false;

    public synchronized void start() throws IOException, InterruptedException {
        if (started) {
            return;
        }

        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .intercept(new LoggingInterceptor())
                .build();

        server.start();
        started = true;
        log.info("Server started, listening on " + port);

        blockUntilShutdown();
    }

    public void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }

    public boolean isShutdown() {
        return server == null || server.isShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static final JsonFormat.TypeRegistry TYPE_REGISTRY =
            JsonFormat.TypeRegistry.newBuilder().add(ErrorInfo.getDescriptor()).build();

    private static final JsonFormat.Printer PRINTER =
            JsonFormat.printer()
                    .omittingInsignificantWhitespace()
                    .includingDefaultValueFields()
                    .usingTypeRegistry(TYPE_REGISTRY);

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        @SneakyThrows
        public void sayHello(HelloRequest req, StreamObserver<HelloResponse> responseObserver) {
            HelloResponse reply = HelloResponse.newBuilder().setMessage("Hello " + req.getName()).build();
            String handleId = Thread.currentThread().getId() + ":" + System.currentTimeMillis();
            log.debug("Handle request {} with message {}", handleId, PRINTER.print(req));
            log.debug("Sending response {} with message {}", handleId, PRINTER.print(reply));
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

    }

}
