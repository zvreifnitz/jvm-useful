package com.github.zvreifnitz.jpipeline.builder.impl;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.*;
import com.github.zvreifnitz.jpipeline.builder.Merger;
import com.github.zvreifnitz.jpipeline.builder.ParallelBuilder;
import com.github.zvreifnitz.jpipeline.builder.SequentialBuilder;
import com.github.zvreifnitz.jpipeline.entry.EntryWrapper;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;
import com.github.zvreifnitz.jpipeline.runnable.impl.RunnableFactories;
import com.github.zvreifnitz.jpipeline.step.Steps;
import com.github.zvreifnitz.jpipeline.utils.Funcs;
import com.github.zvreifnitz.jpipeline.utils.Predicates;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

import static com.github.zvreifnitz.jcore.Preconditions.checkCollection;
import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

final class SequentialBuilders {

    private SequentialBuilders() {
    }

    static <T> AbstractBuilder.Sequential<T, T> step(final String pipelineName, final PipelineExecutor pipelineExecutor) {
        return new InputBuilder<>(pipelineName, pipelineExecutor);
    }

    static <T, P, R> AbstractBuilder.Sequential<T, R> step(
            final AbstractBuilder.Sequential<T, P> prev, final ParallelRunnables<P, R> tasksFactory) {
        return new ParStepBuilder<>(prev, tasksFactory);
    }

    static <T, P, R> AbstractBuilder.Sequential<T, R> step(
            final String name, final AbstractBuilder.Sequential<T, P> prev, final PipelineStep<P, R> step) {
        return new StepBuilder<>(name, prev, step);
    }

    static <T, P, R> AbstractBuilder.Sequential<T, R> step(
            final String name, final AbstractBuilder.Sequential<T, P> prev, final PipelineBuilder<P, R> builder) {
        return new SingleBuilderBuilder<>(name, prev, builder);
    }

    private static <T, P, R> AbstractBuilder.Sequential<T, R> step(
            final String name, final AbstractBuilder.Sequential<T, P> prev,
            final ToIntFunction<P> switcher, final List<PipelineBuilder<P, R>> builders) {
        return new SwitchBuilderBuilder<>(name, prev, switcher, builders);
    }

