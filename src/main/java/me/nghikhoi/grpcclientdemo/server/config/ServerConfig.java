package me.nghikhoi.grpcclientdemo.server.config;

import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

import static me.nghikhoi.grpcclientdemo.ApplicationArguments.PORT;

@Configuration
public class ServerConfig {

    @Bean
    public ServerBuilder<?> serverBuilder(
            @Value("${" + PORT + "}") int port,
            ExecutorService executorService
    ) {
        return ServerBuilder.forPort(50051);
    }

}
