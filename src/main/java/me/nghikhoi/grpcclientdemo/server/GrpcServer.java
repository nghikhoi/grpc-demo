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
import me.nghikhoi.grpcclientdemo.AbstractSpringApplication;
import me.nghikhoi.grpcclientdemo.OptionHelper;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class GrpcServer extends AbstractSpringApplication implements CommandLineRunner {

    public GrpcServer(ApplicationContext context) {
        super(context);
    }

    public static GrpcServer newInstance(int port, int threadCount) {
        ApplicationContext context = SpringApplication.run(GrpcServer.class, "-port", String.valueOf(port), "-thread", String.valueOf(threadCount));
        return context.getBean(GrpcServer.class);
    }

    private int port = 50051;
    private int threadCount = 1;
    private Server server;
    private ExecutorService executorService;
    @Getter
    private boolean started = false;

    public synchronized void start() throws IOException, InterruptedException {
        if (started) {
            return;
        }

        executorService = threadCount < 1 ? Executors.newCachedThreadPool() : Executors.newFixedThreadPool(threadCount);
        ServerBuilder<?> builder = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .intercept(new LoggingInterceptor())
                .executor(executorService);

        server = builder.build();

        server.start();
        started = true;
        log.info("Server started, listening on " + port);

        blockUntilShutdown();
    }

    public void shutdown() {
        if (server != null) {
            server.shutdown();
            executorService.shutdown();
        }
    }

    @Getter
    private Queue<HandleProfile> executeProfiles = new ConcurrentLinkedQueue<>();

    public void summaryExecuteTimes() {
        int size = executeProfiles.size();
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (HandleProfile profile : executeProfiles) {
            long executeTime = profile.getDuration();
            total += executeTime;
            min = Math.min(min, executeTime);
            max = Math.max(max, executeTime);
        }

        log.info("Total request: {}", size);
        log.info("Total execute time: {} ms", total);
        log.info("Min execute time: {} ms", min);
        log.info("Max execute time: {} ms", max);

        log.info("Average execute time: {} ms", total / size);

        int threadCount = (int) executeProfiles.stream().mapToLong(HandleProfile::getThreadId).distinct().count();
        log.info("Thread count: {}", threadCount);

        log.info("Total time of all request: {} ms", executeProfiles.stream().mapToLong(HandleProfile::getEndTime).max().orElse(0) - executeProfiles.stream().mapToLong(HandleProfile::getStartTime).min().orElse(0));
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

    @Override
    public void run(String... args) throws Exception {
        if (args != null) {
            Options options = OptionHelper.newGeneralOptions();

            Option threadOption = new Option("thread", "thread", true, "Thread count");
            threadOption.setRequired(false);
            options.addOption(threadOption);

            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);

            port = Integer.parseInt(cli.getOptionValue("port", "50051"));
            threadCount = Integer.parseInt(cli.getOptionValue("thread", "1"));
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        @SneakyThrows
        public void sayHello(HelloRequest req, StreamObserver<HelloResponse> responseObserver) {
            HandleProfile handleProfile = new HandleProfile();
            handleProfile.setThreadId(Thread.currentThread().getId());
            handleProfile.setStartTime(System.currentTimeMillis());

            String handleId = Thread.currentThread().getId() + ":" + System.currentTimeMillis();
            log.debug("Handle request {} with message {}", handleId, PRINTER.print(req));
            HelloResponse reply = HelloResponse.newBuilder().setMessage("Hello " + req.getName() + ": " + RandomStringUtils.random(256)).build();
            Thread.sleep(TimeUnit.MILLISECONDS.toMillis(5));
            log.debug("Sending response {} with message {}", handleId, PRINTER.print(reply));
            responseObserver.onNext(reply);
            responseObserver.onCompleted();

            handleProfile.setEndTime(System.currentTimeMillis());
            executeProfiles.add(handleProfile);
        }

    }

}
