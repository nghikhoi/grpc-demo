package grpcclientdemo;

import me.nghikhoi.grpcclientdemo.client.BlockingGreeter;
import org.openjdk.jmh.annotations.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
public class SyncBenchmark extends BaseBenchmark {

    private BlockingGreeter stub;
    private Queue<String> results = new ConcurrentLinkedQueue<>();

    @Setup(Level.Trial)
    public void setupServer() {
        setupBeforeBenchmark(1000, 1, 1);
        stub = client.getBlockingStub();
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
            results.add(stub.greetWithResponse(newMessage()));
        }
    }

}
