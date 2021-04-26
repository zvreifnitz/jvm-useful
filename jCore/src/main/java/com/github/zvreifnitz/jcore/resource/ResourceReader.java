package com.github.zvreifnitz.jcore.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ResourceReader {

    private static final int BUFFER_SIZE = 1024;

    private ResourceReader() {
    }

    public static InputStream getInputStream(final String resource) {
        return getInputStream(resource, Thread.currentThread().getContextClassLoader());
    }

    public static InputStream getInputStream(final String resource, final ClassLoader classLoader) {
        return classLoader.getResourceAsStream(resource);
    }

    public static String getString(final String resource) throws IOException {
        return getString(resource, StandardCharsets.UTF_8, Thread.currentThread().getContextClassLoader());
    }

    public static String getString(final String resource, final Charset charset) throws IOException {
        return getString(resource, charset, Thread.currentThread().getContextClassLoader());
    }

    public static String getString(final String resource, final Charset charset, final ClassLoader classLoader) throws IOException {
        try (final InputStream stream = getInputStream(resource, classLoader);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset))) {
            final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
            final char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, count);
            }
            return sb.toString();
        }
    }
}
