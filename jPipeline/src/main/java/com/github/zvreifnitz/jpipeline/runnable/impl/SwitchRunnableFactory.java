package com.github.zvreifnitz.jpipeline.runnable.impl;

import com.github.zvreifnitz.jpipeline.pipeline.Finalizer;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

import java.util.List;
import java.util.function.ToIntFunction;

final class SwitchRunnableFactory<T, R> implements RunnableFactory<T, R> {

    private final ToIntFunction<T> switcher;
    private final List<RunnableFactory<T, R>> factories;

    SwitchRunnableFactory(final ToIntFunction<T> switcher, final List<RunnableFactory<T, R>> factories) {
        this.switcher = switcher;
        this.factories = factories;
    }

    @Override
    public final Runnable create(final String runId, final T result, final Finalizer<R> finalizer) {
        return this.factories.get(this.switcher.applyAsInt(result)).create(runId, result, finalizer);
    }
}
