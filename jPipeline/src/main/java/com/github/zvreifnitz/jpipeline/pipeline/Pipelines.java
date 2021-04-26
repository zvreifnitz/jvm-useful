package com.github.zvreifnitz.jpipeline.pipeline;

import com.github.zvreifnitz.jpipeline.Pipeline;
import com.github.zvreifnitz.jpipeline.execution.PipelineExecutor;
import com.github.zvreifnitz.jpipeline.runnable.RunnableFactory;

public final class Pipelines {
    private Pipelines() {
    }

    public static <T, R> Pipeline<T, R> unbounded(
            final PipelineExecutor executor, final RunnableFactory<T, R> runnableFactory) {
        return new UnboundedPipeline<>(executor, runnableFactory);
    }
}
