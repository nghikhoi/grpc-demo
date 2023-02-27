package me.nghikhoi.grpcdemo.server.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

/*    @Bean
    public ExecutorService executorService(@Value("${" + ApplicationArguments.THREAD + "}") int threadCount) {
        return threadCount < 1 ? Executors.newCachedThreadPool() : Executors.newFixedThreadPool(threadCount);
    }*/

}
