package com.github.zvreifnitz.jpipeline.entry;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.PipelineEntry;

import java.util.concurrent.TimeUnit;

public abstract class AbstractEntry<T, R> extends AbstractValue<T> implements PipelineEntry<T, R> {

    protected AbstractEntry(final String runId, final T value) {
        super(runId, value);
    }

    @Override
    public final void accept(final R result) {
        if (this.tryAccept(result)) {
            return;
        }
        this.throwAlreadyDone();
    }

    @Override
    public final void accept(final R result, final long time, final TimeUnit timeUnit) {
        if (this.tryAccept(result, time, timeUnit)) {
            return;
        }
        this.throwAlreadyDone();
    }

    @Override
    public final boolean tryAccept(final R result) {
        return this.tryAccept(result, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    public final void stop(final Throwable throwable) {
        if (this.tryStop(throwable)) {
            return;
        }
        this.throwAlreadyDone();
    }

    @Override
    public final void retry() {
        if (this.tryRetry()) {
            return;
        }
        this.throwAlreadyDone();
    }

    @Override
    public final void retry(final long time, final TimeUnit timeUnit) {
        if (this.tryRetry(time, timeUnit)) {
            return;
        }
        this.throwAlreadyDone();
    }

    @Override
    public final boolean tryRetry() {
        return this.tryRetry(0L, TimeUnit.MILLISECONDS);
    }

    private void throwAlreadyDone() {
        Exceptions.throwIllegalState("PipelineEntry already finalized");
    }
}
