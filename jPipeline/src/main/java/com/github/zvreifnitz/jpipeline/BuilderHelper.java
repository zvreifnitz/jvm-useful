package com.github.zvreifnitz.jpipeline;

import com.github.zvreifnitz.jpipeline.step.Steps;
import com.github.zvreifnitz.jpipeline.utils.Builders;
import com.github.zvreifnitz.jpipeline.utils.Funcs;
import com.github.zvreifnitz.jpipeline.utils.Mergers;

import java.util.function.*;

public final class BuilderHelper {

    private BuilderHelper() {
    }

    public static <T> Function<T, T> func() {
        return Funcs.identityFunc();
    }

    public static <T> Function<T, T> func(final Consumer<T> consumer) {
        return Funcs.consumerToFunc(consumer);
    }

    public static <T, R> Function<T, R> func(final Supplier<R> supplier) {
        return Funcs.supplierToFunc(supplier);
    }

    public static <T, R> Function<T, R> func(final Consumer<T> consumer, final Supplier<R> supplier) {
        return Funcs.combine(consumer, supplier);
    }

    public static <T, X, R> Function<T, R> func(final Function<T, X> first, final Function<X, R> second) {
        return Funcs.combine(first, second);
    }

    public static <T> PipelineStep<T, T> step() {
        return Steps.identity();
    }

    public static <T, R> PipelineStep<T, R> step(final Function<T, R> func) {
        return Steps.functionToStep(func);
    }

    public static <T, R> PipelineStep<T, R> step(
            final Function<T, R> func, final LongUnaryOperator sleeper) {
        return Steps.retry(step(func), sleeper);
    }

    public static <T, R> PipelineStep<T, R> adapter(final PipelineStep<T, R> step) {
        return Steps.adapter(step);
    }

    public static <T, X, R> PipelineStep<T, R> adapter(final PipelineStep<T, X> step, final BiFunction<T, X, R> merger) {
        return Steps.adapter(step, merger);
    }

    public static <T, X, R> PipelineStep<T, R> adapter(final Function<T, X> extractor, final PipelineStep<X, R> step) {
        return Steps.adapter(extractor, step);
    }

    public static <T, X, Y, R> PipelineStep<T, R> adapter(
            final Function<T, X> extractor, final PipelineStep<X, Y> step, final BiFunction<T, Y, R> merger) {
        return Steps.adapter(extractor, step, merger);
    }

    public static <T, R> PipelineStep<T, R> retry(
            final PipelineStep<T, R> step, final LongUnaryOperator sleeper) {
        return Steps.retry(step, sleeper);
    }

    public static <T, R> PipelineBuilder<T, R> builder(final PipelineBuilder<T, R> builder) {
        return Builders.adapter(builder);
    }

    public static <T, X, R> PipelineBuilder<T, R> adapter(
            final PipelineBuilder<T, X> builder, final BiFunction<T, X, R> merger) {
        return Builders.adapter(builder, merger);
    }

    public static <T, X, R> PipelineBuilder<T, R> adapter(
            final Function<T, X> extractor, final PipelineBuilder<X, R> builder) {
        return Builders.adapter(extractor, builder);
    }

    public static <T, X, Y, R> PipelineBuilder<T, R> adapter(
            final Function<T, X> extractor, final PipelineBuilder<X, Y> builder, final BiFunction<T, Y, R> merger) {
        return Builders.adapter(extractor, builder, merger);
    }

    public static <T, X> BiFunction<T, X, X> merger() {
        return Funcs.passThroughFunc();
    }

    public static <T, X, R> BiFunction<T, X, R> merger(final Function<X, R> merger) {
        return Mergers.merger(merger);
    }

    public static <T, X> BiFunction<T, X, T> merger(final BiConsumer<T, X> merger) {
        return Mergers.merger(merger);
    }
}
