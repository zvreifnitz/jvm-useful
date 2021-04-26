package com.github.zvreifnitz.jpipeline.runnable;

import com.github.zvreifnitz.jpipeline.PipelineEntry;

import java.util.List;

public interface ParallelRunnablesFactory<I, R> {
    List<Runnable> create(final PipelineEntry<I, R> entry);
}
