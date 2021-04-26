package com.github.zvreifnitz.jpipeline;

import com.github.zvreifnitz.jpipeline.entry.Value;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PipelineEntry<T, R> extends Consumer<R>, Value<T>, Supplier<T> {

    @Override
    String getRunId();

    @Override
    T get();

    @Override
    void accept(final R result);

    void accept(final R result, final long time, final TimeUnit timeUnit);

    void stop(final Throwable throwable);

    void retry();

    void retry(final long time, final TimeUnit timeUnit);

    boolean tryAccept(final R result);

    boolean tryAccept(final R result, final long time, final TimeUnit timeUnit);

    boolean tryStop(final Throwable throwable);

    boolean tryRetry();

    boolean tryRetry(final long time, final TimeUnit timeUnit);
}
