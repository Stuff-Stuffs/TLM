package io.github.stuff_stuffs.tlm.common.util;

public final class MathUtil {
    public static final float EPSILON = 1.0E-5F;

    public static boolean greaterThan(final float left, final float right) {
        return left - right >= EPSILON;
    }

    public static boolean lessThan(final float left, final float right) {
        return left - right <= -EPSILON;
    }

    public static boolean equalTo(final float left, final float right) {
        return Math.abs(left - right) < EPSILON;
    }

    private MathUtil() {
    }
}
