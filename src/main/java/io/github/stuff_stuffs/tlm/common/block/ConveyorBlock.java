package io.github.stuff_stuffs.tlm.common.block;

import io.github.stuff_stuffs.tlm.common.block.entity.ConveyorBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.properties.ConveyorOrientation;
import io.github.stuff_stuffs.tlm.common.block.properties.TLMBlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import org.jetbrains.annotations.Nullable;

public class ConveyorBlock extends BlockEntityBlock<ConveyorBlockEntity> {
    public ConveyorBlock(final Settings settings) {
        super(settings, ConveyorBlockEntity::new, (world, state) -> ConveyorBlockEntity::tick);
    }

    @Override
    public BlockEntityType<ConveyorBlockEntity> getType() {
        return TLMBlockEntities.CONVEYOR_BLOCK_ENTITY_TYPE;
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
}
