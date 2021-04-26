package com.github.zvreifnitz.jpipeline;

import com.github.zvreifnitz.jpipeline.builder.impl.Builder;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface Pipeline<T, R> extends PipelineStep<T, R>, AutoCloseable {

    static <T, R> Pipeline<T, R> build(final PipelineBuilder<T, R> builder) {
        return build("", builder, PipelineExecutor.defaultExecutor());
    }

    static <T, R> Pipeline<T, R> build(
            final String name, final PipelineBuilder<T, R> builder) {
        return build(name, builder, PipelineExecutor.defaultExecutor());
    }

    static <T, R> Pipeline<T, R> build(final PipelineBuilder<T, R> builder, final Executor executor) {
        return build("", builder, executor);
    }

    static <T, R> Pipeline<T, R> build(
            final String name, final PipelineBuilder<T, R> builder, final Executor executor) {
        return Builder.build(name, builder, executor);
    }

    default Future<R> execute(final T input) {
        return this.execute(null, input);
    }

    Future<R> execute(final String runId, final T input);

    default void executeAsync(final T input, final Consumer<Future<R>> consumer) {
        this.executeAsync(null, input, consumer);
    }

    void executeAsync(final String runId, final T input, final Consumer<Future<R>> consumer);

    @Override
    void close();
}
