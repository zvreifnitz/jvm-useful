package com.github.zvreifnitz.jpipeline.utils;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static com.github.zvreifnitz.jcore.Preconditions.checkNullPointer;

public final class Predicates {

    private Predicates() {
    }

    public static <T> ToIntFunction<T> switcher(final Predicate<T> predicate) {
        checkNullPointer(predicate, "predicate");
        return new Switcher<>(predicate);
    }

    private static final class Switcher<T> implements ToIntFunction<T> {

        private final Predicate<T> predicate;

        private Switcher(final Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public final int applyAsInt(final T input) {
            return this.predicate.test(input) ? 0 : 1;
        }
    }
}
