package com.github.zvreifnitz.jpipeline.entry;

import com.github.zvreifnitz.jcore.exc.Exceptions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;

import static com.github.zvreifnitz.jcore.Preconditions.checkArgument;
import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

public abstract class ProtectingEntry<T, R> extends AbstractEntry<T, R> {

    private static final VarHandle DONE;

    static {
        try {
            final MethodHandles.Lookup l = MethodHandles.lookup();
            DONE = l.findVarHandle(ProtectingEntry.class, "done", Integer.TYPE);
        } catch (final Exception exc) {
            throw Exceptions.staticCtorFail(exc);
        }
    }

    private volatile int done;

    protected ProtectingEntry(final String runId, final T input) {
        super(runId, input);
    }

    @Override
    public final boolean tryAccept(final R result, final long time, final TimeUnit timeUnit) {
        checkArgument(time >= 0L, "time", "Value must be positive.");
        checkNullPointer(timeUnit, "timeUnit");
        if (this.setDone()) {
            this.processResult(result, time, timeUnit);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean tryStop(final Throwable throwable) {
        checkNullPointer(throwable, "throwable");
        if (this.setDone()) {
            this.processException(throwable);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final boolean tryRetry(final long time, final TimeUnit timeUnit) {
        checkArgument(time >= 0L, "time", "Value must be positive.");
        checkNullPointer(timeUnit, "timeUnit");
        if (this.setDone()) {
            this.processRetry(time, timeUnit);
            return true;
        } else {
            return false;
        }
    }

    protected abstract void processException(final Throwable throwable);

    protected abstract void processResult(final R result, final long time, final TimeUnit timeUnit);

    protected abstract void processRetry(final long time, final TimeUnit timeUnit);

    protected final boolean isDone() {
        return (this.done > 0);
    }

    private boolean setDone() {
        return ((this.done == 0) && DONE.compareAndSet(this, 0, 1));
    }
}
