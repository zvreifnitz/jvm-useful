package com.github.zvreifnitz.jcore.exc;

public class AppException extends Exception {
    static final long serialVersionUID = -7522669841561451714L;

    public AppException() {
    }

    public AppException(final String message) {
        super(message);
    }

    public AppException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AppException(final Throwable cause) {
        super(cause);
    }

    public AppException(final String message, final Throwable cause,
                        final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
