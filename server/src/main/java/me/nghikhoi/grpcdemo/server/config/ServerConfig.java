package me.nghikhoi.grpcdemo.server.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.nghikhoi.grpcdemo.server.GreeterImpl;
import me.nghikhoi.grpcdemo.server.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

import static me.nghikhoi.grpcdemo.ApplicationArguments.HANDLE_WAIT;
import static me.nghikhoi.grpcdemo.ApplicationArguments.PORT;

@Configuration
public class ServerConfig {

    @Bean
    @Qualifier("serverPort")
    public int serverPort(@Value("${" + PORT + "}") int port) {
        return port;
    }

    @Bean
    public ServerBuilder<?> serverBuilder(
            @Value("${" + PORT + "}") int port,
            GreeterImpl greeterImpl,
            ExecutorService executorService
    ) {
        return ServerBuilder.forPort(port)
                .addService(greeterImpl)
                .intercept(new LoggingInterceptor())
                .executor(executorService);
    }

    @Bean
    public GreeterImpl greeterImpl(@Value("${" + HANDLE_WAIT + "}") long handleWait) {
        return new GreeterImpl(handleWait);
    }

    @Bean
    public Server server(ServerBuilder<?> serverBuilder) {
        return serverBuilder.build();
    }

}
