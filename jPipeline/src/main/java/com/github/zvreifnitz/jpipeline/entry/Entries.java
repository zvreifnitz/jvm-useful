package com.github.zvreifnitz.jpipeline.entry;

import com.github.zvreifnitz.jpipeline.PipelineEntry;
import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.builder.Merger;
import com.github.zvreifnitz.jpipeline.utils.Funcs;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Entries {

    private Entries() {
    }

    public static <T, R, X> PipelineEntry<T, R> peek(
            final PipelineEntry<T, R> entry,
            final String pipelineName, final String stepName,
            final Function<R, X> mapper, final Consumer<PipelineValue<X>> consumer) {
        return new PeekEntry<>(entry, pipelineName, stepName, mapper, consumer);
    }

    public static <T, X, R> PipelineEntry<X, R> adapter(
            final PipelineEntry<T, R> entry, final Function<T, X> extractor) {
        return adapter(entry, extractor, Merger.wrap(Funcs::passThrough));
    }

    public static <T, X, R> PipelineEntry<T, X> adapter(
            final PipelineEntry<T, R> entry, final BiFunction<T, X, R> merger) {
        return adapter(entry, Funcs.identityFunc(), merger);
    }

    public static <T, X, Y, R> PipelineEntry<T, R> adapter(
            final PipelineEntry<X, Y> entry, final Function<X, T> extractor, final BiFunction<X, R, Y> merger) {
        return new AdapterEntry<>(entry, extractor, merger);
    }

    public static <T, X, Y, R> PipelineEntry<T, R> adapter(
            final PipelineEntry<X, Y> entry, final Function<X, T> extractor, final Merger<X, R, Y> merger) {
        return new AdapterEntry<>(entry, extractor, merger);
    }

    private static final class PeekEntry<T, R, X> extends EntryWrapper<T, T, R, R> {

        private final String pipelineName;
        private final String stepName;
        private final Function<R, X> mapper;
        private final Consumer<PipelineValue<X>> consumer;

        private PeekEntry(
                final PipelineEntry<T, R> entry,
                final String pipelineName, final String stepName,
                final Function<R, X> mapper, final Consumer<PipelineValue<X>> consumer) {
            super(entry, Funcs.identityFunc(), Funcs.passThroughMerger());
            this.pipelineName = pipelineName;
            this.stepName = stepName;
            this.mapper = mapper;
            this.consumer = consumer;
        }

        @Override
        public final boolean tryAccept(final R result, final long time, final TimeUnit timeUnit) {
            if (super.tryAccept(result, time, timeUnit)) {
                this.peek(result);
                return true;
            } else {
                return false;
            }
        }

        private void peek(final R result) {
            Funcs.peek(this.pipelineName, this.stepName, this.getRunId(), result, this.mapper, this.consumer);
        }
    }

    private static final class AdapterEntry<T, X, Y, R> extends EntryWrapper<T, X, Y, R> {

        private AdapterEntry(
                final PipelineEntry<X, Y> entry, final Function<X, T> extractor, final Function<R, Y> merger) {
            super(entry, extractor, Merger.wrap(merger));
        }

        private AdapterEntry(
                final PipelineEntry<X, Y> entry, final Function<X, T> extractor, final BiFunction<X, R, Y> merger) {
            super(entry, extractor, Merger.wrap(merger));
        }

        private AdapterEntry(
                final PipelineEntry<X, Y> entry, final Function<X, T> extractor, final Merger<X, R, Y> merger) {
            super(entry, extractor, merger);
        }
    }
}
