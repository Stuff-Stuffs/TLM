package io.github.stuff_stuffs.tlm.common.api;

import io.github.stuff_stuffs.tlm.common.impl.ClientBlockApiCacheImpl;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class UnsidedBlockApiCache {
    public static <A, C> BlockApiCache<A, C> getUnsidedCache(final BlockApiLookup<A, C> lookup, final World world, final BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            return BlockApiCache.create(lookup, serverWorld, pos);
        }
        if (!(lookup instanceof BlockApiLookupImpl)) {
            throw new IllegalArgumentException("Cannot cache foreign implementation of BlockApiLookup. Use `BlockApiLookup#get(Identifier, Class<A>, Class<C>);` to get instances.");
        }
        if (world instanceof ClientWorld clientWorld) {
            return new ClientBlockApiCacheImpl<>((BlockApiLookupImpl<A, C>) lookup, clientWorld, pos);
        }
        throw new UnsupportedOperationException("Tried to get a BlockApiCache on an unsupported world");
    }

    private UnsidedBlockApiCache() {
    }
}
