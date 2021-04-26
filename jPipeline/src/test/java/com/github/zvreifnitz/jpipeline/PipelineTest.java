package com.github.zvreifnitz.jpipeline;

import com.github.zvreifnitz.jcore.exc.AppException;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.utils.Funcs;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiFunction;

import static com.github.zvreifnitz.jpipeline.BuilderHelper.*;

public final class PipelineTest {

    private static final Executor PIPELINE_EXECUTOR = PipelineExecutor.defaultExecutor(); //Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final Executor AUX_EXECUTOR = Executors.newSingleThreadExecutor();

    private static int numOfSteps(final double limit) {
        int result = 0;
        double sum = 0.0;
        while (sum < limit) {
            result++;
            sum += ThreadLocalRandom.current().nextDouble();
        }
        return result;
    }

    private static double avg(final List<Integer> vals, final double limit) {
        long sum = 0;
        for (final int v : vals) {
            sum += v;
        }
        return (vals.size() * limit) / sum;
    }

    @org.junit.Before
    public void setUp() {
    }

    @org.junit.After
    public void tearDown() {
    }

    @org.junit.Test
    public void testNoStep() throws Exception {
        final PipelineBuilder<Integer, Integer> builder = Funcs::identity;
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Integer> f = pipeline.execute(7);
            final int result = f.get();
            Assert.assertEquals(7, result);
        }
    }

    @org.junit.Test
    public void testNoStepPipeline() throws Exception {
        final PipelineBuilder<Integer, Integer> builder = Funcs::identity;
        try (final Pipeline<Integer, Integer> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Integer> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Integer> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Integer> f = pipeline.execute(7);
                final int result = f.get();
                Assert.assertEquals(7, result);
            }
        }
    }

    @org.junit.Test
    public void testOneStep() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addStep(IntToLongStep.INSTANCE);
        try (final Pipeline<Integer, Long> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Long> f = pipeline.execute(7);
            final long result = f.get();
            Assert.assertEquals(14L, result);
        }
    }

    @org.junit.Test
    public void testOneStepPipeline() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addStep(IntToLongStep.INSTANCE);
        try (final Pipeline<Integer, Long> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Long> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Long> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Long> f = pipeline.execute(7);
                final long result = f.get();
                Assert.assertEquals(14L, result);
            }
        }
    }

    @org.junit.Test
    public void testOneStepFork() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addStep(IntToLongForkStep.INSTANCE);
        try (final Pipeline<Integer, Long> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Long> f = pipeline.execute(7);
            final long result = f.get();
            Assert.assertEquals(14L, result);
        }
    }

    @org.junit.Test
    public void testOneStepPipelineFork() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addStep(IntToLongForkStep.INSTANCE);
        try (final Pipeline<Integer, Long> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Long> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Long> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Long> f = pipeline.execute(7);
                final long result = f.get();
                Assert.assertEquals(14L, result);
            }
        }
    }

    @org.junit.Test
    public void testTwoSteps() throws Exception {
        final PipelineBuilder<Integer, Double> builder = p -> p
                .addStep(IntToLongStep.INSTANCE)
                .addStep(LongToDoubleStep.INSTANCE);
        try (final Pipeline<Integer, Double> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Double> f = pipeline.execute(7);
            final double result = f.get();
            Assert.assertEquals(28.0, result, 0.00000001);
        }
    }

    @org.junit.Test
    public void testTwoStepsPipeline() throws Exception {
        final PipelineBuilder<Integer, Double> builder = p -> p
                .addStep(IntToLongStep.INSTANCE)
                .addStep(LongToDoubleStep.INSTANCE);
        try (final Pipeline<Integer, Double> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Double> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Double> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Double> f = pipeline.execute(7);
                final double result = f.get();
                Assert.assertEquals(28.0, result, 0.00000001);
            }
        }
    }

    @org.junit.Test
    public void testTwoStepsFork() throws Exception {
        final PipelineBuilder<Integer, Double> builder = p -> p
                .addStep(IntToLongForkStep.INSTANCE)
                .addStep(LongToDoubleForkStep.INSTANCE);
        try (final Pipeline<Integer, Double> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Double> f = pipeline.execute(7);
            final double result = f.get();
            Assert.assertEquals(28.0, result, 0.00000001);
        }
    }

    @org.junit.Test
    public void testTwoStepsPipelineFork() throws Exception {
        final PipelineBuilder<Integer, Double> builder = p -> p
                .addStep(IntToLongForkStep.INSTANCE)
                .addStep(LongToDoubleForkStep.INSTANCE);
        try (final Pipeline<Integer, Double> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Double> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Double> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Double> f = pipeline.execute(7);
                final double result = f.get();
                Assert.assertEquals(28.0, result, 0.00000001);
            }
        }
    }

    @org.junit.Test
    public void testParallel() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .join();
        try (final Pipeline<Integer, Long> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Long> f = pipeline.execute(7);
            final long result = f.get();
            Assert.assertEquals(119L, result);
        }
    }

    @org.junit.Test
    public void testParallelPipeline() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongStep.INSTANCE, Long::sum)
                .join();
        try (final Pipeline<Integer, Long> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Long> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Long> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Long> f = pipeline.execute(7);
                final long result = f.get();
                Assert.assertEquals(119L, result);
            }
        }
    }

    @org.junit.Test
    public void testParallelFork() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .join();
        try (final Pipeline<Integer, Long> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Long> f = pipeline.execute(7);
            final long result = f.get();
            Assert.assertEquals(119L, result);
        }
    }

    @org.junit.Test
    public void testParallelPipelineFork() throws Exception {
        final PipelineBuilder<Integer, Long> builder = p -> p
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .addParallelStep(IntToLongForkStep.INSTANCE, Long::sum)
                .join();
        try (final Pipeline<Integer, Long> step = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final PipelineBuilder<Integer, Long> wrapped = p -> p.addStep(step);
            try (final Pipeline<Integer, Long> pipeline = Pipeline.build(wrapped, PIPELINE_EXECUTOR)) {
                final Future<Long> f = pipeline.execute(7);
                final long result = f.get();
                Assert.assertEquals(119L, result);
            }
        }
    }

    @org.junit.Test
    public void testIf() throws Exception {
        final PipelineBuilder<Integer, Integer> builder = p -> p
                .addIfStep(i -> i % 2 == 0, IncrementStep.INSTANCE);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Integer> f7 = pipeline.execute(7);
            final int seven = f7.get();
            Assert.assertEquals(7, seven);
            final Future<Integer> f14 = pipeline.execute(14);
            final int fifteen = f14.get();
            Assert.assertEquals(15, fifteen);
        }
    }

    @org.junit.Test
    public void testIfElse() throws Exception {
        final PipelineBuilder<Integer, Boolean> builder = p -> p
                .addIfElseStep(i -> i % 2 == 0, EvenStep.INSTANCE, OddStep.INSTANCE);
        try (final Pipeline<Integer, Boolean> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Boolean> fOdd = pipeline.execute(7);
            final boolean isOddEven = fOdd.get();
            Assert.assertFalse(isOddEven);
            final Future<Boolean> fEven = pipeline.execute(14);
            final boolean isEvenEven = fEven.get();
            Assert.assertTrue(isEvenEven);
        }
    }

    @org.junit.Test
    public void testSwitch() throws Exception {
        final PipelineBuilder<Integer, String> builder = p -> p
                .addSwitchStep("switch", i -> Math.abs(i) % 3, Arrays.asList(
                        step((i) -> "reminder 0"),
                        step((i) -> "reminder 1"),
                        step((i) -> "reminder 2"))).peek(System.out::println);
        try (final Pipeline<Integer, String> pipeline = Pipeline.build("testSwitch", builder, PIPELINE_EXECUTOR)) {
            final Future<String> f0 = pipeline.execute(9);
            final String r0 = f0.get();
            Assert.assertEquals("reminder 0", r0);
            final Future<String> f1 = pipeline.execute(10);
            final String r1 = f1.get();
            Assert.assertEquals("reminder 1", r1);
            final Future<String> f2 = pipeline.execute(11);
            final String r2 = f2.get();
            Assert.assertEquals("reminder 2", r2);
        }
    }

    @SuppressWarnings("unchecked")
    @org.junit.Test
    public void testSwitchVarargs() throws Exception {
        final PipelineBuilder<Integer, String> builder = p -> p
                .addSwitchStep("switch", i -> Math.abs(i) % 3,
                        step((i) -> "reminder 0"),
                        step((i) -> "reminder 1"),
                        step((i) -> "reminder 2")).peek(System.out::println);
        try (final Pipeline<Integer, String> pipeline = Pipeline.build("testSwitchVarargs", builder, PIPELINE_EXECUTOR)) {
            final Future<String> f0 = pipeline.execute(9);
            final String r0 = f0.get();
            Assert.assertEquals("reminder 0", r0);
            final Future<String> f1 = pipeline.execute(10);
            final String r1 = f1.get();
            Assert.assertEquals("reminder 1", r1);
            final Future<String> f2 = pipeline.execute(11);
            final String r2 = f2.get();
            Assert.assertEquals("reminder 2", r2);
        }
    }

    @SuppressWarnings("unchecked")
    @org.junit.Test
    public void testSwitchVarargsPipeline() throws Exception {
        final PipelineBuilder<Integer, String> builder = p -> p
                .addSwitchStep("switch", i -> Math.abs(i) % 3,
                        Pipeline.build("p0", r -> r.addStep("s0", step((i) -> "reminder 0")).peek(System.out::println), PIPELINE_EXECUTOR),
                        Pipeline.build("p1", r -> r.addStep("s1", step((i) -> "reminder 1")).peek(System.out::println), PIPELINE_EXECUTOR),
                        Pipeline.build("p2", r -> r.addStep("s2", step((i) -> "reminder 2")).peek(System.out::println), PIPELINE_EXECUTOR));
        try (final Pipeline<Integer, String> pipeline = Pipeline.build("testSwitchVarargsPipeline", builder, PIPELINE_EXECUTOR)) {
            final Future<String> f0 = pipeline.execute(9);
            final String r0 = f0.get();
            Assert.assertEquals("reminder 0", r0);
            final Future<String> f1 = pipeline.execute(10);
            final String r1 = f1.get();
            Assert.assertEquals("reminder 1", r1);
            final Future<String> f2 = pipeline.execute(11);
            final String r2 = f2.get();
            Assert.assertEquals("reminder 2", r2);
        }
    }

    @org.junit.Test
    public void testAvgCalc() throws Exception {
        final PipelineStep<Double, List<Integer>> initStep = step(i -> new ArrayList<>());
        final BiFunction<Double, List<Integer>, List<Integer>> initMerger = merger();
        final PipelineStep<Double, Integer> calcStep = step(PipelineTest::numOfSteps);
        final BiFunction<List<Integer>, Integer, List<Integer>> calcMerger = merger(List::add);
        final PipelineStep<Double, Double> resultStep = step();
        final BiFunction<List<Integer>, Double, Double> resultMerger = PipelineTest::avg;

        final PipelineBuilder<Double, Double> builder = p -> p
                .addParallelStep(initStep, initMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(calcStep, calcMerger)
                .addParallelStep(resultStep, resultMerger)
                .join();
        try (final Pipeline<Double, Double> pipeline = Pipeline.build(builder, PIPELINE_EXECUTOR)) {
            final Future<Double> f3 = pipeline.execute(1_000.0);
            final Double r3 = f3.get();
            Assert.assertTrue(r3 > 0.4 && r3 < 0.6);
            final Future<Double> f6 = pipeline.execute(1_000_000.0);
            final Double r6 = f6.get();
            Assert.assertTrue(r6 > 0.4 && r6 < 0.6);
        }
    }

    @org.junit.Test
    public void testNested() throws Exception {
        final PipelineStep<Integer, Integer> step = e -> e.accept(e.get() + 1);
        final PipelineBuilder<Integer, Integer> seqBuilder = p -> p.peek(System.out::println)
                .addStep("seq_1", step).peek(System.out::println)
                .addStep("seq_2", step).peek(System.out::println)
                .addStep("seq_3", step).peek(System.out::println);
        final PipelineBuilder<Integer, Integer> parBuilder = p -> p.peek(System.out::println)
                .addParallelStep("par_1", step, BuilderHelper.merger()).peek(System.out::println)
                .addParallelStep("par_2", step, BuilderHelper.merger()).peek(System.out::println)
                .addParallelStep("par_3", step, BuilderHelper.merger()).peek(System.out::println)
                .join();
        final PipelineBuilder<Integer, Integer> seqSeqBuilder = p -> p.peek(System.out::println)
                .buildStep("seq_seq_1", seqBuilder).peek(System.out::println)
                .buildStep("seq_seq_2", seqBuilder).peek(System.out::println)
                .buildStep("seq_seq_3", seqBuilder).peek(System.out::println);
        final PipelineBuilder<Integer, Integer> seqParBuilder = p -> p.peek(System.out::println)
                .buildStep("seq_par_1", parBuilder).peek(System.out::println)
                .buildStep("seq_par_2", parBuilder).peek(System.out::println)
                .buildStep("seq_par_3", BuilderHelper.adapter(Funcs.identityFunc(), parBuilder, BuilderHelper.merger())).peek(System.out::println);
        final PipelineBuilder<Integer, Integer> parSeqBuilder = p -> p.peek(System.out::println)
                .buildParallelStep("par_seq_1", seqBuilder, BuilderHelper.merger()).peek(System.out::println)
                .buildParallelStep("par_seq_2", seqBuilder, BuilderHelper.merger()).peek(System.out::println)
                .buildParallelStep("par_seq_3", seqBuilder, BuilderHelper.merger()).peek(System.out::println)
                .join();
        final PipelineBuilder<Integer, Integer> parParBuilder = p -> p.peek(System.out::println)
                .buildParallelStep("par_par_1", parBuilder, BuilderHelper.merger()).peek(System.out::println)
                .buildParallelStep("par_par_2", parBuilder, BuilderHelper.merger()).peek(System.out::println)
                .buildParallelStep("par_par_3", BuilderHelper.adapter(Funcs.identityFunc(), parBuilder, BuilderHelper.merger()), BuilderHelper.merger()).peek(System.out::println)
                .join();
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .buildStep("builder_1", seqSeqBuilder).peek(System.out::println)
                .buildStep("builder_2", seqParBuilder).peek(System.out::println)
                .buildStep("builder_3", parSeqBuilder).peek(System.out::println)
                .buildStep("builder_4", parParBuilder).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("pipeline", builder, PIPELINE_EXECUTOR)) {
            final Future<Integer> f = pipeline.execute(1);
            final int r = f.get();
            Assert.assertEquals("testNested", 17, r);
        }
    }

    @org.junit.Test
    public void testRetry() throws Exception {
        final PipelineStep<Integer, Integer> step = e -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.9) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    throw new RuntimeException("no go 1");
                } else {
                    e.stop(new RuntimeException("no go 2"));
                }
            } else {
                e.accept(e.get() + 1);
            }
        };
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .addStep("retryStep", retry(step, i -> 10L)).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testRetry", builder, PIPELINE_EXECUTOR)) {
            final Future<Integer> f = pipeline.execute(1);
            final int r = f.get();
            Assert.assertEquals("testRetry", 2, r);
        }
    }

    @org.junit.Test
    public void testRetryPar() throws Exception {
        final PipelineStep<Integer, Integer> step = e -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.9) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    throw new RuntimeException("no go 1");
                } else {
                    e.stop(new RuntimeException("no go 2"));
                }
            } else {
                e.accept(e.get() + 1);
            }
        };
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .addParallelStep("retryStep_1", retry(step, i -> 10L), merger()).peek(System.out::println)
                .addParallelStep("retryStep_2", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_3", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_4", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_5", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_6", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_7", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_8", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .addParallelStep("retryStep_9", retry(step, i -> 10L), Integer::sum).peek(System.out::println)
                .join().peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testRetryPar", builder, PIPELINE_EXECUTOR)) {
            final Future<Integer> f = pipeline.execute(1);
            final int r = f.get();
            Assert.assertEquals("testRetryPar", 18, r);
        }
    }

    @org.junit.Test
    public void testOrder() throws Exception {
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .addOrderedStep("ordered", OrderStep.INSTANCE).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testOrder", builder, PIPELINE_EXECUTOR)) {
            final int count = 100;
            final List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                fs.add(pipeline.execute(i));
            }
            for (int i = 0; i < count; i++) {
                final Future<Integer> f = fs.get(i);
                final int r = f.get();
                Assert.assertEquals("testOrder", i, r);
            }
        }
    }

    @org.junit.Test
    public void testOrderBuilder() throws Exception {
        final PipelineBuilder<Integer, Integer> step = p -> p.peek(System.out::println)
                .addStep("step_1", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_2", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_3", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_4", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_5", OrderStep.INSTANCE).peek(System.out::println);
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .buildOrderedStep("ordered", step).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testOrderBuilder", builder, PIPELINE_EXECUTOR)) {
            final int count = 100;
            final List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                fs.add(pipeline.execute(i));
            }
            for (int i = 0; i < count; i++) {
                final Future<Integer> f = fs.get(i);
                final int r = f.get();
                Assert.assertEquals("testOrderBuilder", i, r);
            }
        }
    }

    @org.junit.Test
    public void testOrderPartitionBuilder() throws Exception {
        final PipelineBuilder<Integer, Integer> step = p -> p.peek(System.out::println)
                .addStep("step_1", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_2", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_3", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_4", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_5", OrderStep.INSTANCE).peek(System.out::println);

        final int numOfPartitions = 20;
        final List<PipelineBuilder<Integer, Integer>> partitions = new ArrayList<>(numOfPartitions);
        for (int i = 0; i < numOfPartitions; i++) {
            final String name = "ordered_" + (i + 1);
            final PipelineBuilder<Integer, Integer> partition = p -> p.peek(System.out::println)
                    .buildOrderedStep(name, step).peek(System.out::println);
            partitions.add(partition);
        }
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .buildSwitchStep("switch", i -> Math.abs(i) % numOfPartitions, partitions).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testOrderPartitionBuilder", builder, PIPELINE_EXECUTOR)) {
            final int count = 100;
            final List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                fs.add(pipeline.execute(i));
            }
            for (int i = 0; i < count; i++) {
                final Future<Integer> f = fs.get(i);
                final int r = f.get();
                Assert.assertEquals("testOrderPartitionBuilder", i, r);
            }
        }
    }

    @org.junit.Test
    public void testUnorder() throws Exception {
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .addStep("unordered", OrderStep.INSTANCE).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testUnorder", builder, PIPELINE_EXECUTOR)) {
            final int count = 100;
            final List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                fs.add(pipeline.execute(i));
            }
            for (int i = 0; i < count; i++) {
                final Future<Integer> f = fs.get(i);
                final int r = f.get();
                Assert.assertEquals("testUnorder", i, r);
            }
        }
    }

    @org.junit.Test
    public void testUnorderBuilder() throws Exception {
        final PipelineBuilder<Integer, Integer> step = p -> p.peek(System.out::println)
                .addStep("step_1", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_2", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_3", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_4", OrderStep.INSTANCE).peek(System.out::println)
                .addStep("step_5", OrderStep.INSTANCE).peek(System.out::println);
        final PipelineBuilder<Integer, Integer> builder = p -> p.peek(System.out::println)
                .buildStep("unordered", step).peek(System.out::println);
        try (final Pipeline<Integer, Integer> pipeline = Pipeline.build("testUnorderBuilder", builder, PIPELINE_EXECUTOR)) {
            final int count = 100;
            final List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                fs.add(pipeline.execute(i));
            }
            for (int i = 0; i < count; i++) {
                final Future<Integer> f = fs.get(i);
                final int r = f.get();
                Assert.assertEquals("testUnorderBuilder", i, r);
            }
        }
    }

    private static final class IntToLongStep implements PipelineStep<Integer, Long>, Runnable {
        private static final IntToLongStep INSTANCE = new IntToLongStep();

        private final PipelineEntry<Integer, Long> entry;

        private IntToLongStep() {
            this(null);
        }

        private IntToLongStep(final PipelineEntry<Integer, Long> entry) {
            this.entry = entry;
        }

        private static void exec(final PipelineEntry<Integer, Long> entry) {
            final int input = entry.get();
            final long result = input * 2L;
            entry.accept(result);
        }

        @Override
        public final void process(final PipelineEntry<Integer, Long> entry) {
            exec(entry);
        }

        @Override
        public final void run() {
            exec(this.entry);
        }
    }

    private static final class LongToDoubleStep implements PipelineStep<Long, Double> {
        private static final LongToDoubleStep INSTANCE = new LongToDoubleStep();

        @Override
        public final void process(final PipelineEntry<Long, Double> entry) {
            final long input = entry.get();
            final double result = input * 2.0;
            entry.accept(result);
        }
    }

    private static final class IntToLongForkStep implements PipelineStep<Integer, Long> {
        private static final IntToLongForkStep INSTANCE = new IntToLongForkStep();

        @Override
        public final void process(final PipelineEntry<Integer, Long> entry) {
            AUX_EXECUTOR.execute(() -> IntToLongStep.INSTANCE.process(entry));
        }
    }

    private static final class LongToDoubleForkStep implements PipelineStep<Long, Double> {
        private static final LongToDoubleForkStep INSTANCE = new LongToDoubleForkStep();

        @Override
        public final void process(final PipelineEntry<Long, Double> entry) {
            AUX_EXECUTOR.execute(() -> LongToDoubleStep.INSTANCE.process(entry));
        }
    }

    private static final class EvenStep implements PipelineStep<Integer, Boolean> {
        private static final EvenStep INSTANCE = new EvenStep();

        @Override
        public final void process(final PipelineEntry<Integer, Boolean> entry) {
            AUX_EXECUTOR.execute(() -> {
                if (entry.get() % 2 != 0) {
                    entry.stop(new AppException("Not even"));
                } else {
                    entry.accept(true);
                }
            });
        }
    }

    private static final class OddStep implements PipelineStep<Integer, Boolean> {
        private static final OddStep INSTANCE = new OddStep();

        @Override
        public final void process(final PipelineEntry<Integer, Boolean> entry) {
            AUX_EXECUTOR.execute(() -> {
                if (entry.get() % 2 != 1) {
                    entry.stop(new AppException("Not odd"));
                } else {
                    entry.accept(false);
                }
            });
        }
    }

    private static final class IncrementStep implements PipelineStep<Integer, Integer> {
        private static final IncrementStep INSTANCE = new IncrementStep();

        @Override
        public final void process(final PipelineEntry<Integer, Integer> entry) {
            AUX_EXECUTOR.execute(() -> entry.accept(entry.get() + 1));
        }
    }

    private static final class OrderStep implements PipelineStep<Integer, Integer> {
        private static final OrderStep INSTANCE = new OrderStep();

        @Override
        public final void process(final PipelineEntry<Integer, Integer> entry) {
            if (ThreadLocalRandom.current().nextDouble() < 0.7) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    entry.retry(100, TimeUnit.MILLISECONDS);
                } else {
                    entry.accept(entry.get(), 100, TimeUnit.MILLISECONDS);
                }
            } else {
                entry.accept(entry.get());
            }
        }
    }
}
