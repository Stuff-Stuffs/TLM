package io.github.stuff_stuffs.tlm.common.block;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.entity.ConveyorBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.properties.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ConveyorBlock extends BlockEntityBlock<ConveyorBlockEntity> {
    private static final Map<ConveyorOrientation, VoxelShape> SHAPE_MAP;

    public ConveyorBlock(final Settings settings) {
        super(settings, ConveyorBlockEntity::new, (world, state) -> ConveyorBlockEntity::tick);
    }

    @Override
    public BlockEntityType<ConveyorBlockEntity> getType() {
        return TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE;
    }

    @Override
    public VoxelShape getOutlineShape(final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context) {
        final ConveyorOrientation orientation = state.get(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY);
        return SHAPE_MAP.get(orientation);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(final ItemPlacementContext ctx) {
        final ConveyorOrientation orientation = ConveyorOrientation.getFromContext(ctx);
        if (orientation == null) {
            return null;
        }
        return getDefaultState().with(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY, orientation);
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TLMBlockProperties.CONVEYOR_ORIENTATION_PROPERTY);
    }

    static {
        SHAPE_MAP = new EnumMap<>(ConveyorOrientation.class);
        final Map<ConveyorOrientation.Type, VoxelShape> unrotated = new EnumMap<>(ConveyorOrientation.Type.class);
        unrotated.put(ConveyorOrientation.Type.STRAIGHT, VoxelShapes.cuboid(0, 0, 0, 1, 3 / 16.0, 1));
        unrotated.put(ConveyorOrientation.Type.CLOCKWISE_CORNER, VoxelShapes.cuboid(0, 0, 0, 1, 3 / 16.0, 1));
        unrotated.put(ConveyorOrientation.Type.COUNTER_CLOCKWISE_CORNER, VoxelShapes.cuboid(0, 0, 0, 1, 3 / 16.0, 1));
        VoxelShape slope = VoxelShapes.empty();
        for (int i = 0; i < 16; i++) {
            final double start = Math.max(Math.min(i / 16.0, 1), 0);
            final double end = Math.max(Math.min((i + 3) / 16.0, 1), 0);
            slope = VoxelShapes.union(slope, VoxelShapes.cuboid(0, 1 - end, 1 - end, 1, 1 - start, 1 - start));
        }
        unrotated.put(ConveyorOrientation.Type.UP_SLOPE, slope);
        unrotated.put(ConveyorOrientation.Type.DOWN_SLOPE, MathUtil.rotate(slope, Direction.SOUTH));
        for (final ConveyorOrientation orientation : ConveyorOrientation.values()) {
            SHAPE_MAP.put(orientation, MathUtil.rotate(unrotated.get(orientation.getType()), orientation.getInputSide()));
        }
    }
}
