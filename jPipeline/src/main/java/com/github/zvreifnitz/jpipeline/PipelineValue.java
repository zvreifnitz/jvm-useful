package com.github.zvreifnitz.jpipeline;

import com.github.zvreifnitz.jpipeline.entry.AbstractValue;

public final class PipelineValue<T> extends AbstractValue<T> {

    private final String pipeline;
    private final String step;

    public PipelineValue(final String pipeline, final String step, final String runId, final T value) {
        super(runId, value);
        this.pipeline = pipeline;
        this.step = step;
    }

    public final String getPipeline() {
        return this.pipeline;
    }

    public final String getStep() {
        return this.step;
    }

    @Override
    public final String toString() {
        return "{pipeline='" + this.pipeline +
                "', step='" + this.step +
                "', runId='" + this.getRunId() +
                "', value='" + this.get() + "'}";
    }
}
