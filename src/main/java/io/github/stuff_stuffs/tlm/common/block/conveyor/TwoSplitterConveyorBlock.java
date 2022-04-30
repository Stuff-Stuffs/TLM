package io.github.stuff_stuffs.tlm.common.block.conveyor;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.BlockEntityBlock;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.TwoSplitterConveyorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class TwoSplitterConveyorBlock extends BlockEntityBlock<TwoSplitterConveyorBlockEntity> {
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 3 / 16.0, 1);

    public TwoSplitterConveyorBlock(final Settings settings) {
        super(settings, TwoSplitterConveyorBlockEntity::new, (world, state) -> TwoSplitterConveyorBlockEntity::tick);
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
    }

    @Override
    public VoxelShape getOutlineShape(final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(final ItemPlacementContext ctx) {
        return getDefaultState().with(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY, ConveyorOrientation.fromHorizontalDirection(ctx.getPlayerFacing()));
    }

    @Override
    public BlockEntityType<TwoSplitterConveyorBlockEntity> getType() {
        return TLMBlockEntities.TWO_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE;
    }
}
