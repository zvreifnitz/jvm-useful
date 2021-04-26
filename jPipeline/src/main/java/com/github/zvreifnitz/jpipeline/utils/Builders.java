package com.github.zvreifnitz.jpipeline.utils;

import com.github.zvreifnitz.jpipeline.PipelineBuilder;
import com.github.zvreifnitz.jpipeline.builder.SequentialBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;
import static com.github.zvreifnitz.jpipeline.BuilderHelper.step;

public final class Builders {

    private Builders() {
    }

    public static <T, R> PipelineBuilder<T, R> adapter(final PipelineBuilder<T, R> builder) {
        checkNullPointer(builder, "builder");
        return builder;
    }

    public static <T, X, R> PipelineBuilder<T, R> adapter(
            final PipelineBuilder<T, X> builder, final BiFunction<T, X, R> merger) {
        checkNullPointer(builder, "builder");
        checkNullPointer(merger, "merger");
        return new MergerBuilder<>(builder, merger);
    }

    public static <T, X, R> PipelineBuilder<T, R> adapter(
            final Function<T, X> extractor, final PipelineBuilder<X, R> builder) {
        checkNullPointer(extractor, "extractor");
        checkNullPointer(builder, "builder");
        return new ExtractorBuilder<>(extractor, builder);
    }

    public static <T, X, Y, R> PipelineBuilder<T, R> adapter(
            final Function<T, X> extractor, final PipelineBuilder<X, Y> builder, final BiFunction<T, Y, R> merger) {
        checkNullPointer(extractor, "extractor");
        checkNullPointer(builder, "builder");
        checkNullPointer(merger, "merger");
        return new ExtractorMergerBuilder<>(extractor, builder, merger);
    }

    private static final class MergerBuilder<T, Y, R>
            implements PipelineBuilder<T, R> {

        private final PipelineBuilder<T, Y> builder;
        private final BiFunction<T, Y, R> merger;

        private MergerBuilder(
                final PipelineBuilder<T, Y> builder, final BiFunction<T, Y, R> merger) {
            this.builder = builder;
            this.merger = merger;
        }

        @Override
        public final SequentialBuilder<T, R> apply(final SequentialBuilder<T, T> input) {
            return input
                    .buildParallelStep("builder.adapter", this.builder, this.merger)
                    .join();
        }
    }

    private static final class ExtractorBuilder<T, X, R>
            implements PipelineBuilder<T, R> {

        private final Function<T, X> extractor;
        private final PipelineBuilder<X, R> builder;

        private ExtractorBuilder(
                final Function<T, X> extractor, final PipelineBuilder<X, R> builder) {
            this.extractor = extractor;
            this.builder = builder;
        }

        @Override
        public final SequentialBuilder<T, R> apply(final SequentialBuilder<T, T> input) {
            return input
                    .addStep(step(this.extractor))
                    .buildStep("builder.adapter", this.builder);
        }
    }

    private static final class ExtractorMergerBuilder<T, X, Y, R>
            implements PipelineBuilder<T, R> {

        private final Function<T, X> extractor;
        private final PipelineBuilder<X, Y> builder;
        private final BiFunction<T, Y, R> merger;

        private ExtractorMergerBuilder(
                final Function<T, X> extractor, final PipelineBuilder<X, Y> builder, final BiFunction<T, Y, R> merger) {
            this.extractor = extractor;
            this.builder = builder;
            this.merger = merger;
        }

        @Override
        public final SequentialBuilder<T, R> apply(final SequentialBuilder<T, T> input) {
            return input
                    .buildParallelStep(
                            "builder",
                            p -> p.addStep(step(this.extractor))
                                    .buildStep("adapter", this.builder),
                            this.merger)
                    .join();
        }
    }
}
