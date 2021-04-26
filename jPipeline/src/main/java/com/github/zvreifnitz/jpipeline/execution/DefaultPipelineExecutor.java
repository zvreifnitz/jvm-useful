package com.github.zvreifnitz.jpipeline.execution;

import java.util.concurrent.ForkJoinPool;

public final class DefaultPipelineExecutor {

    private static final PipelineExecutor EXECUTOR;

    static {
        EXECUTOR = PipelineExecutors.fromExecutor(ForkJoinPool.commonPool());
    }

    private DefaultPipelineExecutor() {
    }

    public static PipelineExecutor get() {
        return EXECUTOR;
    }
}
