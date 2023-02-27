package me.nghikhoi.grpcdemo.benchmark;

import me.nghikhoi.grpcdemo.client.BlockingGreeter;
import me.nghikhoi.grpcdemo.client.GreetClient;
import org.openjdk.jmh.annotations.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
public class SyncBenchmark extends BaseBenchmark {

    private GreetClient stub;
    private Queue<String> results = new ConcurrentLinkedQueue<>();

    @Setup(Level.Trial)
    public void setupServer() {
        setupBeforeBenchmark(1000, 1, 1);
        stub = client.getGreetClient("b");
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        clean();
        results.clear();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.BREAK)
    public void blockingStubBenchmark() {
        for (int i = 0; i < messageAmount; i++) {
            results.add(stub.greet(newMessage()));
        }
    }

}
