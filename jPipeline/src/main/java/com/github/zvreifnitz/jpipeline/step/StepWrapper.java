package com.github.zvreifnitz.jpipeline.step;

import com.github.zvreifnitz.jcore.exc.Exceptions;
import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.PipelineStep;

import java.util.function.Function;

public abstract class StepWrapper<T, X, Y, R> implements PipelineStep<T, R> {

    protected final PipelineStep<X, Y> step;
    protected final Function<PipelineEntry<T, R>, PipelineEntry<X, Y>> converter;

    protected StepWrapper(
            final PipelineStep<X, Y> step,
            final Function<PipelineEntry<T, R>, PipelineEntry<X, Y>> converter) {
        this.step = step;
        this.converter = converter;
    }

    @Override
    public void process(final PipelineEntry<T, R> entry) {
        try {
            final PipelineEntry<X, Y> e = this.converter.apply(entry);
            this.step.process(e);
        } catch (final Throwable exception) {
            entry.tryStop(exception);
            Exceptions.rethrowIfError(exception);
        }
    }
}
