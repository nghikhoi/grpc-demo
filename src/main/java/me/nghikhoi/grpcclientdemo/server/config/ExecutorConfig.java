package me.nghikhoi.grpcclientdemo.server.config;

import me.nghikhoi.grpcclientdemo.ApplicationArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService executorService(@Value("${" + ApplicationArguments.THREAD + "}") int threadCount) {
        return threadCount < 1 ? Executors.newCachedThreadPool() : Executors.newFixedThreadPool(threadCount);
    }

}
