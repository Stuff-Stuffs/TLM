package io.github.stuff_stuffs.tlm.common.api.resource;

import io.github.stuff_stuffs.tlm.common.api.storage.AbstractDeltaTransactionParticipant;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractChestLikeStorage<T extends TransferVariant<?>> extends AbstractDeltaTransactionParticipant<ResourceAmount<T>, List<ResourceAmount<T>>> implements Storage<T> {
    private final Map<T, VariantEntry> variantEntries;
    private final long maxUnique;
    private long amount;
    protected boolean syncNeeded = true;

    protected AbstractChestLikeStorage(final long maxUnique) {
        variantEntries = new Object2ReferenceOpenHashMap<>((int) maxUnique);
        this.maxUnique = maxUnique;
        amount = 0;
    }

    @Override
    protected List<ResourceAmount<T>> createBlankDelta() {
        return new ArrayList<>();
    }

    @Override
    protected List<ResourceAmount<T>> updateDelta(final ResourceAmount<T> info, final List<ResourceAmount<T>> delta) {
        delta.add(info);
        return delta;
    }

    @Override
    protected void readDelta(final List<ResourceAmount<T>> resourceAmounts) {
        for (int i = resourceAmounts.size() - 1; i >= 0; i--) {
            final ResourceAmount<T> resourceAmount = resourceAmounts.get(i);
            final T resource = resourceAmount.resource();
            final long amount = resourceAmount.amount();
            if (amount > 0) {
                if (extractInner(resource, amount) != amount) {
                    throw new RuntimeException();
                }
            } else {
                if (insertInner(resource, -amount) != -amount) {
                    throw new RuntimeException();
                }
            }
        }
    }

    @Override
    protected List<ResourceAmount<T>> mergeDeltas(final List<ResourceAmount<T>> first, final List<ResourceAmount<T>> second) {
        first.addAll(second);
        return first;
    }

    protected long getUniqueCount() {
        return amount;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(final T resource, final long maxAmount, final TransactionContext transaction) {
        final long inserted = insertInner(resource, maxAmount);
        if (inserted != 0) {
            save(new ResourceAmount<>(resource, inserted), transaction);
        }
        return inserted;
    }

    protected long insertInner(final T resource, final long maxAmount) {
        if (maxAmount == 0 || resource.isBlank()) {
            return 0;
        }
        if (amount == maxUnique) {
            final VariantEntry entry = variantEntries.get(resource);
            if (entry == null) {
                return 0;
            }
        }
        VariantEntry entry = variantEntries.get(resource);
        final long maxStackSize = getMaxStackSize(resource);
        if (maxStackSize == 0) {
            return 0;
        }
        if (entry == null) {
            final long maxInsertable = Math.min(maxStackSize * (maxUnique - amount), maxAmount);

            entry = new VariantEntry(maxInsertable);
            variantEntries.put(resource, entry);
            amount += (maxInsertable + maxStackSize - 1) / maxStackSize;

            return maxInsertable;
        } else {
            final long freeSpace = maxStackSize - ((entry.amount % maxStackSize) + 1);
            if (maxAmount <= freeSpace) {
                entry.amount += maxAmount;
                return maxAmount;
            }
            final long max = maxStackSize * (maxUnique - amount);
            final long maxInsertable = Math.min(max + freeSpace, maxAmount);

            entry.amount += maxInsertable;

            amount += (maxInsertable + maxStackSize - 1 - freeSpace) / maxStackSize;

            return maxInsertable;
        }
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public long extract(final T resource, final long maxAmount, final TransactionContext transaction) {
        final long extracted = extractInner(resource, maxAmount);
        if (extracted != 0) {
            save(new ResourceAmount<>(resource, -extracted), transaction);
        }
        return extracted;
    }

    protected long extractInner(final T resource, final long maxAmount) {
        if (maxAmount == 0) {
            return 0;
        }
        final VariantEntry entry = variantEntries.get(resource);
        if (entry == null) {
            return 0;
        }
        final long maxStackSize = getMaxStackSize(resource);
        final long maxExtractable = Math.min(maxAmount, entry.amount);

        final long leftOver = entry.amount - maxExtractable;
        final long leftOverSpace = (leftOver + maxStackSize - 1) / maxStackSize;
        final long oldSpace = (entry.amount + maxStackSize - 1) / maxStackSize;
        amount = amount - oldSpace + leftOverSpace;
        entry.amount = entry.amount - maxExtractable;
        if (entry.amount == 0) {
            variantEntries.remove(resource);
        }
        return maxExtractable;
    }

    @Override
    public Iterator<? extends StorageView<T>> iterator(final TransactionContext transaction) {
        return iterator();
    }

    protected Iterator<? extends StorageView<T>> iterator() {
        return new ArrayList<>(variantEntries.keySet()).stream().map(resource -> new EntryView(resource)).filter(view -> view.getAmount() != 0 && !view.isResourceBlank()).iterator();
    }

    @Override
    public @Nullable StorageView<T> exactView(final TransactionContext transaction, final T resource) {
        if (variantEntries.containsKey(resource)) {
            return new EntryView(resource);
        } else {
            return null;
        }
    }

    protected abstract long getMaxStackSize(T variant);

    private final class EntryView implements StorageView<T> {
        private final T variant;

        private EntryView(final T variant) {
            this.variant = variant;
        }

        @Override
        public long extract(final T resource, final long maxAmount, final TransactionContext transaction) {
            if (resource.equals(variant)) {
                return AbstractChestLikeStorage.this.extract(resource, maxAmount, transaction);
            } else {
                return 0;
            }
        }

        @Override
        public boolean isResourceBlank() {
            return variant.isBlank();
        }

        @Override
        public T getResource() {
            return variant;
        }

        @Override
        public long getAmount() {
            if (variant.isBlank()) {
                return 0;
            }
            final VariantEntry entry = variantEntries.get(variant);
            if (entry == null) {
                return 0;
            }
            return entry.amount;
        }

        @Override
        public long getCapacity() {
            final VariantEntry entry = variantEntries.get(variant);
            if (entry == null) {
                return getAmount();
            }
            final long maxStackSize = getMaxStackSize(variant);
            final long freeSpace = maxStackSize - ((entry.amount % maxStackSize) + 1);
            final long max = maxStackSize * (maxUnique - amount);
            return max + freeSpace;
        }
    }

    private static final class VariantEntry {
        private long amount;

        private VariantEntry(final long amount) {
            this.amount = amount;
        }
    }
}
