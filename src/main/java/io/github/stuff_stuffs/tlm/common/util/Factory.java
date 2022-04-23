package io.github.stuff_stuffs.tlm.common.util;

/**
 * Represents a factory for creating instances.
 *
 * <p>This must return a new instance every invocation
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #create()}.
 *
 * @param <T> the type of result created by this factory
 */
@FunctionalInterface
public interface Factory<T> {
    T create();
}
