package com.github.zvreifnitz.jpipeline.builder.impl;

import com.github.zvreifnitz.jpipeline.Pipeline;
import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutors;
import com.github.zvreifnitz.jpipeline.pipeline.Pipelines;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;
import com.github.zvreifnitz.jpipeline.runnable.impl.RunnableFactories;

import java.util.concurrent.Executor;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

public final class Builder {

    private Builder() {
    }

    public static <R, T> Pipeline<T, R> build(
            final String name, final PipelineBuilder<T, R> builder, final Executor executor) {
        checkNullPointer(name, "name");
        checkNullPointer(builder, "builder");
        checkNullPointer(executor, "executor");
        final PipelineExecutor pipelineExecutor = PipelineExecutors.fromExecutor(executor);
        final RunnableFactory<T, R> factory = AbstractBuilder.buildRunnableFactory(
                name, pipelineExecutor, builder, RunnableFactories.create());
        return Pipelines.unbounded(pipelineExecutor, factory);
    }
}
