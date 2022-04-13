package io.github.stuff_stuffs.tlm.common.impl;

import io.github.stuff_stuffs.tlm.mixin.api.ClientWorldCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;
import net.fabricmc.fabric.impl.lookup.block.ServerWorldCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ClientBlockApiCacheImpl<A,C> implements BlockApiCache<A,C> {
    private final BlockApiLookupImpl<A, C> lookup;
    private final ClientWorld world;
    private final BlockPos pos;
    private boolean blockEntityCacheValid = false;
    private BlockEntity cachedBlockEntity = null;
    private BlockState lastState = null;
    private BlockApiLookup.BlockApiProvider<A, C> cachedProvider = null;

    public ClientBlockApiCacheImpl(BlockApiLookupImpl<A, C> lookup, ClientWorld world, BlockPos pos) {
        ((ClientWorldCache) world).tlm_registerCache(pos, this);
        this.lookup = lookup;
        this.world = world;
        this.pos = pos.toImmutable();
    }

    public void invalidate() {
        blockEntityCacheValid = false;
        cachedBlockEntity = null;
        lastState = null;
        cachedProvider = null;
    }

    @Nullable
    @Override
    public A find(@Nullable BlockState state, C context) {
        // Update block entity cache
        getBlockEntity();

        // Get block state
        if (state == null) {
            if (cachedBlockEntity != null) {
                state = cachedBlockEntity.getCachedState();
            } else {
                state = world.getBlockState(pos);
            }
        }

        // Get provider
        if (lastState != state) {
            cachedProvider = lookup.getProvider(state.getBlock());
            lastState = state;
        }

        // Query the provider
        A instance = null;

        if (cachedProvider != null) {
            instance = cachedProvider.find(world, pos, state, cachedBlockEntity, context);
        }

        if (instance != null) {
            return instance;
        }

        // Query the fallback providers
        for (BlockApiLookup.BlockApiProvider<A, C> fallbackProvider : lookup.getFallbackProviders()) {
            instance = fallbackProvider.find(world, pos, state, cachedBlockEntity, context);

            if (instance != null) {
                return instance;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity() {
        if (!blockEntityCacheValid) {
            cachedBlockEntity = world.getBlockEntity(pos);
            blockEntityCacheValid = true;
        }

        return cachedBlockEntity;
    }

    @Override
    public BlockApiLookupImpl<A, C> getLookup() {
        return lookup;
    }

    @Override
    public ServerWorld getWorld() {
        throw new UnsupportedOperationException("Cannot get world of ClientBlockApiCache");
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }
}
