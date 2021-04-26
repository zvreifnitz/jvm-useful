package com.github.zvreifnitz.jpipeline.execution;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public interface PipelineExecutor extends Executor, AutoCloseable {

    static void executeOrSchedule(
            final PipelineExecutor executor, final Runnable runnable, final long time, final TimeUnit timeUnit) {
        if (time > 0L) {
            executor.schedule(runnable, time, timeUnit);
        } else {
            executor.execute(runnable);
        }
    }

    static PipelineExecutor defaultExecutor() {
        return DefaultPipelineExecutor.get();
    }

    @Override
    void execute(final Runnable runnable);

    void execute(final List<? extends Runnable> runnables);

    void schedule(final Runnable runnable, final long time, final TimeUnit timeUnit);

    @Override
    void close();
}
