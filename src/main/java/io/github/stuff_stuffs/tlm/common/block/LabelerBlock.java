package io.github.stuff_stuffs.tlm.common.block;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.entity.LabelerBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import org.jetbrains.annotations.Nullable;

public class LabelerBlock extends BlockEntityBlock<LabelerBlockEntity> {
    public LabelerBlock(final Settings settings) {
        super(settings, LabelerBlockEntity::new, (world, state) -> LabelerBlockEntity::tick);
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
    public BlockEntityType<LabelerBlockEntity> getType() {
        return TLMBlockEntities.LABELER_BLOCK_ENTITY_BLOCK_TYPE;
    }
}
