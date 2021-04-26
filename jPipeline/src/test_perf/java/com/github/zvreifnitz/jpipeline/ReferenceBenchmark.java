package com.github.zvreifnitz.jpipeline;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ReferenceBenchmark {

    @Param({"1", "10", "100", "1000", "10000"})
    private double input;

    @Setup
    public void setup() {
    }

    @TearDown
    public void tearDown() {
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(iterations = 5, time = 4, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
    @Fork(value = 1, warmups = 0, jvmArgsAppend = {"-XX:-RestrictContended", "-XX:+PreserveFramePointer"})
    @Threads(1)
    @BenchmarkMode({Mode.Throughput})
    public double pipelineBaseline() {
        final List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            list.add(PipelineMethods.numOfSteps(this.input));
        }
        return PipelineMethods.avg(list, this.input);
    }
}
