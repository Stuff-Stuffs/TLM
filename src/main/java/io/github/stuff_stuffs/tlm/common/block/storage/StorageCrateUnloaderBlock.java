package io.github.stuff_stuffs.tlm.common.block.storage;

import io.github.stuff_stuffs.tlm.common.block.BlockEntityBlock;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateUnloaderBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class StorageCrateUnloaderBlock extends BlockEntityBlock<StorageCrateUnloaderBlockEntity> {
    public static final VoxelShape Y_SHAPE = createCuboidShape(0, 1, 0, 16, 15, 16);
    public static final VoxelShape X_SHAPE = createCuboidShape(1, 0, 0, 15, 16, 16);
    public static final VoxelShape Z_SHAPE = createCuboidShape(0, 0, 1, 16, 16, 15);
    public static final Property<Direction> FACING = Properties.FACING;

    public StorageCrateUnloaderBlock(final Settings settings) {
        super(settings, StorageCrateUnloaderBlockEntity::new, (world, state) -> StorageCrateUnloaderBlockEntity::tick);
    }

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context) {
        final Direction.Axis axis = state.get(FACING).getAxis();
        return switch (axis) {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(final ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getSide());
    }

    @Override
    public BlockEntityType<StorageCrateUnloaderBlockEntity> getType() {
        return TLMBlockEntities.STORAGE_CRATE_UNLOADER_BLOCK_ENTITY_TYPE;
    }
}
