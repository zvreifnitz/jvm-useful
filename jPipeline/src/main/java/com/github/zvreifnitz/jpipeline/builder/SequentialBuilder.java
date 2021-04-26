package com.github.zvreifnitz.jpipeline.builder;

import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.utils.Funcs;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SequentialBuilder<T, R> extends CommonBuilder<T, R> {

    default <N, X> ParallelBuilder<T, R, N> buildParallelStep(
            final PipelineBuilder<R, X> builder, final BiFunction<R, X, N> merger) {
        return this.buildParallelStep("", builder, merger);
    }

    <N, X> ParallelBuilder<T, R, N> buildParallelStep(
            final String name, final PipelineBuilder<R, X> builder, final BiFunction<R, X, N> merger);

    default <N, X> ParallelBuilder<T, R, N> addParallelStep(
            final PipelineStep<R, X> step, final BiFunction<R, X, N> merger) {
        return this.addParallelStep("", step, merger);
    }

    <N, X> ParallelBuilder<T, R, N> addParallelStep(
            final String name, final PipelineStep<R, X> step, final BiFunction<R, X, N> merger);

    default SequentialBuilder<T, R> peek(final Consumer<PipelineValue<R>> consumer) {
        return this.peek(Funcs.identityFunc(), consumer);
    }

    <N> SequentialBuilder<T, R> peek(final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer);
}
