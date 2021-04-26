package com.github.zvreifnitz.jpipeline;

@FunctionalInterface
public interface PipelineStep<T, R> {
    void process(final PipelineEntry<T, R> entry);
}
