package io.github.stuff_stuffs.tlm.common.impl;

import io.github.stuff_stuffs.tlm.common.api.conveyor.ConveyorAccess;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResource;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Item;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("UnstableApiUsage")
public class ConveyorItemStorageWrapper implements Storage<ItemVariant> {
    private final ConveyorAccess conveyor;

    public ConveyorItemStorageWrapper(final ConveyorAccess conveyor) {
        this.conveyor = conveyor;
    }

    @Override
    public long insert(final ItemVariant resource, final long maxAmount, final TransactionContext transaction) {
        if (maxAmount <= 0) {
            return 0;
        }
        long currentAmount = 0;
        final Iterator<Storage<ItemVariant>> iterator = filter().iterator();
        while (currentAmount < maxAmount && iterator.hasNext()) {
            final Storage<ItemVariant> next = iterator.next();
            currentAmount += next.insert(resource, maxAmount - currentAmount, transaction);
        }
        return currentAmount;
    }

    @Override
    public long extract(final ItemVariant resource, final long maxAmount, final TransactionContext transaction) {
        if (maxAmount <= 0) {
            return 0;
        }
        long currentAmount = 0;
        final Iterator<Storage<ItemVariant>> iterator = filter().iterator();
        while (currentAmount < maxAmount && iterator.hasNext()) {
            final Storage<ItemVariant> next = iterator.next();
            currentAmount += next.extract(resource, maxAmount - currentAmount, transaction);
        }
        return currentAmount;
    }

    @Override
    public Iterator<? extends StorageView<ItemVariant>> iterator(final TransactionContext transaction) {
        return filter().mapMulti((BiConsumer<Storage<ItemVariant>, Consumer<StorageView<ItemVariant>>>) (itemVariantStorage, consumer) -> {
            for (final StorageView<ItemVariant> view : itemVariantStorage.iterable(transaction)) {
                consumer.accept(view);
            }
        }).iterator();
    }

    private Stream<Storage<ItemVariant>> filter() {
        final Iterator<ConveyorTray> trays = conveyor.getTrays();
        if (!trays.hasNext()) {
            return Stream.empty();
        } else {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(trays, Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE), false).map(ConveyorTray::getResource).filter(Optional::isPresent).filter(opt -> opt.get().getType() == ConveyedResourceType.CONVEYED_ITEM_TYPE).map(i -> ((ConveyedResource<Item, ItemVariant>) i.get()).getBackingStorage());
        }
    }
}
