package io.github.stuff_stuffs.tlm.common.api.resource.item;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageCrate;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateLoaderBlockEntity;
import io.github.stuff_stuffs.tlm.common.block.storage.StorageCrateLoaderBlock;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public final class CrateItemLoader implements ConveyedResourceType.CrateLoader<Item, ItemVariant> {
    private BlockApiCache<Storage<ItemVariant>, Direction> cache;
    private Direction side;

    public CrateItemLoader() {
    }

    @Override
    public boolean tryLoad(final StorageCrateLoaderBlockEntity loader, final StorageCrate crate) {
        if (cache == null) {
            final Direction direction = loader.getCachedState().get(StorageCrateLoaderBlock.FACING);
            cache = BlockApiCache.create(ItemStorage.SIDED, (ServerWorld) loader.getWorld(), loader.getPos().offset(direction));
            side = direction.getOpposite();
        }
        final Storage<ItemVariant> storage = cache.find(side);
        if (storage == null) {
            return false;
        }
        try (final Transaction transaction = Transaction.openOuter()) {
            final Optional<Storage<ItemVariant>> optional = crate.getStorage(ConveyedResourceType.CONVEYED_ITEM_TYPE, transaction);
            if (optional.isPresent()) {
                final Storage<ItemVariant> crateStorage = optional.get();
                if (StorageUtil.move(storage, crateStorage, i -> true, 1, transaction) > 0) {
                    transaction.commit();
                    return true;
                }
            }
            transaction.abort();
        }
        return false;
    }
}
