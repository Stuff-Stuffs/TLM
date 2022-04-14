package io.github.stuff_stuffs.tlm.common.block.properties;

import io.github.stuff_stuffs.tlm.common.api.UnsidedBlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public enum ConveyorOrientation implements StringIdentifiable {
    NORTH(Direction.NORTH.getOpposite(), Direction.NORTH, Type.STRAIGHT),
    SOUTH(Direction.SOUTH.getOpposite(), Direction.SOUTH, Type.STRAIGHT),
    EAST(Direction.EAST.getOpposite(), Direction.EAST, Type.STRAIGHT),
    WEST(Direction.WEST.getOpposite(), Direction.WEST, Type.STRAIGHT),
    NORTH_TO_EAST(Direction.NORTH.getOpposite(), Direction.EAST, Type.CLOCKWISE_CORNER),
    NORTH_TO_WEST(Direction.NORTH.getOpposite(), Direction.WEST, Type.COUNTER_CLOCKWISE_CORNER),
    SOUTH_TO_EAST(Direction.SOUTH.getOpposite(), Direction.EAST, Type.COUNTER_CLOCKWISE_CORNER),
    SOUTH_TO_WEST(Direction.SOUTH.getOpposite(), Direction.WEST, Type.CLOCKWISE_CORNER),
    EAST_TO_NORTH(Direction.EAST.getOpposite(), Direction.NORTH, Type.COUNTER_CLOCKWISE_CORNER),
    EAST_TO_SOUTH(Direction.EAST.getOpposite(), Direction.SOUTH, Type.CLOCKWISE_CORNER),
    WEST_TO_NORTH(Direction.WEST.getOpposite(), Direction.NORTH, Type.CLOCKWISE_CORNER),
    WEST_TO_SOUTH(Direction.WEST.getOpposite(), Direction.SOUTH, Type.COUNTER_CLOCKWISE_CORNER),
    NORTH_UP(Direction.NORTH.getOpposite(), Direction.NORTH, pos -> pos.offset(Direction.SOUTH), pos -> pos.offset(Direction.NORTH).offset(Direction.UP), Type.UP_SLOPE),
    NORTH_DOWN(Direction.NORTH.getOpposite(), Direction.NORTH, pos -> pos.offset(Direction.SOUTH).offset(Direction.UP), pos -> pos.offset(Direction.NORTH), Type.DOWN_SLOPE),
    SOUTH_UP(Direction.SOUTH.getOpposite(), Direction.SOUTH, pos -> pos.offset(Direction.NORTH), pos -> pos.offset(Direction.SOUTH).offset(Direction.UP), Type.UP_SLOPE),
    SOUTH_DOWN(Direction.SOUTH.getOpposite(), Direction.SOUTH, pos -> pos.offset(Direction.NORTH).offset(Direction.UP), pos -> pos.offset(Direction.SOUTH), Type.DOWN_SLOPE),
    EAST_UP(Direction.EAST.getOpposite(), Direction.EAST, pos -> pos.offset(Direction.WEST), pos -> pos.offset(Direction.EAST).offset(Direction.UP), Type.UP_SLOPE),
    EAST_DOWN(Direction.EAST.getOpposite(), Direction.EAST, pos -> pos.offset(Direction.WEST).offset(Direction.UP), pos -> pos.offset(Direction.EAST), Type.DOWN_SLOPE),
    WEST_UP(Direction.WEST.getOpposite(), Direction.WEST, pos -> pos.offset(Direction.EAST), pos -> pos.offset(Direction.WEST).offset(Direction.UP), Type.UP_SLOPE),
    WEST_DOWN(Direction.WEST.getOpposite(), Direction.WEST, pos -> pos.offset(Direction.EAST).offset(Direction.UP), pos -> pos.offset(Direction.WEST), Type.DOWN_SLOPE);

    private final Direction inputSide;
    private final Direction outputDirection;
    private final UnaryOperator<BlockPos> inputPosFunc;
    private final UnaryOperator<BlockPos> outputPosFunc;
    private final Type type;

    ConveyorOrientation(final Direction inputSide, final Direction outputDirection, final Type type) {
        this(inputSide, outputDirection, pos -> pos.offset(inputSide), pos -> pos.offset(outputDirection), type);
    }

    ConveyorOrientation(final Direction inputSide, final Direction outputDirection, final UnaryOperator<BlockPos> inputPosFunc, final UnaryOperator<BlockPos> outputPosFunc, final Type type) {
        this.inputSide = inputSide;
        this.outputDirection = outputDirection;
        this.inputPosFunc = inputPosFunc;
        this.outputPosFunc = outputPosFunc;
        this.type = type;
    }

    public static <A, C> Supplier<@Nullable A> createInputFinder(final BlockApiLookup<A, C> lookup, final C context, final BlockPos pos, final World world) {
        final BlockApiCache<A, C> first = UnsidedBlockApiCache.getUnsidedCache(lookup, world, pos);
        final BlockApiCache<A, C> down = UnsidedBlockApiCache.getUnsidedCache(lookup, world, pos.offset(Direction.DOWN));
        return () -> {
            A api = first.find(context);
            if (api != null) {
                return api;
            } else {
                api = down.find(context);
                if (api != null) {
                    final BlockEntity entity = down.getBlockEntity();
                    if (entity != null) {
                        final BlockState state = entity.getCachedState();
                        if (state.contains(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY) && state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY).getType() == Type.UP_SLOPE) {
                            return api;
                        }
                    }
                }
            }
            return null;
        };
    }

    public static <A, C> Supplier<@Nullable A> createOutputFinder(final BlockApiLookup<A, C> lookup, final C context, final BlockPos pos, final World world) {
        final BlockApiCache<A, C> first = UnsidedBlockApiCache.getUnsidedCache(lookup, world, pos);
        final BlockApiCache<A, C> down = UnsidedBlockApiCache.getUnsidedCache(lookup, world, pos.offset(Direction.DOWN));
        return () -> {
            A api = first.find(context);
            if (api != null) {
                return api;
            } else {
                api = down.find(context);
                if (api != null) {
                    final BlockEntity entity = down.getBlockEntity();
                    if (entity != null) {
                        final BlockState state = entity.getCachedState();
                        if (state.contains(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY) && state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY).getType() == Type.DOWN_SLOPE) {
                            return api;
                        }
                    }
                }
            }
            return null;
        };
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

    public BlockPos getInputPos(final BlockPos pos) {
        return inputPosFunc.apply(pos);
    }

    public Type getType() {
        return type;
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
        if (best == Direction.UP) {
            return switch (ctx.getSide().getOpposite()) {
                case NORTH -> NORTH_DOWN;
                case SOUTH -> SOUTH_DOWN;
                case EAST -> EAST_DOWN;
                case WEST -> WEST_DOWN;
                default -> throw new RuntimeException();
            };
        }
        if (best == Direction.DOWN) {
            return switch (ctx.getSide().getOpposite()) {
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
            if (direction.getAxis() != Direction.Axis.Y) {
                final double score = delta.dotProduct(new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()));
                if (score > bestScore) {
                    bestScore = score;
                    best = direction;
                }
            }
        }
        if (best == null) {
            return null;
        }
        if (best == horizontalDirection) {
            return fromHorizontalDirection(horizontalDirection.getOpposite());
        }
        return horizontal(horizontalDirection, best.getOpposite());
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

    public enum Type {
        STRAIGHT,
        CLOCKWISE_CORNER,
        COUNTER_CLOCKWISE_CORNER,
        UP_SLOPE,
        DOWN_SLOPE
    }
}
