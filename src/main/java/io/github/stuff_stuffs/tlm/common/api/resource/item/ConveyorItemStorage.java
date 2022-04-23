package io.github.stuff_stuffs.tlm.common.api.resource.item;

import io.github.stuff_stuffs.tlm.common.api.storage.SerializableStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class ConveyorItemStorage implements SerializableStorage<ItemVariant> {
    private final SingleVariantStorage<ItemVariant> delegate = new SingleVariantStorage<>() {
        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(final ItemVariant variant) {
            return variant.getItem().getMaxCount();
        }
    };
    private ItemVariant lastSyncVariant = ItemVariant.blank();
    private long lastSyncAmount = -1;

    @Override
    public NbtCompound writeToNbt() {
        final NbtCompound compound = new NbtCompound();
        compound.put("variant", delegate.variant.toNbt());
        compound.putLong("amount", delegate.amount);
        return compound;
    }

    @Override
    public void readFromNbt(final NbtCompound compound) {
        delegate.variant = ItemVariant.fromNbt(compound.getCompound("variant"));
        delegate.amount = compound.getLong("amount");
    }

    @Override
    public boolean isEmpty() {
        return delegate.isResourceBlank() || delegate.amount == 0;
    }

    @Override
    public boolean isSyncNeeded() {
        return !lastSyncVariant.equals(delegate.variant) || lastSyncAmount != delegate.amount;
    }

    @Override
    public void clearSyncFlag() {
        lastSyncVariant = delegate.variant;
        lastSyncAmount = delegate.amount;
    }

    @Override
    public boolean supportsInsertion() {
        return delegate.supportsInsertion();
    }

    @Override
    public long simulateInsert(final ItemVariant resource, final long maxAmount, @Nullable final TransactionContext transaction) {
        return delegate.simulateInsert(resource, maxAmount, transaction);
    }

    @Override
    public boolean supportsExtraction() {
        return delegate.supportsExtraction();
    }

    @Override
    public long simulateExtract(final ItemVariant resource, final long maxAmount, @Nullable final TransactionContext transaction) {
        return delegate.simulateExtract(resource, maxAmount, transaction);
    }

    @Override
    public Iterable<? extends StorageView<ItemVariant>> iterable(final TransactionContext transaction) {
        return delegate.iterable(transaction);
    }

    @Override
    public @Nullable StorageView<ItemVariant> exactView(final TransactionContext transaction, final ItemVariant resource) {
        return delegate.exactView(transaction, resource);
    }

    @Override
    public long getVersion() {
        return delegate.getVersion();
    }

    @Override
    public long insert(final ItemVariant insertedVariant, final long maxAmount, final TransactionContext transaction) {
        return delegate.insert(insertedVariant, maxAmount, transaction);
    }

    @Override
    public long extract(final ItemVariant extractedVariant, final long maxAmount, final TransactionContext transaction) {
        return delegate.extract(extractedVariant, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator(final TransactionContext transaction) {
        return delegate.iterator(transaction);
    }
}
