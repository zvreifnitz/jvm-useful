package com.github.zvreifnitz.jcore.concurrent;

public final class VisibilityBarrier<T> {
    private final T instance;

    public VisibilityBarrier(final T instance) {
        this.instance = instance;
    }

    public static <I> I makeVisible(final I instance) {
        final VisibilityBarrier<I> barrier = new VisibilityBarrier<>(instance);
        return barrier.getInstance();
    }

    public T getInstance() {
        return this.instance;
    }
}
