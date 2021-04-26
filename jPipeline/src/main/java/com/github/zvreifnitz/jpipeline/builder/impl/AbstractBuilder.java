package com.github.zvreifnitz.jpipeline.builder.impl;

import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.builder.CommonBuilder;
import com.github.zvreifnitz.jpipeline.builder.ParallelBuilder;
import com.github.zvreifnitz.jpipeline.builder.SequentialBuilder;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;
import com.github.zvreifnitz.jpipeline.runnable.impl.RunnableFactories;
import com.github.zvreifnitz.jpipeline.step.Steps;

abstract class AbstractBuilder<T, R> implements CommonBuilder<T, R> {

    protected final String pipelineName;
    protected final String stepName;
    protected final PipelineExecutor executor;

    protected AbstractBuilder(final String pipelineName, final String stepName, final PipelineExecutor executor) {
        this.pipelineName = pipelineName;
        this.stepName = stepName;
        this.executor = executor;
    }

    static <T, R, F> RunnableFactory<T, F> buildRunnableFactory(
            final String pipelineName, final PipelineExecutor pipelineExecutor,
            final PipelineBuilder<T, R> builder, final RunnableFactory<R, F> next) {
        return SequentialBuilders
                .<T>step(pipelineName, pipelineExecutor)
                .materialize(builder)
                .buildRunnableFactory(next);
    }

    static String pipelineName(final String pipelineName, final String stepName) {
        return pipelineName + "." + stepName;
    }

    static <T, R> PipelineStep<T, R> convertToStep(
            final String name, final AbstractBuilder<?, ?> prev, final PipelineBuilder<T, R> builder) {
        return convertToStep(prev.pipelineName, name, prev.executor, builder);
    }

    static <T, R> PipelineStep<T, R> convertToStep(
            final String pipelineName, final String stepName,
            final PipelineExecutor executor, final PipelineBuilder<T, R> builder) {
        final RunnableFactory<T, R> factory = AbstractBuilder.buildRunnableFactory(
                AbstractBuilder.pipelineName(pipelineName, stepName),
                executor, builder, RunnableFactories.create());
        return Steps.step(factory);
    }

    final <P, F> RunnableFactory<P, F> buildRunnableFactory(
            final String pipelineName, final PipelineBuilder<P, R> builder, final RunnableFactory<R, F> next) {
        return buildRunnableFactory(pipelineName, this.executor, builder, next);
    }

    abstract <F> AbstractBuilder<T, F> materialize(final PipelineBuilder<T, F> builder);

    abstract <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<R, F> next);

    abstract static class Sequential<T1, R1>
            extends AbstractBuilder<T1, R1>
            implements SequentialBuilder<T1, R1> {
        protected Sequential(final String pipelineName, final String stepName, final PipelineExecutor executor) {
            super(pipelineName, stepName, executor);
        }
    }

    abstract static class Parallel<T1, I1, R1>
            extends AbstractBuilder<T1, R1>
            implements ParallelBuilder<T1, I1, R1> {
        protected Parallel(final String pipelineName, final String stepName, final PipelineExecutor executor) {
            super(pipelineName, stepName, executor);
        }
    }
}
