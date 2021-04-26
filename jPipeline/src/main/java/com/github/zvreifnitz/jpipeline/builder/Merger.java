package com.github.zvreifnitz.jpipeline.builder;

import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.entry.Value;
import com.github.zvreifnitz.jpipeline.utils.Funcs;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

@FunctionalInterface
public interface Merger<T, X, R> {

    static <T1, X1, R1> Merger<T1, X1, R1> wrap(final Function<X1, R1> merger) {
        checkNullPointer(merger, "merger");
        return new FunctionToMerger<>(merger);
    }

    static <T1, X1, R1> Merger<T1, X1, R1> wrap(final BiFunction<T1, X1, R1> merger) {
        checkNullPointer(merger, "merger");
        return new BiFunctionToMerger<>(merger);
    }

    static <T1, X1, Y1, R1> Merger<T1, X1, R1> wrap(
            final String pipelineName, final String stepName,
            final Merger<T1, X1, R1> merger, final Function<R1, Y1> mapper,
            final Consumer<PipelineValue<Y1>> consumer) {
        checkNullPointer(pipelineName, "pipelineName");
        checkNullPointer(stepName, "stepName");
        checkNullPointer(merger, "merger");
        checkNullPointer(mapper, "mapper");
        checkNullPointer(consumer, "consumer");
        return new PeekMerger<>(pipelineName, stepName, merger, mapper, consumer);
    }

    R apply(final Value<T> var1, final X var2);

    final class FunctionToMerger<T1, X1, R1> implements Merger<T1, X1, R1> {

        private final Function<X1, R1> merger;

        private FunctionToMerger(final Function<X1, R1> merger) {
            this.merger = merger;
        }

        @Override
        public final R1 apply(final Value<T1> t, final X1 x) {
            return this.merger.apply(x);
        }
    }

    final class BiFunctionToMerger<T1, X1, R1> implements Merger<T1, X1, R1> {

        private final BiFunction<T1, X1, R1> merger;

        private BiFunctionToMerger(final BiFunction<T1, X1, R1> merger) {
            this.merger = merger;
        }

        @Override
        public final R1 apply(final Value<T1> t, final X1 x) {
            return this.merger.apply(t.get(), x);
        }
    }

    final class PeekMerger<T1, X1, Y1, R1> implements Merger<T1, X1, R1> {

        private final String pipelineName;
        private final String stepName;
        private final Merger<T1, X1, R1> merger;
        private final Function<R1, Y1> mapper;
        private final Consumer<PipelineValue<Y1>> consumer;

        private PeekMerger(
                final String pipelineName, final String stepName,
                final Merger<T1, X1, R1> merger, final Function<R1, Y1> mapper,
                final Consumer<PipelineValue<Y1>> consumer) {
            this.pipelineName = pipelineName;
            this.stepName = stepName;
            this.merger = merger;
            this.mapper = mapper;
            this.consumer = consumer;
        }

        @Override
        public final R1 apply(final Value<T1> t, final X1 x) {
            final R1 r = this.merger.apply(t, x);
            Funcs.peek(this.pipelineName, this.stepName, t.getRunId(), r, this.mapper, this.consumer);
            return r;
        }
    }
}
