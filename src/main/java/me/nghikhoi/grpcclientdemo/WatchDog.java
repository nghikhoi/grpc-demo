package me.nghikhoi.grpcclientdemo;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WatchDog {

    @Getter
    private boolean started = false;

    public synchronized void start() {
        if (started) {
            return;
        }

        started = true;
        log.info("WatchDog is running");

        while (started) {
            while (isChanged()) {
                printReport();
            }
        }
    }

    public void shutdown() {
        started = false;
    }

    @SneakyThrows
    private void printReport() {
        lastThreads = getCurrentThreads();
        log.info("Current threads: " + lastThreads);
    }

    private int lastThreads = 0;

    private int getCurrentThreads() {
        return Thread.getAllStackTraces().size();
    }

    private boolean isChanged() {
        int currentThreads = getCurrentThreads();
        if (currentThreads != lastThreads) {
            lastThreads = currentThreads;
            return true;
        }
        return false;
    }

}
