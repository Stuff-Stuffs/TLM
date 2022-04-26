package io.github.stuff_stuffs.tlm.common.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class CollisionUtil {
    public static float sweep(final Box first, final Box second, final Vec3d firstVelocity) {
        final float entryX = getEntryTime(first, second, firstVelocity, Direction.Axis.X);
        if (Float.isNaN(entryX)) {
            return Float.NaN;
        }
        final float exitX = getExitTime(first, second, firstVelocity, Direction.Axis.X);
        if (Float.isNaN(exitX) || exitX < entryX) {
            return Float.NaN;
        }

        final float entryY = getEntryTime(first, second, firstVelocity, Direction.Axis.Y);
        if (Float.isNaN(entryY) || entryY > exitX) {
            return Float.NaN;
        }
        final float exitY = getExitTime(first, second, firstVelocity, Direction.Axis.Y);
        if (Float.isNaN(exitY) || exitY < entryY || entryX > exitY) {
            return Float.NaN;
        }

        final float entryZ = getEntryTime(first, second, firstVelocity, Direction.Axis.Z);
        if (Float.isNaN(entryZ) || entryZ > Math.min(exitX, exitY)) {
            return Float.NaN;
        }
        final float exitZ = getExitTime(first, second, firstVelocity, Direction.Axis.Z);
        if (Float.isNaN(exitZ) || exitZ < entryZ || exitZ < Math.max(entryX, entryY)) {
            return Float.NaN;
        }
        final float n = Math.max(Math.max(entryX, entryY), entryZ);
        if (n < 0 || n > 1) {
            return Float.NaN;
        }
        return n;
    }

    private static float getEntryTime(final Box first, final Box second, final Vec3d firstVelocity, final Direction.Axis axis) {
        if (MathUtil.equalTo((float) firstVelocity.getComponentAlongAxis(axis), 0)) {
            if (first.getMin(axis) <= second.getMax(axis) && second.getMin(axis) <= first.getMax(axis)) {
                return Float.NEGATIVE_INFINITY;
            } else {
                return Float.NaN;
            }
        } else {
            final float entryDistanceX;
            if (firstVelocity.x > 0) {
                entryDistanceX = (float) (second.minX - first.maxX);
            } else {
                entryDistanceX = (float) (first.minX - second.maxX);
            }
            return entryDistanceX / Math.abs((float) firstVelocity.x);
        }
    }

    private static float getExitTime(final Box first, final Box second, final Vec3d firstVelocity, final Direction.Axis axis) {
        if (MathUtil.equalTo((float) firstVelocity.getComponentAlongAxis(axis), 0)) {
            if (first.getMin(axis) <= second.getMax(axis) && second.getMin(axis) <= first.getMax(axis)) {
                return Float.POSITIVE_INFINITY;
            } else {
                return Float.NaN;
            }
        } else {
            final float exitDistanceX;
            if (firstVelocity.x > 0) {
                exitDistanceX = (float) (second.maxX - first.minX);
            } else {
                exitDistanceX = (float) (first.maxX - second.minX);
            }
            return exitDistanceX / Math.abs((float) firstVelocity.x);
        }
    }

    private CollisionUtil() {
    }
}
