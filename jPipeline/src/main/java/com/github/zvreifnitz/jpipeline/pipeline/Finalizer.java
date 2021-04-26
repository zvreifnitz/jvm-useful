package com.github.zvreifnitz.jpipeline.pipeline;

import java.util.concurrent.Future;

public interface Finalizer<R> {
    void setResult(final R result);

    void setError(final Throwable throwable);

    interface FinalizerFuture<Q> extends Finalizer<Q>, Future<Q> {
    }
}
