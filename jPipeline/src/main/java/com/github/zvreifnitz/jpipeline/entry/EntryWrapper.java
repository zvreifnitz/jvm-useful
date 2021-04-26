package com.github.zvreifnitz.jpipeline.entry;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.builder.Merger;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class EntryWrapper<T, X, Y, R> extends AbstractEntry<T, R> {

    protected final PipelineEntry<X, Y> entry;
    protected final Merger<X, R, Y> merger;

    protected EntryWrapper(
            final PipelineEntry<X, Y> entry,
            final Function<X, T> inputConverter,
            final Merger<X, R, Y> merger) {
        super(entry.getRunId(), inputConverter.apply(entry.get()));
        this.entry = entry;
        this.merger = merger;
    }

    @Override
    public boolean tryAccept(final R result, final long time, final TimeUnit timeUnit) {
        try {
            final Y r = this.merger.apply(this.entry, result);
            return this.entry.tryAccept(r, time, timeUnit);
        } catch (final Throwable exception) {
            this.entry.tryStop(exception);
            Exceptions.rethrowIfError(exception);
            return false;
        }
    }

    @Override
    public boolean tryStop(final Throwable throwable) {
        return this.entry.tryStop(throwable);
    }

    @Override
    public boolean tryRetry(final long time, final TimeUnit timeUnit) {
        return this.entry.tryRetry(time, timeUnit);
    }
}
