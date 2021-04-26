package com.github.zvreifnitz.jpipeline.step;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.builder.Merger;
import com.github.zvreifnitz.jpipeline.entry.Entries;
import com.github.zvreifnitz.jpipeline.entry.EntryWrapper;
import com.github.zvreifnitz.jpipeline.pipeline.Finalizers;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;
import com.github.zvreifnitz.jpipeline.utils.Funcs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static com.github.zvreifnitz.jcore.Preconditions.checkCollection;
import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

public final class Steps {

    private Steps() {
    }

    public static <T> PipelineStep<T, T> identity() {
        return functionToStep(Funcs::identity);
    }

    public static <T, R> PipelineStep<T, R> step(final RunnableFactory<T, R> factory) {
        checkNullPointer(factory, "factory");
        return new RunnableFactoryStep<>(factory);
    }

    public static <T, R> PipelineStep<T, R> functionToStep(final Function<T, R> func) {
        checkNullPointer(func, "func");
        return new FunctionStep<>(func);
    }

    public static <T, R> PipelineStep<T, R> adapter(final PipelineStep<T, R> step) {
        checkNullPointer(step, "step");
        return step;
    }

    public static <T, X, R> PipelineStep<T, R> adapter(
            final PipelineStep<T, X> step, final BiFunction<T, X, R> merger) {
        return adapter(Funcs.identityFunc(), step, merger);
    }

    public static <T, X, R> PipelineStep<T, R> adapter(
            final Function<T, X> extractor, final PipelineStep<X, R> step) {
        return adapter(extractor, step, Funcs.passThroughMerger());
    }

    public static <T, X, Y, R> PipelineStep<T, R> adapter(
            final Function<T, X> extractor, final PipelineStep<X, Y> step, final BiFunction<T, Y, R> merger) {
        checkNullPointer(extractor, "extractor");
        checkNullPointer(step, "step");
        checkNullPointer(merger, "merger");
        return new AdapterStep<>(extractor, step, merger);
    }

    public static <T, Y, R> PipelineStep<T, R> adapter(
            final PipelineStep<T, Y> step, final Merger<T, Y, R> merger) {
        return adapter(Funcs.identityFunc(), step, merger);
    }

    public static <T, X, Y, R> PipelineStep<T, R> adapter(
            final Function<T, X> extractor, final PipelineStep<X, Y> step, final Merger<T, Y, R> merger) {
        checkNullPointer(extractor, "extractor");
        checkNullPointer(step, "step");
        checkNullPointer(merger, "merger");
        return new AdapterStep<>(extractor, step, merger);
    }

    public static <T> PipelineStep<T, T> ifStep(
            final Predicate<T> predicate, final PipelineStep<T, T> trueStep) {
        return ifElseStep(predicate, trueStep, identity());
    }

    public static <T, R> PipelineStep<T, R> ifElseStep(
            final Predicate<T> predicate, final PipelineStep<T, R> trueStep, final PipelineStep<T, R> falseStep) {
        checkNullPointer(predicate, "predicate");
        checkNullPointer(trueStep, "trueStep");
        checkNullPointer(falseStep, "falseStep");
        return new IfElseStep<>(predicate, trueStep, falseStep);
    }

    public static <T, R> PipelineStep<T, R> switchStep(
            final ToIntFunction<T> indexSelector, final List<PipelineStep<T, R>> steps) {
        checkNullPointer(indexSelector, "indexSelector");
        checkCollection(steps, "steps");
        return new SwitchStep<>(indexSelector, steps);
    }

    public static <T, X> PipelineStep<T, T> peekStep(
            final String pipelineName, final String stepName,
            final Function<T, X> mapper, final Consumer<PipelineValue<X>> consumer) {
        return peekStep(pipelineName, stepName, identity(), mapper, consumer);
    }

    public static <T, X, R> PipelineStep<T, R> peekStep(
            final String pipelineName, final String stepName, final PipelineStep<T, R> step,
            final Function<R, X> mapper, final Consumer<PipelineValue<X>> consumer) {
        checkNullPointer(pipelineName, "pipelineName");
        checkNullPointer(stepName, "stepName");
        checkNullPointer(step, "step");
        checkNullPointer(mapper, "mapper");
        checkNullPointer(consumer, "consumer");
        return new PeekStep<>(pipelineName, stepName, step, mapper, consumer);
    }

    public static <T, R> PipelineStep<T, R> retry(
            final PipelineStep<T, R> step, final LongUnaryOperator sleeper) {
        checkNullPointer(step, "step");
        checkNullPointer(sleeper, "sleeper");
        return new RetryStep<>(step, sleeper);
    }

    private static final class FunctionStep<T, R> implements PipelineStep<T, R> {

        private final Function<T, R> func;

