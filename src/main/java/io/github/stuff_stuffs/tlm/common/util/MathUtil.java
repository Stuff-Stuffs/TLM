package io.github.stuff_stuffs.tlm.common.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.commons.lang3.mutable.MutableObject;

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

    public static VoxelShape rotate(final VoxelShape shape, final Direction direction) {
        if(direction==Direction.NORTH) {
            return shape;
        }
        final MutableObject<VoxelShape> rotated = new MutableObject<>(VoxelShapes.empty());
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            switch (direction) {
                case SOUTH -> {
                    double tmp;
                    tmp = minX;
                    minX = 1 - maxX;
                    maxX = 1 - tmp;

                    tmp = minZ;
                    minZ = 1 - maxZ;
                    maxZ = 1 - tmp;
                }
                case EAST -> {
                    double tmp;
                    tmp = minX;
                    minX = minZ;
                    minZ = tmp;

                    tmp = maxX;
                    maxX = maxZ;
                    maxZ = tmp;

                    tmp = minX;
                    minX = 1 - maxX;
                    maxX = 1 - tmp;

                    tmp = minZ;
                    minZ = 1 - maxZ;
                    maxZ = 1 - tmp;
                }
                case WEST -> {
                    double tmp;
                    tmp = minX;
                    minX = minZ;
                    minZ = tmp;

                    tmp = maxX;
                    maxX = maxZ;
                    maxZ = tmp;
                }
            }
            rotated.setValue(VoxelShapes.union(rotated.getValue(), VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ)));
        });
        return rotated.getValue().simplify();
    }

    private MathUtil() {
    }
}
