package io.github.stuff_stuffs.tlm.mixin.impl;

import io.github.stuff_stuffs.tlm.common.impl.ClientBlockApiCacheImpl;
import io.github.stuff_stuffs.tlm.mixin.api.ClientWorldCache;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements ClientWorldCache {
    @Unique
    private final Map<BlockPos, List<WeakReference<ClientBlockApiCacheImpl<?, ?>>>> apiLookupCaches = new Object2ReferenceOpenHashMap<>();
    @Unique
    private int apiLookupAccessesWithoutCleanup = 0;

    @Override
    public void tlm_registerCache(final BlockPos pos, final ClientBlockApiCacheImpl<?, ?> cache) {
        final List<WeakReference<ClientBlockApiCacheImpl<?, ?>>> caches = apiLookupCaches.computeIfAbsent(pos.toImmutable(), ignored -> new ArrayList<>());
        caches.removeIf(weakReference -> weakReference.get() == null);
        caches.add(new WeakReference<>(cache));
        apiLookupAccessesWithoutCleanup++;
    }

    @Override
    public void tlm_invalidateCache(final BlockPos pos) {
        final List<WeakReference<ClientBlockApiCacheImpl<?, ?>>> caches = apiLookupCaches.get(pos);
        if (caches != null) {
            caches.removeIf(weakReference -> weakReference.get() == null);
            if (caches.size() == 0) {
                apiLookupCaches.remove(pos);
            } else {
                caches.forEach(weakReference -> {
                    final ClientBlockApiCacheImpl<?, ?> cache = weakReference.get();

                    if (cache != null) {
                        cache.invalidate();
                    }
                });
            }
        }
        apiLookupAccessesWithoutCleanup++;
        // Try to invalidate GC'd lookups from the cache after 2 * the number of cached lookups
        if (apiLookupAccessesWithoutCleanup > 2 * apiLookupCaches.size()) {
            apiLookupCaches.entrySet().removeIf(entry -> {
                entry.getValue().removeIf(weakReference -> weakReference.get() == null);
                return entry.getValue().isEmpty();
            });
            apiLookupAccessesWithoutCleanup = 0;
        }
    }
}