    private abstract static class AbstractSequentialBuilder<T, R>
            extends AbstractBuilder.Sequential<T, R> {

        private AbstractSequentialBuilder(
                final String pipelineName, final String stepName, final PipelineExecutor pipelineExecutor) {
            super(pipelineName, stepName, pipelineExecutor);
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildOrderedStep(
                final String name, final PipelineBuilder<R, N> builder) {
            checkNullPointer(name, "name");
            checkNullPointer(builder, "builder");
            return addOrderedStep(name, convertToStep(this.pipelineName, name, this.executor, builder));
        }

        @Override
        public final <N> SequentialBuilder<T, N> addOrderedStep(final String name, final PipelineStep<R, N> step) {
            checkNullPointer(name, "name");
            checkNullPointer(step, "step");
            return SequentialBuilders.step(name,
                    SequentialBuilders.step("barrier", this, new OrderBarrier<>()),
                    new Ordered<>(step));
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildStep(final String name, final PipelineBuilder<R, N> builder) {
            checkNullPointer(name, "name");
            checkNullPointer(builder, "builder");
            return SequentialBuilders.step(name, this, builder);
        }

        @Override
        public final SequentialBuilder<T, R> buildIfStep(
                final String name, final Predicate<R> predicate, final PipelineBuilder<R, R> trueBuilder) {
            return this.buildIfElseStep(name, predicate, trueBuilder, Funcs::identity);
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildIfElseStep(
                final String name, final Predicate<R> predicate,
                final PipelineBuilder<R, N> trueBuilder, final PipelineBuilder<R, N> falseBuilder) {
            checkNullPointer(trueBuilder, "trueBuilder");
            checkNullPointer(falseBuilder, "falseBuilder");
            final List<PipelineBuilder<R, N>> builders = new ArrayList<>(2);
            builders.add(trueBuilder);
            builders.add(falseBuilder);
            return this.buildSwitchStep(name, Predicates.switcher(predicate), builders);
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildSwitchStep(
                final String name, final ToIntFunction<R> switcher, final List<PipelineBuilder<R, N>> pipelineBuilders) {
            checkNullPointer(name, "name");
            checkNullPointer(switcher, "switcher");
            checkCollection(pipelineBuilders, "pipelineBuilders");
            return SequentialBuilders.step(name, this, switcher, pipelineBuilders);
        }

        @Override
        public final <N> SequentialBuilder<T, N> addStep(final String name, final PipelineStep<R, N> step) {
            checkNullPointer(name, "name");
            checkNullPointer(step, "step");
            return SequentialBuilders.step(name, this, step);
        }

        @Override
        public final SequentialBuilder<T, R> addIfStep(
                final String name, final Predicate<R> predicate, final PipelineStep<R, R> trueStep) {
            checkNullPointer(name, "name");
            return SequentialBuilders.step(name, this, Steps.ifStep(predicate, trueStep));
        }

        @Override
        public final <N> SequentialBuilder<T, N> addIfElseStep(
                final String name, final Predicate<R> predicate,
                final PipelineStep<R, N> trueStep, final PipelineStep<R, N> falseStep) {
            checkNullPointer(name, "name");
            return SequentialBuilders.step(name, this, Steps.ifElseStep(predicate, trueStep, falseStep));
        }

        @Override
        public final <N> SequentialBuilder<T, N> addSwitchStep(
                final String name, final ToIntFunction<R> indexSelector, final List<PipelineStep<R, N>> steps) {
            checkNullPointer(name, "name");
            return SequentialBuilders.step(name, this, Steps.switchStep(indexSelector, steps));
        }

        @Override
        public final <N, X> ParallelBuilder<T, R, N> addParallelStep(
                final String name, final PipelineStep<R, X> step, final BiFunction<R, X, N> merger) {
            checkNullPointer(name, "name");
            checkNullPointer(step, "step");
            checkNullPointer(merger, "merger");
            return ParallelBuilders.step(name, this, step, Merger.wrap(merger));
        }

        @Override
        public final <N, X> ParallelBuilder<T, R, N> buildParallelStep(
                final String name, final PipelineBuilder<R, X> builder, final BiFunction<R, X, N> merger) {
            checkNullPointer(name, "name");
            checkNullPointer(builder, "builder");
            checkNullPointer(merger, "merger");
            return ParallelBuilders.step(name, this, builder, Merger.wrap(merger));
        }

        @Override
        public final SequentialBuilder<T, R> peek(final Consumer<PipelineValue<R>> consumer) {
            return this.peek(Function.identity(), consumer);
        }
    }

    private static final class InputBuilder<T>
            extends AbstractSequentialBuilder<T, T> {

        private InputBuilder(final String pipelineName, final PipelineExecutor pipelineExecutor) {
            super(pipelineName, "Input", pipelineExecutor);
        }

        @SuppressWarnings("unchecked")
        @Override
        final <F> AbstractBuilder<T, F> materialize(final PipelineBuilder<T, F> builder) {
            return (AbstractBuilder<T, F>) builder.apply(this);
        }

        @Override
        final <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<T, F> next) {
            return next;
        }

        @Override
        public final <N> SequentialBuilder<T, T> peek(
                final Function<T, N> mapper, final Consumer<PipelineValue<N>> consumer) {
            checkNullPointer(mapper, "mapper");
            checkNullPointer(consumer, "consumer");
            return SequentialBuilders.step(this.stepName, this,
                    Steps.peekStep(this.pipelineName, this.stepName, mapper, consumer));
        }
    }

    private static final class StepBuilder<T, P, R>
            extends AbstractSequentialBuilder<T, R> {

        private final Sequential<T, P> prev;
        private final PipelineStep<P, R> step;

        private StepBuilder(final String name, final Sequential<T, P> prev, final PipelineStep<P, R> step) {
            super(prev.pipelineName, name, prev.executor);
            this.prev = prev;
            this.step = step;
        }

        @Override
        final <F> AbstractBuilder<T, F> materialize(final PipelineBuilder<T, F> builder) {
            throw new IllegalStateException();
        }

        @Override
        final <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<R, F> next) {
            return this.prev.buildRunnableFactory(RunnableFactories.create(executor, this.step, next));
        }

        @Override
        public final <N> SequentialBuilder<T, R> peek(
                final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer) {
            checkNullPointer(mapper, "mapper");
            checkNullPointer(consumer, "consumer");
            return SequentialBuilders.step(
                    this.stepName, this.prev,
                    Steps.peekStep(this.pipelineName, this.stepName, this.step, mapper, consumer));
        }
    }

    private static final class ParStepBuilder<T, P, R>
            extends AbstractSequentialBuilder<T, R> {

        private final Sequential<T, P> prev;
        private final ParallelRunnables<P, R> tasksFactory;

        private ParStepBuilder(final Sequential<T, P> prev, final ParallelRunnables<P, R> tasksFactory) {
            super(prev.pipelineName, "", prev.executor);
            this.prev = prev;
            this.tasksFactory = tasksFactory;
        }

        @Override
        final <F> AbstractBuilder<T, F> materialize(final PipelineBuilder<T, F> builder) {
            throw new IllegalStateException();
        }

        @Override
        final <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<R, F> next) {
            return this.prev.buildRunnableFactory(
                    RunnableFactories.create(this.executor, this.tasksFactory, next));
        }

        @Override
        public final <N> SequentialBuilder<T, R> peek(
                final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer) {
            return this.addStep("join_peek", BuilderHelper.step()).peek(mapper, consumer);
        }
    }

    private abstract static class BuilderBuilder<T, R>
            extends AbstractSequentialBuilder<T, R> {

        private BuilderBuilder(
                final String pipelineName, final String stepName, final PipelineExecutor pipelineExecutor) {
            super(pipelineName, stepName, pipelineExecutor);
        }

        @Override
        final <F> AbstractBuilder<T, F> materialize(final PipelineBuilder<T, F> builder) {
            throw new IllegalStateException();
        }

        @Override
        public final <N> SequentialBuilder<T, R> peek(
                final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer) {
            checkNullPointer(mapper, "mapper");
            checkNullPointer(consumer, "consumer");
            return SequentialBuilders.step(this.stepName, this,
                    Steps.peekStep(this.pipelineName, this.stepName, mapper, consumer));
        }
    }

    private static final class SingleBuilderBuilder<T, P, R>
            extends BuilderBuilder<T, R> {

        private final Sequential<T, P> prev;
        private final PipelineBuilder<P, R> builder;

        private SingleBuilderBuilder(
                final String name, final Sequential<T, P> prev, final PipelineBuilder<P, R> builder) {
            super(prev.pipelineName, name, prev.executor);
            this.prev = prev;
            this.builder = builder;
        }

        @Override
        final <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<R, F> next) {
            final String childPipelineName = AbstractBuilder.pipelineName(this.pipelineName, this.stepName);
            final RunnableFactory<P, F> factory = this.buildRunnableFactory(childPipelineName, this.builder, next);
            return this.prev.buildRunnableFactory(factory);
        }
    }

