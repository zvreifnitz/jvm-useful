package com.github.zvreifnitz.jcore;

import java.util.Collection;

import static com.github.zvreifnitz.jcore.exc.Exceptions.*;

public final class Preconditions {

    private Preconditions() {
    }

    public static <T, X extends Collection<T>> void checkCollection(final X input, final String param) {
        if (input == null) {
            throwNullPointer(param);
            return;
        }
        if (input.isEmpty()) {
            throwIllegalArgument(param, "Provided collection is empty.");
            return;
        }
        for (final T item : input) {
            if (item == null) {
                throwIllegalArgument(param, "Provided collection contains null item(s).");
            }
        }
    }

    public static <X> void checkNullPointer(final X input, final String param) {
        if (input == null) {
            throwNullPointer(param);
        }
    }

    public static void checkArgument(final boolean condition, final String param) {
        if (!condition) {
            throwIllegalArgument(param);
        }
    }

    public static void checkArgument(final boolean condition, final String param, final String msg) {
        if (!condition) {
            throwIllegalArgument(param, msg);
        }
    }

    public static void checkState(final boolean condition, final String msg) {
        if (!condition) {
            throwIllegalState(msg);
        }
    }

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    public static <X> X notNullOrExc(final X input, final String param) {
        return (input != null) ? input : throwNullPointer(param);
    }

    public static <X> X notNullOrProvided(final X input, final X provided) {
        return (input != null) ? input : provided;
    }
}
