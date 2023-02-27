package me.nghikhoi.grpcdemo.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.nghikhoi.grpcdemo.AbstractSpringApplication;
import me.nghikhoi.grpcdemo.OptionHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
@Slf4j
public class GrpcClient extends AbstractSpringApplication implements CommandLineRunner {

    public static GrpcClient newInstance(int port) {
        ApplicationContext context = SpringApplication.run(GrpcClient.class, "-port", String.valueOf(port));
        return context.getBean(GrpcClient.class);
    }

    private int port;
    private boolean useConsole = true;
    private ManagedChannel channel;
    @Getter
    private BlockingGreeter blockingStub;
    @Getter
    private AsyncGreeter asyncStub;
    @Getter
    private FutureGreeter futureStub;

    private ExecutorService blockingExecutor;

    public GrpcClient(ApplicationContext context) {
        super(context);
    }

    @Getter
    private boolean started = false;

    public synchronized void start() {
        if (started) {
            return;
        }

        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();

        blockingExecutor = Executors.newCachedThreadPool();
        blockingStub = new BlockingGreeter(GreeterGrpc.newBlockingStub(channel), blockingExecutor);
        asyncStub = new AsyncGreeter(GreeterGrpc.newStub(channel));
        futureStub = new FutureGreeter(GreeterGrpc.newFutureStub(channel));

        if (useConsole) {
            Scanner scanner = new Scanner(System.in);
            while (started) {
                String input = scanner.nextLine();
                if (input.equals("exit")) {
                    break;
                }

                sendMessage(input);
            }
        }

        log.info("Client started");
        started = true;
    }

    public void shutdown() {
        log.info("Shutting down");
        started = false;
        channel.shutdown();
        blockingExecutor.shutdown();
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
            } else if (type.equals("b")) {
                client = this.getBlockingStub();
            } else if (type.equals("f")) {
                client = this.getFutureStub();
            }
        }
        return client;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args != null) {
            Options options = OptionHelper.newGeneralOptions();
            CommandLineParser parser = new DefaultParser();
            CommandLine cli = parser.parse(options, args);

            port = Integer.parseInt(cli.getOptionValue("port", "50051"));
        }

        start();
    }

}