    private static final class SwitchBuilderBuilder<T, P, R>
            extends BuilderBuilder<T, R> {

        private final Sequential<T, P> prev;
        private final ToIntFunction<P> switcher;
        private final List<PipelineBuilder<P, R>> builders;

        private SwitchBuilderBuilder(
                final String name, final Sequential<T, P> prev,
                final ToIntFunction<P> switcher, final List<PipelineBuilder<P, R>> builders) {
            super(prev.pipelineName, name, prev.executor);
            this.prev = prev;
            this.switcher = switcher;
            this.builders = builders;
        }

        @Override
        final <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<R, F> next) {
            final String childPipelineName = AbstractBuilder.pipelineName(this.pipelineName, this.stepName);
            final List<RunnableFactory<P, F>> factories = new ArrayList<>(this.builders.size());
            for (final PipelineBuilder<P, R> builder : this.builders) {
                factories.add(this.buildRunnableFactory(childPipelineName, builder, next));
            }
            final RunnableFactory<P, F> factory = RunnableFactories.create(this.switcher, factories);
            return this.prev.buildRunnableFactory(factory);
        }
    }

    private static final class BarrierRelease<T> implements Runnable {

        private final T value;
        private final Runnable runnable;

        private BarrierRelease(final T value, final Runnable runnable) {
            this.value = value;
            this.runnable = runnable;
        }

        public final T getValue() {
            return value;
        }

        @Override
        public final void run() {
            try {
                this.runnable.run();
            } catch (final Exception ignored) {
            }
        }
    }

    private static final class OrderBarrier<T> implements PipelineStep<T, BarrierRelease<T>>, Runnable {

        private final AtomicBoolean taken = new AtomicBoolean(false);
        private final ConcurrentLinkedQueue<PipelineEntry<T, BarrierRelease<T>>> queue = new ConcurrentLinkedQueue<>();

        @Override
        public final void run() {
            this.taken.set(false);
            this.execute();
        }

        private void execute() {
            final PipelineEntry<T, BarrierRelease<T>> entry = this.getNextEntry();
            if (entry == null) {
                return;
            }
            this.execute(entry);
        }

        private void execute(final PipelineEntry<T, BarrierRelease<T>> entry) {
            try {
                entry.accept(new BarrierRelease<>(entry.get(), this));
            } catch (final Throwable exception) {
                this.run();
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }

        @Override
        public final void process(final PipelineEntry<T, BarrierRelease<T>> entry) {
            try {
                this.queue.add(entry);
            } catch (final Throwable exception) {
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
            this.execute();
        }

        private PipelineEntry<T, BarrierRelease<T>> getNextEntry() {
            final PipelineEntry<T, BarrierRelease<T>> e = this.queue.peek();
            if ((e == null) || this.taken.get()
                    || !this.taken.compareAndSet(false, true)) {
                return null;
            }
            final PipelineEntry<T, BarrierRelease<T>> r = this.queue.poll();
            if (r == null) {
                this.taken.set(false);
                return null;
            }
            return r;
        }
    }

    private static final class Ordered<T, R> implements PipelineStep<BarrierRelease<T>, R> {

        private final PipelineStep<T, R> step;

        private Ordered(final PipelineStep<T, R> step) {
            this.step = step;
        }

        @Override
        public final void process(final PipelineEntry<BarrierRelease<T>, R> entry) {
            final BarrierRelease<T> barrier = entry.get();
            try {
                this.step.process(new Entry(entry));
            } catch (final Throwable exception) {
                barrier.run();
                entry.tryStop(exception);
                Exceptions.rethrowIfError(exception);
            }
        }

        private final class Entry extends EntryWrapper<T, BarrierRelease<T>, R, R> {

            private final Runnable runnable;

            private Entry(final PipelineEntry<BarrierRelease<T>, R> entry) {
                super(entry, BarrierRelease::getValue, Funcs::passThrough);
                this.runnable = entry.get();
            }

            @Override
            public final boolean tryAccept(final R result, final long time, final TimeUnit timeUnit) {
                if (super.tryAccept(result, time, timeUnit)) {
                    this.runnable.run();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public final boolean tryStop(final Throwable throwable) {
                if (super.tryStop(throwable)) {
                    this.runnable.run();
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
