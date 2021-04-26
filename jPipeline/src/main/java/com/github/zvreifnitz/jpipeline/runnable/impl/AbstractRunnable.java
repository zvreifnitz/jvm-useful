package com.github.zvreifnitz.jpipeline.runnable.impl;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.entry.ProtectingEntry;
import com.github.zvreifnitz.jpipeline.pipeline.Finalizer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRunnable<T, R, F> extends ProtectingEntry<T, R> implements PipelineStep<T, R>, Runnable {

    private static final VarHandle STATE;

    static {
        try {
            final MethodHandles.Lookup l = MethodHandles.lookup();
            STATE = l.findVarHandle(AbstractRunnable.class, "state", Integer.TYPE);
        } catch (final Exception exc) {
            throw Exceptions.staticCtorFail(exc);
        }
    }

    private final Finalizer<F> finalizer;
    private volatile int state;
    private Runnable next;

    protected AbstractRunnable(final String runId, final T input, final Finalizer<F> finalizer) {
        super(runId, input);
        this.finalizer = finalizer;
    }

    @Override
    public final void run() {
        try {
            this.setState(0);
            this.process(this);
            if (this.compareAndSetState(0, 1)) {
                return;
            }
            this.next.run();
            this.next = null;
        } catch (final Throwable exception) {
            this.handleException(exception);
        }
    }

    @Override
    protected final void processException(final Throwable throwable) {
        try {
            this.finalizer.setError(throwable);
        } catch (final Throwable exception) {
            Exceptions.rethrowIfError(exception);
        }
    }

    @Override
    protected final void processResult(final R result, final long time, final TimeUnit timeUnit) {
        try {
            final Runnable runnable = this.createNext(result);
            this.processRunnable(runnable, time, timeUnit, 1L);
        } catch (final Throwable exception) {
            this.handleException(exception);
        }
    }

    @Override
    protected final void processRetry(final long time, final TimeUnit timeUnit) {
        try {
            final Runnable runnable = this.createSame();
            this.processRunnable(runnable, time, timeUnit, Long.MIN_VALUE);
        } catch (final Throwable exception) {
            this.handleException(exception);
        }
    }

    protected Finalizer<F> getFinalizer() {
        return this.finalizer;
    }

    protected abstract Runnable createNext(final R result);

    protected abstract Runnable createSame();

    protected abstract void enqueue(final Runnable runnable, final long time, final TimeUnit timeUnit);

    private void processRunnable(final Runnable runnable, final long time, final TimeUnit timeUnit, final long limit) {
        this.next = (time < limit) ? runnable : new EnqueueTask(runnable, time, timeUnit);
        if (this.compareAndSetState(0, 1)) {
            return;
        }
        this.next = null;
        this.enqueue(runnable, time, timeUnit);
    }

    private void handleException(final Throwable exception) {
        this.processException(exception);
        Exceptions.rethrowIfError(exception);
    }

    private void setState(final int value) {
        if (this.state != value) {
            this.state = value;
        }
    }

    private boolean compareAndSetState(final int expected, final int value) {
        return ((this.state == expected) && STATE.compareAndSet(this, expected, value));
    }

    private final class EnqueueTask implements Runnable {

        private final Runnable runnable;
        private final long time;
        private final TimeUnit timeUnit;

        private EnqueueTask(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            this.runnable = runnable;
            this.time = time;
            this.timeUnit = timeUnit;
        }

        @Override
        public final void run() {
            try {
                AbstractRunnable.this.enqueue(this.runnable, this.time, this.timeUnit);
            } catch (final Throwable exception) {
                AbstractRunnable.this.handleException(exception);
            }
        }
    }
}
