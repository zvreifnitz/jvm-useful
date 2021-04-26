package com.github.zvreifnitz.jpipeline;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class SequentialPipelineBenchmark {

    private ExecutorService executorService;
    private Pipeline<Double, Double> pipeline;
    @Param({"1", "10", "100", "1000", "10000"})
    private double input;
    @Param({"1", "2", "4"})
    private int threads;

    @Setup
    public void setup() {
        this.executorService = new ForkJoinPool(this.threads);
        this.pipeline = PipelineMethods.buildSequentialPipeline(this.executorService);
    }

    @TearDown
    public void tearDown() {
        this.pipeline.close();
        this.executorService.shutdownNow();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 4, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 0, jvmArgsAppend = {"-XX:-RestrictContended", "-XX:+PreserveFramePointer"})
    @Threads(1)
    @BenchmarkMode({Mode.Throughput})
    public double pipelineSingle() {
        return PipelineMethods.getResult(this.pipeline.execute(this.input));
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 4, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 0, jvmArgsAppend = {"-XX:-RestrictContended", "-XX:+PreserveFramePointer"})
    @Threads(1)
    @BenchmarkMode({Mode.Throughput})
    public double pipelineMany() {
        final List<Future<Double>> futures = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            futures.add(this.pipeline.execute(this.input));
        }
        return futures.stream().mapToDouble(PipelineMethods::getResult).sum();
    }
}
