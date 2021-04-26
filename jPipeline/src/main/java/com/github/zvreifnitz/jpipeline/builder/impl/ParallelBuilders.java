package com.github.zvreifnitz.jpipeline.builder.impl;

import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.PipelineStep;
import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.builder.Merger;
import com.github.zvreifnitz.jpipeline.builder.ParallelBuilder;
import com.github.zvreifnitz.jpipeline.builder.SequentialBuilder;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;
import com.github.zvreifnitz.jpipeline.step.Steps;

import java.util.List;
import java.util.function.*;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;
import static com.github.zvreifnitz.jpipeline.builder.impl.AbstractBuilder.convertToStep;

final class ParallelBuilders {

    private ParallelBuilders() {
    }

    static <T, I, R, X> AbstractBuilder.Parallel<T, I, R> step(
            final String name, final AbstractBuilder.Sequential<T, I> prev,
            final PipelineStep<I, X> step, final Merger<I, X, R> merger) {
        return new InitParStepBuilder<>(name, prev, step, merger);
    }

    static <T, I, R, X> AbstractBuilder.Parallel<T, I, R> step(
            final String name, final AbstractBuilder.Sequential<T, I> prev,
            final PipelineBuilder<I, X> builder, final Merger<I, X, R> merger) {
        return new InitParStepBuilder<>(name, prev, convertToStep(name, prev, builder), merger);
    }

    private static <T, I, P, R, X> AbstractBuilder.Parallel<T, I, R> step(
            final String name, final AbstractParallelBuilder<T, I, P> prev,
            final PipelineStep<I, X> step, final Merger<P, X, R> merger) {
        return new ParStepBuilder<>(name, prev, step, merger);
    }

    private static <T, I, P, R, X> AbstractBuilder.Parallel<T, I, R> step(
            final String name, final AbstractParallelBuilder<T, I, P> prev,
            final PipelineBuilder<I, X> builder, final Merger<P, X, R> merger) {
        return new ParStepBuilder<>(name, prev, convertToStep(name, prev, builder), merger);
    }

