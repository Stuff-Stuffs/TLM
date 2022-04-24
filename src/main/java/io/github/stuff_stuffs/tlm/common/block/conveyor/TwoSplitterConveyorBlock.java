package io.github.stuff_stuffs.tlm.common.block.conveyor;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.BlockEntityBlock;
import io.github.stuff_stuffs.tlm.common.block.TLMBlockProperties;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.entity.conveyor.TwoSplitterConveyorBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import org.jetbrains.annotations.Nullable;

public class TwoSplitterConveyorBlock extends BlockEntityBlock<TwoSplitterConveyorBlockEntity> {
    public TwoSplitterConveyorBlock(final Settings settings) {
        super(settings, TwoSplitterConveyorBlockEntity::new, (world, state) -> TwoSplitterConveyorBlockEntity::tick);
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TLMBlockProperties.CONVEYOR_STRAIGHT_FLAT_ORIENTATION_PROPERTY);
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
