package com.github.zvreifnitz.jpipeline.utils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

public final class Mergers {

    private Mergers() {
    }

    public static <T, X, R> BiFunction<T, X, R> merger(final Function<X, R> merger) {
        checkNullPointer(merger, "merger");
        return (t, x) -> merger.apply(x);
    }

    public static <T, X, R> BiFunction<T, X, R> merger(final BiFunction<T, X, R> merger) {
        checkNullPointer(merger, "merger");
        return merger;
    }

    public static <T, X> BiFunction<T, X, T> merger(final BiConsumer<T, X> merger) {
        checkNullPointer(merger, "merger");
        return (t, r) -> {
            merger.accept(t, r);
            return t;
        };
    }
}