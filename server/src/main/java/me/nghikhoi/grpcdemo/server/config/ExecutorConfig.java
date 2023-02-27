package me.nghikhoi.grpcdemo.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.nghikhoi.grpcdemo.ApplicationArguments.THREAD;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService executorService(@Value("${" + THREAD + "}") int threadCount) {
        return threadCount < 1 ? Executors.newCachedThreadPool() : Executors.newFixedThreadPool(threadCount);
    }

}
