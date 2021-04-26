package com.github.zvreifnitz.jpipeline.pipeline;

import com.github.zvreifnitz.jcore.concurrent.Futures;
import com.github.zvreifnitz.jpipeline.PipelineEntry;

import java.util.concurrent.*;
import java.util.function.Consumer;

public final class Finalizers {

    private Finalizers() {
    }

    public static <R> Finalizer.FinalizerFuture<R> future() {
        return new FutureFinalizer<>();
    }

    public static <R> Finalizer<R> fromConsumer(final Consumer<Future<R>> consumer) {
        return new ConsumerFinalizer<>(consumer);
    }

    public static <R> Finalizer<R> fromPipelineEntry(final PipelineEntry<?, R> entry) {
        return new PipelineEntryFinalizer<>(entry);
    }

    private static final class FutureFinalizer<R>
            implements Finalizer.FinalizerFuture<R>, Finalizer<R>, Future<R> {

        private final CountDownLatch latch = new CountDownLatch(1);
        private R result;
        private ExecutionException exc;

        private FutureFinalizer() {
        }

        @Override
        public final boolean cancel(final boolean interrupt) {
            return false;
        }

        @Override
        public final boolean isCancelled() {
            return false;
        }

        @Override
        public final boolean isDone() {
            return (this.latch.getCount() <= 0L);
        }

        @Override
        public final R get() throws InterruptedException, ExecutionException {
            this.latch.await();
            return this.getResult();
        }

        @Override
        public final R get(final long period, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            if (!this.latch.await(period, timeUnit)) {
                throw new TimeoutException();
            }
            return this.getResult();
        }

        private R getResult() throws ExecutionException {
            if (this.exc != null) {
                throw this.exc;
            }
            return this.result;
        }

        @Override
        public final void setResult(final R result) {
            this.result = result;
            this.latch.countDown();
        }

        @Override
        public final void setError(final Throwable throwable) {
            this.exc = Futures.toExecutionException(throwable);
            this.latch.countDown();
        }
    }

    private static final class ConsumerFinalizer<R>
            implements Finalizer<R> {

        private final Consumer<Future<R>> consumer;

        private ConsumerFinalizer(final Consumer<Future<R>> consumer) {
            this.consumer = consumer;
        }

        @Override
        public final void setResult(final R result) {
            try {
                this.consumer.accept(Futures.success(result));
            } catch (final Exception ignored) {
            }
        }

        @Override
        public final void setError(final Throwable throwable) {
            try {
                this.consumer.accept(Futures.failure(throwable));
            } catch (final Exception ignored) {
            }
        }
    }

    private static final class PipelineEntryFinalizer<R>
            implements Finalizer<R> {

        private final PipelineEntry<?, R> entry;

        private PipelineEntryFinalizer(final PipelineEntry<?, R> entry) {
            this.entry = entry;
        }

        @Override
        public final void setResult(final R result) {
            this.entry.accept(result);
        }

        @Override
        public final void setError(final Throwable throwable) {
            this.entry.stop(throwable);
        }
    }
}
