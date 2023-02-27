package grpcclientdemo;

import com.google.common.util.concurrent.ListenableFuture;
import me.nghikhoi.grpcclientdemo.client.GreetClient;
import org.openjdk.jmh.annotations.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
public class AsyncBenchmark extends BaseBenchmark {

    private final Queue<ListenableFuture<String>> responses = new ConcurrentLinkedQueue<>();
    private final Queue<String> results = new ConcurrentLinkedQueue<>();
    private GreetClient stub;

    /**
     * b: blocking stub
     * f: future stub
     */
    @Param({"b", "f"})
    private String stubType;

    /**
     * Number of threads to use for the server
     * <1: use cached thread pool
     * >=1: use fixed thread pool
     */
    @Param({"-1", "1", "5", "10"})
    private int threadCount;

    @Param({"1", "100", "1000"})
    private int messageAmount;

    /**
     * Time to wait in the server before sending response (unit = milliseconds)
     */
    @Param({"1"})
    private long handleWait;

    @Setup(Level.Trial)
    public void setupServer() {
        setupBeforeBenchmark(messageAmount, threadCount, handleWait);
        responses.clear();
        stub = client.getGreetClient(stubType);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        System.out.println("Thread count: " + threadCount + ", Stub type: " + stubType);
        clean();
        results.clear();
    }

    @Benchmark
    public void send() {
        for (int i = 0; i < messageAmount; i++) {
            responses.add(stub.greetFuture(newMessage()));
        }

        while (!responses.isEmpty()) {
            try {
                results.add(responses.poll().get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
