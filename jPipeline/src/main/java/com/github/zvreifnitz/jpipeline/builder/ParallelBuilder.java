package com.github.zvreifnitz.jpipeline.builder;

import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.utils.Funcs;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


public interface ParallelBuilder<T, I, R> extends CommonBuilder<T, R> {

    default <N, X> ParallelBuilder<T, I, N> buildParallelStep(
            final PipelineBuilder<I, X> builder, final BiFunction<R, X, N> merger) {
        return this.buildParallelStep("", builder, merger);
    }

    <N, X> ParallelBuilder<T, I, N> buildParallelStep(
            final String name, final PipelineBuilder<I, X> builder, final BiFunction<R, X, N> merger);

    default <N, X> ParallelBuilder<T, I, N> addParallelStep(
            final PipelineStep<I, X> step, final BiFunction<R, X, N> merger) {
        return this.addParallelStep("", step, merger);
    }

    <N, X> ParallelBuilder<T, I, N> addParallelStep(
            final String name, final PipelineStep<I, X> step, final BiFunction<R, X, N> merger);

    default ParallelBuilder<T, I, R> peek(final Consumer<PipelineValue<R>> consumer) {
        return this.peek(Funcs.identityFunc(), consumer);
    }

    <N> ParallelBuilder<T, I, R> peek(final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer);

    SequentialBuilder<T, R> join();
}
