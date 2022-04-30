package io.github.stuff_stuffs.tlm.common.block.entity.storage;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.Conveyor;
import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorApi;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResource;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageApi;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageCrate;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import io.github.stuff_stuffs.tlm.common.block.storage.StorageCrateUnloaderBlock;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class StorageCrateUnloaderBlockEntity extends BlockEntity {
    private BlockApiCache<StorageCrate, Void> crateCache;
    private BlockApiCache<Conveyor, Direction> conveyorCache;
    private boolean initialized = false;

    public StorageCrateUnloaderBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.STORAGE_CRATE_UNLOADER_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static void tick(final World world, final BlockPos pos, final BlockState state, final StorageCrateUnloaderBlockEntity unloader) {
        if (!unloader.initialized) {
            if (world instanceof ServerWorld serverWorld) {
                final Direction direction = state.get(StorageCrateUnloaderBlock.FACING);
                unloader.crateCache = BlockApiCache.create(StorageApi.STORAGE_CRATE_BLOCK_API_LOOKUP, serverWorld, pos.offset(direction));
                unloader.conveyorCache = BlockApiCache.create(ConveyorApi.CONVEYOR_BLOCK_API_LOOKUP, serverWorld, pos.offset(direction.getOpposite()));
            }
            unloader.initialized = true;
        }
        if (world.isClient()) {
            return;
        }
        final StorageCrate storageCrate = unloader.crateCache.find(null);
        if (storageCrate != null && !storageCrate.isEmpty()) {
            final ConveyedResourceType<?, ?> type = storageCrate.getOccupied();
            final Conveyor conveyor = unloader.conveyorCache.find(null);
            if (conveyor != null) {
                move(type, storageCrate, conveyor);
            }
        }
    }

    private static <O, T extends TransferVariant<O>> void move(final ConveyedResourceType<O, T> type, final StorageCrate crate, final Conveyor conveyor) {
        while (true) {
            try (final Transaction transaction = Transaction.openOuter()) {
                final ConveyedResource<O, T> resource = new ConveyedResource<>(type);
                final Optional<Storage<T>> optional = crate.getStorage(type, transaction);
                if (optional.isEmpty()) {
                    transaction.abort();
                    return;
                }
                if (StorageUtil.move(optional.get(), resource.getBackingStorage(), i -> true, Long.MAX_VALUE, transaction) == 0) {
                    transaction.abort();
                    return;
                }
                final ConveyorTray tray = new ConveyorTray(resource);
                if (conveyor.tryInsert(tray, 1, TLM.getTickOrder())) {
                    transaction.commit();
                } else {
                    transaction.abort();
                    return;
                }
            }
        }
    }
}
