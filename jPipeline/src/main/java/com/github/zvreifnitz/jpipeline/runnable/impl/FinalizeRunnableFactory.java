package com.github.zvreifnitz.jpipeline.runnable.impl;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.pipeline.Finalizer;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

final class FinalizeRunnableFactory<R> implements RunnableFactory<R, R> {

    FinalizeRunnableFactory() {
    }

    @Override
    public final Runnable create(final String runId, final R result, final Finalizer<R> finalizer) {
        return new FinalizeRunnable<>(result, finalizer);
    }

    private static final class FinalizeRunnable<I>
            implements Runnable {

        private final I result;
        private final Finalizer<I> finalizer;

        private FinalizeRunnable(final I result, final Finalizer<I> finalizer) {
            this.result = result;
            this.finalizer = finalizer;
        }

        @Override
        public final void run() {
            try {
                this.finalizer.setResult(this.result);
            } catch (final Throwable exception) {
                this.finalizer.setError(exception);
                Exceptions.rethrowIfError(exception);
            }
        }
    }
}
