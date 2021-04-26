package com.github.zvreifnitz.jpipeline.pipeline;

import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

public final class UnboundedPipeline<T, R> extends AbstractPipeline<T, R> {

    private final PipelineExecutor executor;
    private final RunnableFactory<T, R> runnableFactory;

    public UnboundedPipeline(final PipelineExecutor executor, final RunnableFactory<T, R> runnableFactory) {
        this.executor = executor;
        this.runnableFactory = runnableFactory;
    }

    @Override
    public final void close() {
        this.executor.close();
    }

    @Override
    protected final void doExecute(final String runId, final T input, final Finalizer<R> finalizer) {
        final Runnable runnable = this.runnableFactory.create(runId, input, finalizer);
        this.executor.execute(runnable);
    }
}
