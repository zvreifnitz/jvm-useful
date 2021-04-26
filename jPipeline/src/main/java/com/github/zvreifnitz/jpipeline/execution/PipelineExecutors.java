package com.github.zvreifnitz.jpipeline.execution;

import com.github.zvreifnitz.jcore.Preconditions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.github.zvreifnitz.jcore.Preconditions.*;

public final class PipelineExecutors {

    private PipelineExecutors() {
    }

    public static PipelineExecutor fromExecutor(final Executor executor) {
        checkNullPointer(executor, "executor");
        if (executor instanceof PipelineExecutor) {
            return new WrappedPipelineExecutor((PipelineExecutor) executor);
        }
        if (executor instanceof ScheduledExecutorService) {
            return new WrappedScheduledExecutorService((ScheduledExecutorService) executor);
        }
        return new WrappedExecutor(executor);
    }

    private abstract static class WrappedCloseable
            implements AutoCloseable {

        private volatile boolean closed = false;

        @Override
        public void close() {
            this.closed = true;
        }

        protected void checkOpen() {
            Preconditions.checkState(!this.closed, "Pipeline is closed");
        }
    }

    private static final class WrappedPipelineExecutor
            extends WrappedCloseable
            implements PipelineExecutor {

        private final PipelineExecutor executor;

        private WrappedPipelineExecutor(final PipelineExecutor executor) {
            this.executor = executor;
        }

        @Override
        public final void execute(final Runnable runnable) {
            this.checkOpen();
            this.executor.execute(runnable);
        }

        @Override
        public final void execute(final List<? extends Runnable> runnables) {
            this.checkOpen();
            this.executor.execute(runnables);
        }

        @Override
        public final void schedule(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            this.checkOpen();
            this.executor.schedule(runnable, time, timeUnit);
        }
    }

    private static final class WrappedExecutor
            extends WrappedCloseable
            implements PipelineExecutor {

        private final Executor executor;
        private final Timer timer;

        private WrappedExecutor(final Executor executor) {
            this.executor = executor;
            this.timer = new Timer(true);
        }

        @Override
        public final void execute(final Runnable runnable) {
            checkNullPointer(runnable, "runnable");
            this.checkOpen();
            this.executor.execute(runnable);
        }

        @Override
        public final void execute(final List<? extends Runnable> runnables) {
            checkCollection(runnables, "runnables");
            this.checkOpen();
            for (final Runnable runnable : runnables) {
                this.executor.execute(runnable);
            }
        }

        @Override
        public final void schedule(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            checkNullPointer(runnable, "runnable");
            checkNullPointer(timeUnit, "timeUnit");
            checkArgument(time > 0, "time", "Value must be positive");
            this.checkOpen();
            this.timer.schedule(toTask(runnable), timeUnit.toMillis(time));
        }

        @Override
        public final void close() {
            super.close();
            this.timer.cancel();
        }

        private TimerTask toTask(final Runnable runnable) {
            return new DelayTask(this.executor, runnable);
        }
    }

    private static final class WrappedScheduledExecutorService
            extends WrappedCloseable
            implements PipelineExecutor {

        private final ScheduledExecutorService scheduledExecutorService;

        private WrappedScheduledExecutorService(final ScheduledExecutorService scheduledExecutorService) {
            this.scheduledExecutorService = scheduledExecutorService;
        }

        @Override
        public final void execute(final Runnable runnable) {
            checkNullPointer(runnable, "runnable");
            this.checkOpen();
            this.scheduledExecutorService.execute(runnable);
        }

        @Override
        public final void execute(final List<? extends Runnable> runnables) {
            checkCollection(runnables, "runnables");
            this.checkOpen();
            for (final Runnable runnable : runnables) {
                this.scheduledExecutorService.execute(runnable);
            }
        }

        @Override
        public final void schedule(final Runnable runnable, final long time, final TimeUnit timeUnit) {
            checkNullPointer(runnable, "runnable");
            checkNullPointer(timeUnit, "timeUnit");
            checkArgument(time > 0, "time", "Value must be positive");
            this.checkOpen();
            this.scheduledExecutorService.schedule(runnable, time, timeUnit);
        }
    }

    private static final class DelayTask extends TimerTask {

        private final Executor executor;
        private final Runnable runnable;

        private DelayTask(final Executor executor, final Runnable runnable) {
            this.executor = executor;
            this.runnable = runnable;
        }

        @Override
        public final void run() {
            this.executor.execute(this.runnable);
        }
    }
}
