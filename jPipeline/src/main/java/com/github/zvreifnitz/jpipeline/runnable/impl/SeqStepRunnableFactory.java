package com.github.zvreifnitz.jpipeline.runnable.impl;

import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.pipeline.Finalizer;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

import java.util.concurrent.TimeUnit;

final class SeqStepRunnableFactory<T, R, F> implements RunnableFactory<T, F> {

    private final PipelineExecutor executor;
    private final PipelineStep<T, R> step;
    private final RunnableFactory<R, F> factory;

    SeqStepRunnableFactory(
            final PipelineExecutor executor, final PipelineStep<T, R> step, final RunnableFactory<R, F> factory) {
        this.executor = executor;
        this.step = step;
        this.factory = factory;
    }

    @Override
    public final Runnable create(final String runId, final T input, final Finalizer<F> finalizer) {
        return new SeqStepRunnable(runId, input, finalizer);
    }

    private final class SeqStepRunnable extends AbstractRunnable<T, R, F> implements Runnable {

        private SeqStepRunnable(final String runId, final T input, final Finalizer<F> finalizer) {
            super(runId, input, finalizer);
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            SeqStepRunnableFactory.this.step.process(entry);
        }

        @Override
        protected final Runnable createNext(final R result) {
            return SeqStepRunnableFactory.this.factory.create(this.getRunId(), result, this.getFinalizer());
        }

        @Override
        protected final Runnable createSame() {
            return new SeqStepRunnable(this.getRunId(), this.get(), this.getFinalizer());
        }

        @Override
        protected final void enqueue(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            PipelineExecutor.executeOrSchedule(
                    SeqStepRunnableFactory.this.executor, runnable, time, timeUnit);
        }
    }
}