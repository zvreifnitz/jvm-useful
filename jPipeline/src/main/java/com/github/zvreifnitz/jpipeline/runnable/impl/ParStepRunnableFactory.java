package com.github.zvreifnitz.jpipeline.runnable.impl;

import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.pipeline.Finalizer;
import com.github.zvreifnitz.jpipeline.runnable.ParallelRunnablesFactory;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

final class ParStepRunnableFactory<T, R, F> implements RunnableFactory<T, F> {

    private final PipelineExecutor executor;
    private final ParallelRunnablesFactory<T, R> taskFactory;
    private final RunnableFactory<R, F> factory;

    ParStepRunnableFactory(
            final PipelineExecutor executor, final ParallelRunnablesFactory<T, R> taskFactory,
            final RunnableFactory<R, F> factory) {
        this.executor = executor;
        this.taskFactory = taskFactory;
        this.factory = factory;
    }

    @Override
    public final Runnable create(final String runId, final T input, final Finalizer<F> finalizer) {
        return new ParStepRunnable(runId, input, finalizer);
    }

    private final class ParStepRunnable extends AbstractRunnable<T, R, F> implements Runnable {

        private ParStepRunnable(final String runId, final T input, final Finalizer<F> finalizer) {
            super(runId, input, finalizer);
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            final List<Runnable> runnables = ParStepRunnableFactory.this.taskFactory.create(entry);
            final Runnable runnable = runnables.remove(runnables.size() - 1);
            ParStepRunnableFactory.this.executor.execute(runnables);
            runnable.run();
        }

        @Override
        protected final Runnable createNext(final R result) {
            return ParStepRunnableFactory.this.factory.create(this.getRunId(), result, this.getFinalizer());
        }

        @Override
        protected final Runnable createSame() {
            return new ParStepRunnable(this.getRunId(), this.get(), this.getFinalizer());
        }

        @Override
        protected final void enqueue(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            PipelineExecutor.executeOrSchedule(
                    ParStepRunnableFactory.this.executor, runnable, time, timeUnit);
        }
    }
}