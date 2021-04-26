package com.github.zvreifnitz.jpipeline.entry;

public abstract class AbstractValue<T> implements Value<T> {

    private final String runId;
    private final T value;

    protected AbstractValue(final String runId, final T value) {
        this.runId = runId;
        this.value = value;
    }

    @Override
    public final String getRunId() {
        return this.runId;
    }

    @Override
    public final T get() {
        return this.value;
    }
}
