package com.github.zvreifnitz.jpipeline.builder;

import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.PipelineStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public interface CommonBuilder<T, R> {

    default <N> SequentialBuilder<T, N> buildStep(final PipelineBuilder<R, N> builder) {
        return this.buildStep("", builder);
    }

    <N> SequentialBuilder<T, N> buildStep(final String name, final PipelineBuilder<R, N> builder);

    default <N> SequentialBuilder<T, N> addStep(final PipelineStep<R, N> step) {
        return this.addStep("", step);
    }

    <N> SequentialBuilder<T, N> addStep(
            final String name, final PipelineStep<R, N> step);

    default SequentialBuilder<T, R> buildIfStep(
            final Predicate<R> predicate, final PipelineBuilder<R, R> trueBuilder) {
        return this.buildIfStep("", predicate, trueBuilder);
    }

    SequentialBuilder<T, R> buildIfStep(
            final String name, final Predicate<R> predicate, final PipelineBuilder<R, R> trueBuilder);

    default SequentialBuilder<T, R> addIfStep(
            final Predicate<R> predicate, final PipelineStep<R, R> trueStep) {
        return this.addIfStep("", predicate, trueStep);
    }

    SequentialBuilder<T, R> addIfStep(
            final String name, final Predicate<R> predicate, final PipelineStep<R, R> trueStep);

    default <N> SequentialBuilder<T, N> buildIfElseStep(
            final Predicate<R> predicate,
            final PipelineBuilder<R, N> trueBuilder, final PipelineBuilder<R, N> falseBuilder) {
        return this.buildIfElseStep("", predicate, trueBuilder, falseBuilder);
    }

    <N> SequentialBuilder<T, N> buildIfElseStep(
            final String name, final Predicate<R> predicate,
            final PipelineBuilder<R, N> trueBuilder, final PipelineBuilder<R, N> falseBuilder);

    default <N> SequentialBuilder<T, N> addIfElseStep(
            final Predicate<R> predicate, final PipelineStep<R, N> trueStep, final PipelineStep<R, N> falseStep) {
        return this.addIfElseStep("", predicate, trueStep, falseStep);
    }

    <N> SequentialBuilder<T, N> addIfElseStep(
            final String name, final Predicate<R> predicate,
            final PipelineStep<R, N> trueStep, final PipelineStep<R, N> falseStep);

    @SuppressWarnings("unchecked")
    default <N> SequentialBuilder<T, N> buildSwitchStep(
            final ToIntFunction<R> switcher,
            final PipelineBuilder<R, N> builder0, final PipelineBuilder<R, N>... buildersN) {
        final List<PipelineBuilder<R, N>> steps = new ArrayList<>();
        steps.add(builder0);
        steps.addAll(Arrays.asList(buildersN));
        return this.buildSwitchStep("", switcher, steps);
    }

    @SuppressWarnings("unchecked")
    default <N> SequentialBuilder<T, N> buildSwitchStep(
            final String name, final ToIntFunction<R> switcher,
            final PipelineBuilder<R, N> builder0, final PipelineBuilder<R, N>... buildersN) {
        final List<PipelineBuilder<R, N>> steps = new ArrayList<>();
        steps.add(builder0);
        steps.addAll(Arrays.asList(buildersN));
        return this.buildSwitchStep(name, switcher, steps);
    }

    @SuppressWarnings("unchecked")
    default <N> SequentialBuilder<T, N> addSwitchStep(
            final ToIntFunction<R> switcher, final PipelineStep<R, N> step0, final PipelineStep<R, N>... stepsN) {
        return this.addSwitchStep("", switcher, step0, stepsN);
    }

    @SuppressWarnings("unchecked")
    default <N> SequentialBuilder<T, N> addSwitchStep(
            final String name, final ToIntFunction<R> switcher,
            final PipelineStep<R, N> step0, final PipelineStep<R, N>... stepsN) {
        final List<PipelineStep<R, N>> steps = new ArrayList<>();
        steps.add(step0);
        steps.addAll(Arrays.asList(stepsN));
        return this.addSwitchStep(name, switcher, steps);
    }

    default <N> SequentialBuilder<T, N> buildSwitchStep(
            final ToIntFunction<R> switcher, final List<PipelineBuilder<R, N>> builders) {
        return this.buildSwitchStep("", switcher, builders);
    }

    <N> SequentialBuilder<T, N> buildSwitchStep(
            final String name, final ToIntFunction<R> switcher, final List<PipelineBuilder<R, N>> builders);

    default <N> SequentialBuilder<T, N> addSwitchStep(
            final ToIntFunction<R> switcher, final List<PipelineStep<R, N>> steps) {
        return this.addSwitchStep("", switcher, steps);
    }

    <N> SequentialBuilder<T, N> addSwitchStep(
            final String name, final ToIntFunction<R> switcher, final List<PipelineStep<R, N>> steps);

    default <N> SequentialBuilder<T, N> buildOrderedStep(final PipelineBuilder<R, N> builder) {
        return this.buildOrderedStep("", builder);
    }

    <N> SequentialBuilder<T, N> buildOrderedStep(final String name, final PipelineBuilder<R, N> builder);

    default <N> SequentialBuilder<T, N> addOrderedStep(final PipelineStep<R, N> step) {
        return this.addOrderedStep("", step);
    }

    <N> SequentialBuilder<T, N> addOrderedStep(
            final String name, final PipelineStep<R, N> step);
}
