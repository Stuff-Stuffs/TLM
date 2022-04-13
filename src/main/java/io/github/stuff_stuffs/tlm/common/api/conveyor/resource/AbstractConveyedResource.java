package io.github.stuff_stuffs.tlm.common.api.conveyor.resource;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.NbtCompound;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractConveyedResource<O, V extends TransferVariant<O>> implements ConveyedResource<O, V> {
    public static final int MAX_SIZE = 64;
    protected V variant;
    protected long count;
    private final StorageImpl storage;

    public AbstractConveyedResource(final NbtCompound compound) {
        storage = new StorageImpl();
        read(compound);
    }

    protected abstract void read(NbtCompound compound);

    public AbstractConveyedResource() {
        storage = new StorageImpl();
    }

    @Override
    public Storage<V> getBackingStorage() {
        return storage;
    }

    @Override
    public boolean isEmpty() {
        return count == 0 || variant.isBlank();
    }

    public abstract NbtCompound toNbt();

    protected abstract long getCapacity(V variant);

    protected abstract V getBlankVariant();

    private class StorageImpl extends SnapshotParticipant<ResourceAmount<V>> implements SingleSlotStorage<V> {
        @Override
        public long insert(final V resource, final long maxAmount, final TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (resource.equals(variant) || variant.isBlank()) {
                final long insertedAmount = Math.min(maxAmount, AbstractConveyedResource.this.getCapacity(resource) - count);

                if (insertedAmount > 0) {
                    updateSnapshots(transaction);

                    if (variant.isBlank()) {
                        variant = resource;
                        count = insertedAmount;
                    } else {
                        count += insertedAmount;
                    }
                }

                return insertedAmount;
            }

            return 0;
        }

        @Override
        public long extract(final V resource, final long maxAmount, final TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);

            if (resource.equals(variant)) {
                final long extractedAmount = Math.min(maxAmount, count);

                if (extractedAmount > 0) {
                    updateSnapshots(transaction);
                    count -= extractedAmount;

                    if (count == 0) {
                        variant = getBlankVariant();
                    }
                }

                return extractedAmount;
            }

            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return variant.isBlank();
        }

        @Override
        public V getResource() {
            return variant;
        }

        @Override
        public long getAmount() {
            return count;
        }

        @Override
        public long getCapacity() {
            return MAX_SIZE;
        }

        @Override
        protected ResourceAmount<V> createSnapshot() {
            return new ResourceAmount<>(variant, count);
        }

        @Override
        protected void readSnapshot(final ResourceAmount<V> snapshot) {
            variant = snapshot.resource();
            count = snapshot.amount();
        }
    }
}
