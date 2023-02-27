package me.nghikhoi.grpcdemo.server;

import com.google.protobuf.util.JsonFormat;
import com.google.rpc.ErrorInfo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.nghikhoi.grpcdemo.AbstractSpringApplication;
import me.nghikhoi.grpcdemo.OptionHelper;
import org.apache.commons.cli.*;
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
public class GrpcServer extends AbstractSpringApplication {

    public GrpcServer(ApplicationContext context) {
        super(context);
    }

    public static GrpcServer newInstance(int port, int threadCount) {
        return newInstance("--port=" + port, "--thread=" + threadCount);
    }

    public static GrpcServer newInstance(String... args) {
        ApplicationContext context = SpringApplication.run(GrpcServer.class, args);
        return context.getBean(GrpcServer.class);
    }

    @Getter
    private boolean started = false;

    public synchronized void start() throws IOException, InterruptedException {
        if (started) {
            return;
        }

        Server server = getHandleServer();
        server.start();
        started = true;
        log.info("Server started, listening on " + getContext().getBean("serverPort", int.class));

        blockUntilShutdown();
    }

    protected Server getHandleServer() {
        return getContext().getBean(Server.class);
    }

    public void shutdown() {
        getHandleServer().shutdown();

        ExecutorService executorService = getContext().getBean(ExecutorService.class);
        executorService.shutdown();
    }

    public void summaryExecuteTimes() {
        Queue<HandleProfile> executeProfiles = getContext().getBean(GreeterImpl.class).getExecuteProfiles();
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
        return getHandleServer().isShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        getHandleServer().awaitTermination();
    }

    private static final JsonFormat.TypeRegistry TYPE_REGISTRY =
            JsonFormat.TypeRegistry.newBuilder().add(ErrorInfo.getDescriptor()).build();

    private static final JsonFormat.Printer PRINTER =
            JsonFormat.printer()
                    .omittingInsignificantWhitespace()
                    .includingDefaultValueFields()
                    .usingTypeRegistry(TYPE_REGISTRY);

}
