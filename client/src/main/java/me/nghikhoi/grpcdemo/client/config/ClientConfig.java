package me.nghikhoi.grpcdemo.client.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import me.nghikhoi.grpcdemo.client.AsyncGreeter;
import me.nghikhoi.grpcdemo.client.BlockingGreeter;
import me.nghikhoi.grpcdemo.client.FutureGreeter;
import me.nghikhoi.grpcdemo.client.GreetClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.nghikhoi.grpcdemo.ApplicationArguments.CLIENT_USE_CONSOLE;
import static me.nghikhoi.grpcdemo.ApplicationArguments.PORT;

@Configuration
public class ClientConfig {

    @Bean
    public int serverPort(@Value("${" + PORT + "}") int port) {
        return port;
    }

    @Bean
    public boolean useConsole(@Value("${" + CLIENT_USE_CONSOLE + ":#{false}}") boolean useConsole) {
        return useConsole;
    }

    @Bean
    public ManagedChannel managedChannel(int serverPort) {
        return ManagedChannelBuilder.forAddress("localhost", serverPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public ExecutorService blockingExecutors() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public GreetClient blockingClient(ManagedChannel managedChannel, ExecutorService blockingExecutors) {
        return new BlockingGreeter(GreeterGrpc.newBlockingStub(managedChannel), blockingExecutors);
    }

    @Bean
    public GreetClient asyncClient(ManagedChannel managedChannel) {
        return new AsyncGreeter(GreeterGrpc.newStub(managedChannel));
    }

    @Bean
    public GreetClient futureClient(ManagedChannel managedChannel) {
        return new FutureGreeter(GreeterGrpc.newFutureStub(managedChannel));
    }

}
