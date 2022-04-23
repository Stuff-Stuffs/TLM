package io.github.stuff_stuffs.tlm.common.util;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

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
        if (direction == Direction.NORTH) {
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

    public static HitResult rayCast(final Vec3d start, Vec3d end, final Function<BlockPos, @Nullable HitResult> stopFunction) {
        end = end.add(end.subtract(start).multiply(0.0001));
        final int dx = (int) Math.signum(end.x - start.x);
        final double tDeltaX;
        if (dx != 0) {
            if ((end.x - start.x) != 0) {
                tDeltaX = Math.min(dx / (end.x - start.x), Double.MAX_VALUE);
            } else {
                tDeltaX = Double.MAX_VALUE;
            }
        } else {
            tDeltaX = Double.MAX_VALUE;
        }
        double tMaxX;
        if (dx < 0) {
            tMaxX = tDeltaX * MathHelper.fractionalPart(start.x);
        } else {
            tMaxX = tDeltaX * (1 - MathHelper.fractionalPart(start.x));
        }
        int x = MathHelper.floor(start.x);

        final int dy = (int) Math.signum(end.y - start.y);
        final double tDeltaY;
        if (dy != 0) {
            if ((end.y - start.y) != 0) {
                tDeltaY = Math.min(dy / (end.y - start.y), Double.MAX_VALUE);
            } else {
                tDeltaY = Double.MAX_VALUE;
            }
        } else {
            tDeltaY = Double.MAX_VALUE;
        }
        double tMaxY;
        if (dy < 0) {
            tMaxY = tDeltaY * MathHelper.fractionalPart(start.y);
        } else {
            tMaxY = tDeltaY * (1 - MathHelper.fractionalPart(start.y));
        }
        int y = MathHelper.floor(start.y);

        final int dz = (int) Math.signum(end.z - start.z);
        final double tDeltaZ;
        if (dz != 0) {
            if ((end.z - start.z) != 0) {
                tDeltaZ = Math.min(dz / (end.z - start.z), Double.MAX_VALUE);
            } else {
                tDeltaZ = Double.MAX_VALUE;
            }
        } else {
            tDeltaZ = Double.MAX_VALUE;
        }
        double tMaxZ;
        if (dz < 0) {
            tMaxZ = tDeltaZ * MathHelper.fractionalPart(start.z);
        } else {
            tMaxZ = tDeltaZ * (1 - MathHelper.fractionalPart(start.z));
        }
        int z = MathHelper.floor(start.z);
        final BlockPos.Mutable mutable = new BlockPos.Mutable();
        {
            final HitResult stop = stopFunction.apply(mutable.set(x, y, z));
            if (stop != null) {
                return stop;
            }
        }
        while (!(tMaxX > 1 && tMaxY > 1 && tMaxZ > 1)) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += dx;
                    tMaxX += tDeltaX;
                } else {
                    z += dz;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += dy;
                    tMaxY += tDeltaY;
                } else {
                    z += dz;
                    tMaxZ += tDeltaZ;
                }
            }
            final HitResult stop = stopFunction.apply(mutable.set(x, y, z));
            if (stop != null) {
                return stop;
            }
        }
        return null;
    }

    private MathUtil() {
    }
}
