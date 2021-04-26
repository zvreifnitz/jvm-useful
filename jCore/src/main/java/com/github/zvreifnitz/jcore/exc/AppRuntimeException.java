package com.github.zvreifnitz.jcore.exc;

public class AppRuntimeException extends RuntimeException {
    static final long serialVersionUID = -2389091949112565499L;

    public AppRuntimeException() {
    }

    public AppRuntimeException(final String message) {
        super(message);
    }

    public AppRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AppRuntimeException(final Throwable cause) {
        super(cause);
    }

    public AppRuntimeException(final String message, final Throwable cause,
                               final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
