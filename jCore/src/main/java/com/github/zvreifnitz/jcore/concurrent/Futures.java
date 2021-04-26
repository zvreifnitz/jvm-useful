package com.github.zvreifnitz.jcore.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Futures {

    private Futures() {
    }

    public static <T> Future<T> success(final T result) {
        return new SuccessFuture<>(result);
    }

    public static <T> Future<T> failure(final Throwable throwable) {
        return new FailureFuture<>(toExecutionException(throwable));
    }

    public static ExecutionException toExecutionException(final Throwable throwable) {
        if (throwable instanceof ExecutionException) {
            return (ExecutionException) throwable;
        } else {
            return new ExecutionException(throwable);
        }
    }

    private abstract static class DoneFuture<R> implements Future<R> {

        @Override
        public final boolean cancel(final boolean b) {
            return false;
        }

        @Override
        public final boolean isCancelled() {
            return false;
        }

        @Override
        public final boolean isDone() {
            return true;
        }

        @Override
        public final R get(final long l, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.get();
        }
    }

    private static final class SuccessFuture<R> extends DoneFuture<R> {

        private final R result;

        private SuccessFuture(final R result) {
            this.result = result;
        }

        @Override
        public final R get() throws InterruptedException, ExecutionException {
            return this.result;
        }
    }

    private static final class FailureFuture<R> extends DoneFuture<R> {

        private final ExecutionException exc;

        private FailureFuture(final ExecutionException exc) {
            this.exc = exc;
        }

        @Override
        public final R get() throws InterruptedException, ExecutionException {
            throw this.exc;
        }
    }
}
