package com.github.zvreifnitz.jpipeline.runnable.impl;

import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.runnable.ParallelRunnablesFactory;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

import java.util.List;
import java.util.function.ToIntFunction;

public final class RunnableFactories {

    private RunnableFactories() {
    }

    public static <R> RunnableFactory<R, R> create() {
        return new FinalizeRunnableFactory<>();
    }

    public static <T, R, F> RunnableFactory<T, F> create(
            final PipelineExecutor executor, final PipelineStep<T, R> step, final RunnableFactory<R, F> nextFactory) {
        return new SeqStepRunnableFactory<>(executor, step, nextFactory);
    }

    public static <T, R, F> RunnableFactory<T, F> create(
            final PipelineExecutor executor, final ParallelRunnablesFactory<T, R> taskFactory,
            final RunnableFactory<R, F> factory) {
        return new ParStepRunnableFactory<>(executor, taskFactory, factory);
    }

    public static <T, R> RunnableFactory<T, R> create(
            final ToIntFunction<T> switcher, final List<RunnableFactory<T, R>> factories) {
        return new SwitchRunnableFactory<>(switcher, factories);
    }
}
