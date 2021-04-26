package com.github.zvreifnitz.jpipeline.pipeline;

import com.github.zvreifnitz.jcore.Preconditions;
import com.github.zvreifnitz.jpipeline.Pipeline;
import com.github.zvreifnitz.jpipeline.PipelineEntry;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public abstract class AbstractPipeline<T, R> implements Pipeline<T, R> {

    @Override
    public final Future<R> execute(final T input) {
        return this.execute(null, input);
    }

    @Override
    public final Future<R> execute(final String runId, final T input) {
        final Finalizer.FinalizerFuture<R> result = Finalizers.future();
        this.execute(runId, input, result);
        return result;
    }

    @Override
    public final void executeAsync(final T input, final Consumer<Future<R>> consumer) {
        this.executeAsync(null, input, consumer);
    }

    @Override
    public final void executeAsync(final String runId, final T input, final Consumer<Future<R>> consumer) {
        Preconditions.checkNullPointer(consumer, "consumer");
        this.execute(runId, input, Finalizers.fromConsumer(consumer));
    }

    @Override
    public final void process(final PipelineEntry<T, R> entry) {
        Preconditions.checkNullPointer(entry, "entry");
        this.execute(entry.getRunId(), entry.get(), Finalizers.fromPipelineEntry(entry));
    }

    private void execute(final String runId, final T input, final Finalizer<R> finalizer) {
        try {
            final String id = (runId == null) ? UUID.randomUUID().toString() : runId;
            this.doExecute(id, input, finalizer);
        } catch (final Exception e) {
            finalizer.setError(e);
        }
    }

    protected abstract void doExecute(final String runId, final T input, final Finalizer<R> finalizer);
}
