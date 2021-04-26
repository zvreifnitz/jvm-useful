package com.github.zvreifnitz.jpipeline;

import com.github.zvreifnitz.jpipeline.builder.SequentialBuilder;

import java.util.function.Function;

@FunctionalInterface
public interface PipelineBuilder<T, R> extends Function<SequentialBuilder<T, T>, SequentialBuilder<T, R>> {
}
