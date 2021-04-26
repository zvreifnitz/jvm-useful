package com.github.zvreifnitz.jpipeline.builder.impl;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.builder.Merger;
import com.github.zvreifnitz.jpipeline.entry.Value;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.pipeline.Finalizers;
import com.github.zvreifnitz.jpipeline.runnable.ParallelRunnablesFactory;
import com.github.zvreifnitz.jpipeline.runnable.impl.AbstractRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

abstract class ParallelRunnables<I, R> implements ParallelRunnablesFactory<I, R> {

    static <I, R, X> ParallelRunnables<I, R> create(
            final PipelineStep<I, X> step, final Merger<I, X, R> merger, final PipelineExecutor executor) {
        return new InitRunnableGenerator<>(step, merger, executor);
    }

    static <I, P, R, X> ParallelRunnables<I, R> create(
            final ParallelRunnables<I, P> prev, final PipelineStep<I, X> step, final Merger<P, X, R> merger,
            final PipelineExecutor executor) {
        return new StepRunnableGenerator<>(prev, step, merger, executor);
    }

    @Override
    public final List<Runnable> create(final PipelineEntry<I, R> entry) {
        final List<Runnable> runnables = new ArrayList<>();
        final ResultBarrier<R> barrier = new ResultBarrier<>();
        final Result<R, ?, ?> endResult = this.createResultAndRunnables(entry, runnables, barrier);
        barrier.setup(runnables.size(), endResult);
        return runnables;
    }

    abstract <F> Result<R, ?, ?> createResultAndRunnables(
            final PipelineEntry<I, F> entry, final List<Runnable> runnables,
            final ResultBarrier<F> collector);

    private abstract static class RunnableGenerator<I, R, P, X>
            extends ParallelRunnables<I, R> {

        final PipelineStep<I, X> step;
        final Merger<P, X, R> merger;
        final PipelineExecutor executor;

        private RunnableGenerator(
                final PipelineStep<I, X> step, final Merger<P, X, R> merger, final PipelineExecutor executor) {
            this.step = step;
            this.merger = merger;
            this.executor = executor;
        }
    }

    private static final class InitRunnableGenerator<I, R, X>
            extends RunnableGenerator<I, R, I, X> {

        private InitRunnableGenerator(
                final PipelineStep<I, X> step, final Merger<I, X, R> merger, final PipelineExecutor executor) {
            super(step, merger, executor);
        }

        @Override
        final <F> Result<R, ?, ?> createResultAndRunnables(
                final PipelineEntry<I, F> entry, final List<Runnable> runnables,
                final ResultBarrier<F> collector) {
            final Result<R, I, X> result = new InputResult<>(entry, this.merger);
            runnables.add(new ParallelRunnable<>(this, entry, result, collector));
            return result;
        }
    }

    private static final class StepRunnableGenerator<I, R, P, X>
            extends RunnableGenerator<I, R, P, X> {

        private final ParallelRunnables<I, P> prev;

        private StepRunnableGenerator(
                final ParallelRunnables<I, P> prev, final PipelineStep<I, X> step, final Merger<P, X, R> merger,
                final PipelineExecutor executor) {
            super(step, merger, executor);
            this.prev = prev;
        }

        @Override
        final <F> Result<R, ?, ?> createResultAndRunnables(
                final PipelineEntry<I, F> entry, final List<Runnable> runnables,
                final ResultBarrier<F> collector) {
            final Result<R, P, X> result = new StepResult<>(
                    this.prev.createResultAndRunnables(entry, runnables, collector), this.merger);
            runnables.add(new ParallelRunnable<>(this, entry, result, collector));
            return result;
        }
    }

    private static final class ParallelRunnable<I, R, X>
            extends AbstractRunnable<I, X, R>
            implements Runnable {

        private final RunnableGenerator<I, ?, ?, X> parent;
        private final PipelineEntry<I, R> entry;
        private final Consumer<X> consumer;
        private final ResultBarrier<R> barrier;

        private ParallelRunnable(
                final RunnableGenerator<I, ?, ?, X> parent, final PipelineEntry<I, R> entry,
                final Consumer<X> consumer, final ResultBarrier<R> barrier) {
            super(entry.getRunId(), entry.get(), Finalizers.fromPipelineEntry(entry));
            this.parent = parent;
            this.entry = entry;
            this.consumer = consumer;
            this.barrier = barrier;
        }

        @Override
        public final void process(final PipelineEntry<I, X> entry) {
            this.parent.step.process(entry);
        }

        @Override
        protected final Runnable createNext(final X result) {
            this.consumer.accept(result);
            return this::collectResult;
        }

        @Override
        protected final Runnable createSame() {
            return new ParallelRunnable<>(this.parent, this.entry, this.consumer, this.barrier);
        }

        @Override
        protected final void enqueue(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            PipelineExecutor.executeOrSchedule(this.parent.executor, runnable, time, timeUnit);
        }

        private void collectResult() {
            try {
                if (this.barrier.isDone()) {
                    this.entry.accept(this.barrier.getResult());
                }
            } catch (final Throwable exception) {
                this.entry.stop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }
    }

    private static final class ResultBarrier<R> {

        private AtomicInteger counter;
        private Result<R, ?, ?> endResult;

        private void setup(final int count, final Result<R, ?, ?> endResult) {
            this.endResult = endResult;
            this.counter = new AtomicInteger(count);
        }

        final boolean isDone() {
            final int result = this.counter.decrementAndGet();
            return (result <= 0);
        }

        final R getResult() {
            return this.endResult.getResult();
        }
    }

    private abstract static class Result<R, P, X>
            implements Consumer<X> {

        final Merger<P, X, R> merger;
        X result;

        private Result(final Merger<P, X, R> merger) {
            this.merger = merger;
        }

        @Override
        public final void accept(final X result) {
            this.result = result;
        }

        abstract Value<R> getResultValue();

        final R getResult() {
            return this.getResultValue().get();
        }
    }

    private static final class InputResult<I, R, X>
            extends Result<R, I, X> {

        private final String runId;
        private final I input;

        private InputResult(final PipelineEntry<I, ?> entry, final Merger<I, X, R> merger) {
            super(merger);
            this.runId = entry.getRunId();
            this.input = entry.get();
        }

        @Override
        final Value<R> getResultValue() {
            final Value<I> v = Value.create(this.runId, this.input);
            final R r = this.merger.apply(v, this.result);
            return Value.create(v.getRunId(), r);
        }
    }

    private static final class StepResult<R, P, X>
            extends Result<R, P, X> {

        private final Result<P, ?, ?> prev;

        private StepResult(final Result<P, ?, ?> prev, final Merger<P, X, R> merger) {
            super(merger);
            this.prev = prev;
        }

        @Override
        final Value<R> getResultValue() {
            final Value<P> v = this.prev.getResultValue();
            final R r = this.merger.apply(v, this.result);
            return Value.create(v.getRunId(), r);
        }
    }
}