        private FunctionStep(final Function<T, R> func) {
            this.func = func;
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            try {
                final T input = entry.get();
                final R result = this.func.apply(input);
                entry.accept(result);
            } catch (final Throwable exception) {
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }
    }

    private static final class AdapterStep<T, X, Y, R> extends StepWrapper<T, X, Y, R> {

        private AdapterStep(
                final Function<T, X> extractor, final PipelineStep<X, Y> step, final BiFunction<T, Y, R> merger) {
            super(step, e -> Entries.adapter(e, extractor, merger));
        }

        private AdapterStep(
                final Function<T, X> extractor, final PipelineStep<X, Y> step, final Merger<T, Y, R> merger) {
            super(step, e -> Entries.adapter(e, extractor, merger));
        }
    }

    private static final class IfElseStep<T, R> implements PipelineStep<T, R> {

        private final Predicate<T> predicate;
        private final PipelineStep<T, R> trueStep;
        private final PipelineStep<T, R> falseStep;

        private IfElseStep(
                final Predicate<T> predicate, final PipelineStep<T, R> trueStep, final PipelineStep<T, R> falseStep) {
            this.predicate = predicate;
            this.trueStep = trueStep;
            this.falseStep = falseStep;
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            try {
                if (this.predicate.test(entry.get())) {
                    this.trueStep.process(entry);
                } else {
                    this.falseStep.process(entry);
                }
            } catch (final Throwable exception) {
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }
    }

    private static final class SwitchStep<T, R> implements PipelineStep<T, R> {

        private final ToIntFunction<T> indexSelector;
        private final List<PipelineStep<T, R>> steps;

        private SwitchStep(final ToIntFunction<T> indexSelector, final List<PipelineStep<T, R>> steps) {
            this.indexSelector = indexSelector;
            this.steps = steps;
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            try {
                this.steps.get(this.indexSelector.applyAsInt(entry.get())).process(entry);
            } catch (final Throwable exception) {
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }
    }

    private static final class PeekStep<T, X, R> extends StepWrapper<T, T, R, R> {

        private PeekStep(
                final String pipelineName, final String stepName, final PipelineStep<T, R> step,
                final Function<R, X> mapper, final Consumer<PipelineValue<X>> consumer) {
            super(step, e -> Entries.peek(e, pipelineName, stepName, mapper, consumer));
        }
    }

    private static final class RunnableFactoryStep<T, R> implements PipelineStep<T, R> {

        private final RunnableFactory<T, R> factory;

        private RunnableFactoryStep(final RunnableFactory<T, R> factory) {
            this.factory = factory;
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            try {
                final Runnable runnable = this.factory.create(
                        entry.getRunId(), entry.get(), Finalizers.fromPipelineEntry(entry));
                runnable.run();
            } catch (final Throwable exception) {
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }
    }

    private static final class RetryStep<T, R> implements PipelineStep<T, R> {

        private final PipelineStep<T, R> step;
        private final LongUnaryOperator sleeper;
        private final Map<String, Long> counts;

        private RetryStep(final PipelineStep<T, R> step, final LongUnaryOperator sleeper) {
            this.step = step;
            this.sleeper = sleeper;
            this.counts = new ConcurrentHashMap<>();
        }

        @Override
        public final void process(final PipelineEntry<T, R> entry) {
            try {
                this.step.process(new Entry(entry));
            } catch (final Throwable exception) {
                this.retry(entry, exception);
            }
        }

        private boolean retry(final PipelineEntry<T, R> entry, final Throwable throwable) {
            try {
                final String runId = entry.getRunId();
                final long sleep = this.calcSleep(this.getCount(runId));
                if (sleep < 0) {
                    this.remove(runId);
                    return entry.tryStop(throwable);
                } else {
                    return entry.tryRetry(sleep, TimeUnit.MILLISECONDS);
                }
            } catch (final Throwable exception) {
                this.remove(entry.getRunId());
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
                return false;
            }
        }

        private void remove(final String runId) {
            this.counts.remove(runId);
        }

        private long calcSleep(final long count) {
            return this.sleeper.applyAsLong(count);
        }

        private long getCount(final String runId) {
            return this.counts.merge(runId, 1L, Long::sum);
        }

        private final class Entry extends EntryWrapper<T, T, R, R> {

            private Entry(final PipelineEntry<T, R> entry) {
                super(entry, Funcs::identity, Funcs::passThrough);
            }

            @Override
            public final boolean tryAccept(final R result, final long time, final TimeUnit timeUnit) {
                if (super.tryAccept(result, time, timeUnit)) {
                    RetryStep.this.remove(this.getRunId());
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public final boolean tryStop(final Throwable throwable) {
                return RetryStep.this.retry(this.entry, throwable);
            }
        }
    }
}
