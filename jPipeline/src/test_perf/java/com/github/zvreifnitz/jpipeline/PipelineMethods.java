package com.github.zvreifnitz.jpipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

import static com.github.zvreifnitz.jpipeline.BuilderHelper.merger;
import static com.github.zvreifnitz.jpipeline.BuilderHelper.step;

public final class PipelineMethods {

    private PipelineMethods() {
    }

    public static double getResult(final Future<Double> future) {
        try {
            return future.get();
        } catch (final Exception ignored) {
            return 0.0;
        }
    }

    public static Pipeline<Double, Double> buildSequentialPipeline(final Executor executor) {
        final PipelineStep<Double, SeqStepData> initStep = step(SeqStepData::new);
        final PipelineStep<SeqStepData, SeqStepData> calcStep = step(s -> {
            s.list.add(PipelineMethods.numOfSteps(s.N));
            return s;
        });
        final PipelineStep<SeqStepData, Double> resultStep = step(s -> {
            return PipelineMethods.avg(s.list, s.N);
        });

        final PipelineBuilder<Double, Double> builder = p -> p
                .addStep(initStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(calcStep)
                .addStep(resultStep);
        return Pipeline.build(builder, executor);
    }

    public static Pipeline<Double, Double> buildParallelPipeline(final Executor executor) {
        final PipelineStep<Double, List<Integer>> initStep = step(i -> new ArrayList<>());
        final BiFunction<Double, List<Integer>, List<Integer>> initMerger = merger();
        final PipelineStep<Double, Integer> calcStep = step(PipelineMethods::numOfSteps);
        final BiFunction<List<Integer>, Integer, List<Integer>> calcMerger = merger(List::add);
        final PipelineStep<Double, Double> resultStep = step();
        final BiFunction<List<Integer>, Double, Double> resultMerger = PipelineMethods::avg;

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
        return Pipeline.build(builder, executor);
    }

    public static int numOfSteps(final double limit) {
        int result = 0;
        double sum = 0.0;
        while (sum < limit) {
            result++;
            sum += ThreadLocalRandom.current().nextDouble();
        }
        return result;
    }

    public static double avg(final List<Integer> vals, final double limit) {
        long sum = 0;
        for (final int v : vals) {
            sum += v;
        }
        return (vals.size() * limit) / sum;
    }

    private static final class SeqStepData {
        private final double N;
        private final List<Integer> list;

        private SeqStepData(final double n) {
            this.N = n;
            this.list = new ArrayList<>();
        }
    }
}
