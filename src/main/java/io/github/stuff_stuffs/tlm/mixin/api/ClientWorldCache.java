package io.github.stuff_stuffs.tlm.mixin.api;

import io.github.stuff_stuffs.tlm.common.impl.ClientBlockApiCacheImpl;
import net.minecraft.util.math.BlockPos;

public interface ClientWorldCache {
    void tlm_registerCache(BlockPos pos, ClientBlockApiCacheImpl<?, ?> cache);

    void tlm_invalidateCache(BlockPos pos);
}
