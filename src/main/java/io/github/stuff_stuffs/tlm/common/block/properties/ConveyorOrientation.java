package io.github.stuff_stuffs.tlm.common.block.properties;

import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.UnaryOperator;

public enum ConveyorOrientation implements StringIdentifiable {
    NORTH(Direction.NORTH.getOpposite(), Direction.NORTH),
    SOUTH(Direction.SOUTH.getOpposite(), Direction.SOUTH),
    EAST(Direction.EAST.getOpposite(), Direction.EAST),
    WEST(Direction.WEST.getOpposite(), Direction.WEST),
    NORTH_TO_EAST(Direction.NORTH.getOpposite(), Direction.EAST),
    NORTH_TO_WEST(Direction.NORTH.getOpposite(), Direction.WEST),
    SOUTH_TO_EAST(Direction.SOUTH.getOpposite(), Direction.EAST),
    SOUTH_TO_WEST(Direction.SOUTH.getOpposite(), Direction.WEST),
    EAST_TO_NORTH(Direction.EAST.getOpposite(), Direction.NORTH),
    EAST_TO_SOUTH(Direction.EAST.getOpposite(), Direction.SOUTH),
    WEST_TO_NORTH(Direction.WEST.getOpposite(), Direction.NORTH),
    WEST_TO_SOUTH(Direction.WEST.getOpposite(), Direction.SOUTH),
    NORTH_UP(Direction.NORTH.getOpposite(), Direction.NORTH, Direction.UP),
    NORTH_DOWN(Direction.NORTH.getOpposite(), Direction.NORTH, Direction.DOWN),
    SOUTH_UP(Direction.SOUTH.getOpposite(), Direction.SOUTH, Direction.UP),
    SOUTH_DOWN(Direction.SOUTH.getOpposite(), Direction.SOUTH, Direction.DOWN),
    EAST_UP(Direction.EAST.getOpposite(), Direction.EAST, Direction.UP),
    EAST_DOWN(Direction.EAST.getOpposite(), Direction.EAST, Direction.DOWN),
    WEST_UP(Direction.WEST.getOpposite(), Direction.WEST, Direction.UP),
    WEST_DOWN(Direction.WEST.getOpposite(), Direction.WEST, Direction.DOWN);

    private final Direction inputSide;
    private final Direction outputDirection;
    private final UnaryOperator<BlockPos> outputPosFunc;

    ConveyorOrientation(final Direction inputSide, final Direction outputDirection, final Direction offset) {
        this(inputSide, outputDirection, pos -> pos.offset(outputDirection).offset(offset));
    }

    ConveyorOrientation(final Direction inputSide, final Direction outputDirection) {
        this(inputSide, outputDirection, pos -> pos.offset(outputDirection));
    }

    ConveyorOrientation(final Direction inputSide, final Direction outputDirection, final UnaryOperator<BlockPos> outputPosFunc) {
        this.inputSide = inputSide;
        this.outputDirection = outputDirection;
        this.outputPosFunc = outputPosFunc;
    }

    public Direction getOutputDirection() {
        return outputDirection;
    }

    public Direction getInputSide() {
        return inputSide;
    }

    public BlockPos getOutputPos(final BlockPos pos) {
        return outputPosFunc.apply(pos);
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static @Nullable ConveyorOrientation getFromContext(final ItemPlacementContext ctx) {
        final Direction hitSide = ctx.getSide();
        if (hitSide.getAxis() == Direction.Axis.Y) {
            return getFromContextHorizontal(ctx);
        } else {
            return getFromContextVertical(ctx);
        }
    }

    private static @Nullable ConveyorOrientation getFromContextVertical(final ItemPlacementContext ctx) {
        final BlockPos pos = ctx.getBlockPos();
        final Vec3d center = Vec3d.ofCenter(pos);
        final Vec3d faceCenter = center.withBias(ctx.getSide(), -0.5);
        final Vec3d delta = faceCenter.subtract(ctx.getHitPos());
        if (delta.lengthSquared() < 0.25 * 0.25) {
            return fromHorizontalDirection(ctx.getSide());
        }
        Direction best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (final Direction direction : Direction.values()) {
            final double score = delta.dotProduct(new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()));
            if (score > bestScore) {
                bestScore = score;
                best = direction;
            }
        }
        if (best == null) {
            return null;
        }
        if (best == Direction.DOWN) {
            return switch (ctx.getSide()) {
                case NORTH -> NORTH_DOWN;
                case SOUTH -> SOUTH_DOWN;
                case EAST -> EAST_DOWN;
                case WEST -> WEST_DOWN;
                default -> throw new RuntimeException();
            };
        }
        if (best == Direction.UP) {
            return switch (ctx.getSide()) {
                case NORTH -> NORTH_UP;
                case SOUTH -> SOUTH_UP;
                case EAST -> EAST_UP;
                case WEST -> WEST_UP;
                default -> throw new RuntimeException();
            };
        }
        return horizontal(ctx.getSide(), best);
    }

    private static @Nullable ConveyorOrientation getFromContextHorizontal(final ItemPlacementContext ctx) {
        final BlockPos pos = ctx.getBlockPos();
        final Vec3d center = Vec3d.ofCenter(pos);
        final Vec3d faceCenter = center.withBias(ctx.getSide(), -0.5);
        final Vec3d delta = faceCenter.subtract(ctx.getHitPos());
        final Direction horizontalDirection = ctx.getPlayerFacing();
        if (delta.lengthSquared() < 0.25 * 0.25) {
            return fromHorizontalDirection(horizontalDirection);
        }
        Direction best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (final Direction direction : Direction.values()) {
            final double score = delta.dotProduct(new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()));
            if (score > bestScore) {
                bestScore = score;
                best = direction;
            }
        }
        if (best == null) {
            return null;
        }
        if (best == horizontalDirection) {
            return fromHorizontalDirection(horizontalDirection.getOpposite());
        }
        return horizontal(horizontalDirection, best);
    }

    public static ConveyorOrientation horizontal(final Direction from, final Direction to) {
        if (from == to) {
            throw new IllegalArgumentException("Expected two different directions got same!");
        }
        if (from == to.getOpposite()) {
            return fromHorizontalDirection(from);
        }
        return switch (from) {
            case NORTH -> switch (to) {
                case EAST -> NORTH_TO_EAST;
                case WEST -> NORTH_TO_WEST;
                default -> throw new IllegalArgumentException("Expected horizontal direction, got " + to);
            };
            case SOUTH -> switch (to) {
                case EAST -> SOUTH_TO_EAST;
                case WEST -> SOUTH_TO_WEST;
                default -> throw new IllegalArgumentException("Expected horizontal direction, got " + to);
            };
            case EAST -> switch (to) {
                case NORTH -> EAST_TO_NORTH;
                case SOUTH -> EAST_TO_SOUTH;
                default -> throw new IllegalArgumentException("Expected horizontal direction, got " + to);
            };
            case WEST -> switch (to) {
                case NORTH -> WEST_TO_NORTH;
                case SOUTH -> WEST_TO_SOUTH;
                default -> throw new IllegalArgumentException("Expected horizontal direction, got " + to);
            };
            default -> throw new IllegalArgumentException("Expected horizontal direction, got " + from);
        };
    }

    public static ConveyorOrientation fromHorizontalDirection(final Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> throw new IllegalArgumentException("Expected horizontal direction, got " + direction);
        };
    }
}
