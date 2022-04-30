package io.github.stuff_stuffs.tlm.common.block.conveyor;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.BlockEntityBlock;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.ThreeSplitterConveyorBlockEntity;
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

public class ThreeSplitterConveyorBlock extends BlockEntityBlock<ThreeSplitterConveyorBlockEntity> {
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 3 / 16.0, 1);
    public ThreeSplitterConveyorBlock(final Settings settings) {
        super(settings, ThreeSplitterConveyorBlockEntity::new, (world, state) -> ThreeSplitterConveyorBlockEntity::tick);
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(final ItemPlacementContext ctx) {
        return getDefaultState().with(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY, ConveyorOrientation.fromHorizontalDirection(ctx.getPlayerFacing()));
    }

    @Override
    public BlockEntityType<ThreeSplitterConveyorBlockEntity> getType() {
        return TLMBlockEntities.THREE_SPLITTER_CONVEYOR_BLOCK_ENTITY_TYPE;
    }
}
