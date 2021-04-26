package com.github.zvreifnitz.jpipeline.utils;

import com.github.zvreifnitz.jpipeline.PipelineValue;
import com.github.zvreifnitz.jpipeline.builder.Merger;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

public final class Funcs {

    private Funcs() {
    }

    public static <T> Function<T, T> consumerToFunc(final Consumer<T> consumer) {
        checkNullPointer(consumer, "consumer");
        return i -> {
            consumer.accept(i);
            return i;
        };
    }

    public static <T, R> Function<T, R> supplierToFunc(final Supplier<R> supplier) {
        checkNullPointer(supplier, "supplier");
        return i -> supplier.get();
    }

    public static <T, R> Function<T, R> combine(final Consumer<T> consumer, final Supplier<R> supplier) {
        checkNullPointer(consumer, "consumer");
        checkNullPointer(supplier, "supplier");
        return i -> {
            consumer.accept(i);
            return supplier.get();
        };
    }

    public static <T, X, R> Function<T, R> combine(final Function<T, X> first, final Function<X, R> second) {
        checkNullPointer(first, "first");
        checkNullPointer(second, "second");
        return i -> second.apply(first.apply(i));
    }

    public static <T> Function<T, T> identityFunc() {
        return Funcs::identity;
    }

    public static <T> T identity(final T result) {
        return result;
    }

    public static <T, R> BiFunction<T, R, R> passThroughFunc() {
        return Funcs::passThrough;
    }

    public static <T, R> Merger<T, R, R> passThroughMerger() {
        return Funcs::passThrough;
    }

    public static <T, R> R passThrough(final T ignored, final R result) {
        return result;
    }

    public static <T> Consumer<T> blackHoleFunc() {
        return Funcs::blackHole;
    }

    public static <T> void blackHole(final T ignored) {
    }

    public static <R> void peek(
            final String pipelineName, final String stepName,
            final String runId, final R result,
            final Consumer<PipelineValue<R>> consumer) {
        peek(pipelineName, stepName, runId, result, Funcs.identityFunc(), consumer);
    }

    public static <R, X> void peek(
            final String pipelineName, final String stepName, final String runId, final R result,
            final Function<R, X> mapper, final Consumer<PipelineValue<X>> consumer) {
        try {
            consumer.accept(new PipelineValue<>(pipelineName, stepName, runId, mapper.apply(result)));
        } catch (final Exception ignored) {
        }
    }
}
