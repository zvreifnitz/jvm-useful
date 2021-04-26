package com.github.zvreifnitz.jpipeline.runnable;

import com.github.zvreifnitz.jpipeline.pipeline.Finalizer;

public interface RunnableFactory<T, R> {
    Runnable create(final String runId, final T input, final Finalizer<R> finalizer);
}
