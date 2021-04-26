package com.github.zvreifnitz.jcore.exc;

public final class Exceptions {

    private Exceptions() {
    }

    public static ExceptionInInitializerError staticCtorFail(final Throwable throwable) {
        return new ExceptionInInitializerError(throwable);
    }

    public static <X> X throwStaticCtorFail(final Throwable throwable) {
        throw staticCtorFail(throwable);
    }

    public static NullPointerException nullPointer(final String param) {
        return new NullPointerException("Parameter '" + param + "' must not be null.");
    }

    public static <X> X throwNullPointer(final String param) {
        throw nullPointer(param);
    }

    public static IllegalArgumentException illegalArgument(final String param) {
        return illegalArgument(param, null);
    }

    public static <X> X throwIllegalArgument(final String param) {
        throw illegalArgument(param, null);
    }

    public static IllegalArgumentException illegalArgument(final String param, final String msg) {
        return new IllegalArgumentException("Parameter '" + param + "' is invalid." + ((msg == null) ? "" : " " + msg));
    }

    public static <X> X throwIllegalArgument(final String param, final String msg) {
        throw illegalArgument(param, msg);
    }

    public static IllegalStateException illegalState(final String msg) {
        return new IllegalStateException(msg);
    }

    public static <X> X throwIllegalState(final String msg) {
        throw illegalState(msg);
    }

    public static void rethrowIfError(final Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
    }

    public static <T> T rethrow(final Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable != null) {
            throw new AppRuntimeException(throwable);
        }
        return null;
    }

    public static <T> T rethrowOrShutdown(final Throwable throwable) {
        if (throwable instanceof Error) {
            Runtime.getRuntime().exit(1);
            throw (Error) throwable;
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable != null) {
            throw new AppRuntimeException(throwable);
        }
        return null;
    }
}
