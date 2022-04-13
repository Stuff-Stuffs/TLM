package io.github.stuff_stuffs.tlm.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public abstract class BlockEntityBlock<BE extends BlockEntity> extends Block implements BlockEntityProvider {
    private final BiFunction<BlockPos, BlockState, BE> factory;
    private final BiFunction<World, BlockState, ? extends BlockEntityTicker<BE>> tickerFactory;
    private final BiFunction<ServerWorld, BE, GameEventListener> listenerFactory;

    public BlockEntityBlock(final Settings settings, final BiFunction<BlockPos, BlockState, BE> factory) {
        this(settings, factory, (world, blockState) -> null);
    }

    public BlockEntityBlock(final Settings settings, final BiFunction<BlockPos, BlockState, BE> factory, final BiFunction<World, BlockState, ? extends BlockEntityTicker<BE>> tickerFactory) {
        this(settings, factory, tickerFactory, (serverWorld, be) -> null);
    }

    public BlockEntityBlock(final Settings settings, final BiFunction<BlockPos, BlockState, BE> factory, final BiFunction<World, BlockState, ? extends BlockEntityTicker<BE>> tickerFactory, final BiFunction<ServerWorld, BE, GameEventListener> listenerFactory) {
        super(settings);
        this.factory = factory;
        this.tickerFactory = tickerFactory;
        this.listenerFactory = listenerFactory;
    }

    public abstract BlockEntityType<BE> getType();

    @Nullable
    @Override
    public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
        return factory.apply(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final World world, final BlockState state, final BlockEntityType<T> type) {
        if (getType() == type) {
            return (BlockEntityTicker<T>) tickerFactory.apply(world, state);
        }
        return null;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> GameEventListener getGameEventListener(final ServerWorld world, final T blockEntity) {
        if (blockEntity.getType() == getType()) {
            return listenerFactory.apply(world, (BE) blockEntity);
        }
        return null;
    }
}