    private abstract static class AbstractParallelBuilder<T, I, R>
            extends AbstractBuilder.Parallel<T, I, R> {

        private final boolean init;

        private AbstractParallelBuilder(
                final String pipelineName, final String stepName, final boolean init, final PipelineExecutor executor) {
            super(pipelineName, stepName, executor);
            this.init = init;
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildStep(final String name, final PipelineBuilder<R, N> builder) {
            return this.toSeq().buildStep(name, builder);
        }

        @Override
        public final SequentialBuilder<T, R> buildIfStep(
                final String name, final Predicate<R> predicate, final PipelineBuilder<R, R> trueBuilder) {
            return this.toSeq().buildIfStep(name, predicate, trueBuilder);
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildIfElseStep(
                final String name, final Predicate<R> predicate,
                final PipelineBuilder<R, N> trueBuilder, final PipelineBuilder<R, N> falseBuilder) {
            return this.toSeq().buildIfElseStep(name, predicate, trueBuilder, falseBuilder);
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildSwitchStep(
                final String name, final ToIntFunction<R> switcher, final List<PipelineBuilder<R, N>> pipelineBuilders) {
            return this.toSeq().buildSwitchStep(name, switcher, pipelineBuilders);
        }

        @Override
        public final <N> SequentialBuilder<T, N> addStep(final String name, final PipelineStep<R, N> step) {
            return this.toSeq().addStep(name, step);
        }

        @Override
        public final SequentialBuilder<T, R> addIfStep(
                final String name, final Predicate<R> predicate, final PipelineStep<R, R> step) {
            return this.toSeq().addIfStep(name, predicate, step);
        }

        @Override
        public final <N> SequentialBuilder<T, N> addIfElseStep(
                final String name, final Predicate<R> predicate,
                final PipelineStep<R, N> trueStep, final PipelineStep<R, N> falseStep) {
            return this.toSeq().addIfElseStep(name, predicate, trueStep, falseStep);
        }

        @Override
        public final <N> SequentialBuilder<T, N> addSwitchStep(
                final String name, final ToIntFunction<R> indexSelector, final List<PipelineStep<R, N>> steps) {
            return this.toSeq().addSwitchStep(name, indexSelector, steps);
        }

        @Override
        public final <N, X> ParallelBuilder<T, I, N> addParallelStep(
                final String name, final PipelineStep<I, X> step, final BiFunction<R, X, N> merger) {
            checkNullPointer(name, "name");
            checkNullPointer(step, "step");
            checkNullPointer(merger, "merger");
            return ParallelBuilders.step(name, this, step, Merger.wrap(merger));
        }

        @Override
        public final <N, X> ParallelBuilder<T, I, N> buildParallelStep(
                final String name, final PipelineBuilder<I, X> builder, final BiFunction<R, X, N> merger) {
            checkNullPointer(name, "name");
            checkNullPointer(builder, "builder");
            checkNullPointer(merger, "merger");
            return ParallelBuilders.step(name, this, builder, Merger.wrap(merger));
        }

        @Override
        public final <N> SequentialBuilder<T, N> buildOrderedStep(
                final String name, final PipelineBuilder<R, N> builder) {
            return this.toSeq().buildOrderedStep(name, builder);
        }

        @Override
        public final <N> SequentialBuilder<T, N> addOrderedStep(final String name, final PipelineStep<R, N> step) {
            return this.toSeq().addOrderedStep(name, step);
        }

        @Override
        public final SequentialBuilder<T, R> join() {
            return this.toSeq();
        }

        @Override
        final <F> RunnableFactory<T, F> buildRunnableFactory(final RunnableFactory<R, F> next) {
            return this.toSeq().buildRunnableFactory(next);
        }

        @Override
        final <F> AbstractBuilder<T, F> materialize(final PipelineBuilder<T, F> builder) {
            throw new IllegalStateException();
        }

        private Sequential<T, R> toSeq() {
            return this.init
                    ? SequentialBuilders.step(this.stepName, this.getPrevSeq(), this.getStep())
                    : SequentialBuilders.step(this.getPrevSeq(), this.createParallelRunnables());
        }

        abstract PipelineStep<I, R> getStep();

        abstract Sequential<T, I> getPrevSeq();

        abstract ParallelRunnables<I, R> createParallelRunnables();
    }

    private static final class InitParStepBuilder<T, I, R, X>
            extends AbstractParallelBuilder<T, I, R> {

        private final Sequential<T, I> prev;
        private final PipelineStep<I, X> step;
        private final Merger<I, X, R> merger;

        private InitParStepBuilder(
                final String name, final Sequential<T, I> prev,
                final PipelineStep<I, X> step, final Merger<I, X, R> merger) {
            super(prev.pipelineName, name, true, prev.executor);
            this.prev = prev;
            this.step = step;
            this.merger = merger;
        }

        @Override
        public final <N> ParallelBuilder<T, I, R> peek(final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer) {
            checkNullPointer(mapper, "mapper");
            checkNullPointer(consumer, "consumer");
            return ParallelBuilders.step(this.stepName, this.prev, this.step,
                    Merger.wrap(this.pipelineName, this.stepName, this.merger, mapper, consumer));
        }

        @Override
        final PipelineStep<I, R> getStep() {
            return Steps.adapter(this.step, this.merger);
        }

        @Override
        final Sequential<T, I> getPrevSeq() {
            return this.prev;
        }

        @Override
        final ParallelRunnables<I, R> createParallelRunnables() {
            return ParallelRunnables.create(this.step, this.merger, this.executor);
        }
    }

    private static final class ParStepBuilder<T, I, P, R, X>
            extends AbstractParallelBuilder<T, I, R> {

        private final AbstractParallelBuilder<T, I, P> prev;
        private final PipelineStep<I, X> step;
        private final Merger<P, X, R> merger;

        private ParStepBuilder(
                final String name, final AbstractParallelBuilder<T, I, P> prev,
                final PipelineStep<I, X> step, final Merger<P, X, R> merger) {
            super(prev.pipelineName, name, false, prev.executor);
            this.prev = prev;
            this.step = step;
            this.merger = merger;
        }

        @Override
        public final <N> ParallelBuilder<T, I, R> peek(final Function<R, N> mapper, final Consumer<PipelineValue<N>> consumer) {
            checkNullPointer(mapper, "mapper");
            checkNullPointer(consumer, "consumer");
            return ParallelBuilders.step(this.stepName, this.prev, this.step,
                    Merger.wrap(this.pipelineName, this.stepName, this.merger, mapper, consumer));
        }

        @Override
        final PipelineStep<I, R> getStep() {
            throw new IllegalStateException();
        }

        @Override
        final Sequential<T, I> getPrevSeq() {
            return this.prev.getPrevSeq();
        }

        @Override
        ParallelRunnables<I, R> createParallelRunnables() {
            return ParallelRunnables.create(
                    this.prev.createParallelRunnables(), this.step, this.merger, this.executor);
        }
    }
}
