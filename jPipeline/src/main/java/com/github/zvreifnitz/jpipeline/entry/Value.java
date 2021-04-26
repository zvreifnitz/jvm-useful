package com.github.zvreifnitz.jpipeline.entry;

import java.util.function.Supplier;

public interface Value<T> extends Supplier<T> {
    static <A> Value<A> create(final String runId, final A input) {
        return new DefaultValue<>(runId, input);
    }

    String getRunId();

    final class DefaultValue<A> extends AbstractValue<A> {
        private DefaultValue(final String runId, final A value) {
            super(runId, value);
        }
    }
}
