package io.github.stuff_stuffs.tlm.common.block.entity.storage;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageApi;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageCrate;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.storage.StorageCrateLoaderBlock;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@SuppressWarnings("UnstableApiUsage")
public class StorageCrateLoaderBlockEntity extends BlockEntity {
    private final Reference2ReferenceLinkedOpenHashMap<ConveyedResourceType<?, ?>, ConveyedResourceType.CrateLoader<?, ?>> crateLoaders;
    private BlockApiCache<StorageCrate, Void> crateCache;
    private boolean initialized = false;

    public StorageCrateLoaderBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.STORAGE_CRATE_LOADER_BLOCK_ENTITY_TYPE, pos, state);
        crateLoaders = new Reference2ReferenceLinkedOpenHashMap<>();
    }

    @Override
    public void setCachedState(final BlockState state) {
        if (state != getCachedState()) {
            initialized = false;
        }
        super.setCachedState(state);
    }

    public <O, V extends TransferVariant<O>> void addCrateLoader(final ConveyedResourceType<O, V> type) {
        crateLoaders.putAndMoveToLast(type, type.createCrateLoader());
    }

    public <O, V extends TransferVariant<O>> ConveyedResourceType.CrateLoader<O, V> getCrateLoader(final ConveyedResourceType<O, V> type) {
        return (ConveyedResourceType.CrateLoader<O, V>) crateLoaders.get(type);
    }

    public static void tick(final World world, final BlockPos pos, final BlockState state, final StorageCrateLoaderBlockEntity loader) {
        if (!loader.initialized) {
            if (world instanceof ServerWorld serverWorld) {
                loader.crateLoaders.clear();
                for (final ConveyedResourceType<?, ?> type : ConveyedResourceType.REGISTRY) {
                    loader.addCrateLoader(type);
                }
                final Direction direction = state.get(StorageCrateLoaderBlock.FACING);
                loader.crateCache = BlockApiCache.create(StorageApi.STORAGE_CRATE_BLOCK_API_LOOKUP, serverWorld, pos.offset(direction.getOpposite()));
            }
            loader.initialized = true;
        }
        if (world.isClient()) {
            return;
        }
        final StorageCrate storageCrate = loader.crateCache.find(null);
        if (storageCrate != null) {
            if (storageCrate.isEmpty()) {
                for (final ConveyedResourceType<?, ?> type : ConveyedResourceType.REGISTRY) {
                    if (tryMove(loader, storageCrate, type)) {
                        break;
                    }
                }
            } else {
                tryMove(loader, storageCrate, storageCrate.getOccupied());
            }
        }
    }

    private static <O, V extends TransferVariant<O>> boolean tryMove(final StorageCrateLoaderBlockEntity loader, final StorageCrate crate, final ConveyedResourceType<O, V> type) {
        final ConveyedResourceType.CrateLoader<O, V> crateLoader = loader.getCrateLoader(type);
        return crateLoader.tryLoad(loader, crate);
    }
}
