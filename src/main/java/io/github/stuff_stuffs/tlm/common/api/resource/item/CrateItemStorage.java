package io.github.stuff_stuffs.tlm.common.api.resource.item;

import io.github.stuff_stuffs.tlm.common.api.resource.AbstractChestLikeStorage;
import io.github.stuff_stuffs.tlm.common.api.storage.SerializableStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class CrateItemStorage extends AbstractChestLikeStorage<ItemVariant> implements SerializableStorage<ItemVariant> {
    public CrateItemStorage() {
        super(27);
    }

    @Override
    protected long getMaxStackSize(final ItemVariant variant) {
        if (variant.getItem() instanceof BlockItem item) {
            if (item.getBlock() instanceof ShulkerBoxBlock) {
                return 0;
            }
        }
        return variant.getItem().getMaxCount();
    }

    @Override
    public NbtCompound writeToNbt() {
        final NbtCompound compound = new NbtCompound();
        final NbtList nbtList = new NbtList();
        compound.put("data", nbtList);
        final Iterator<? extends StorageView<ItemVariant>> iterator = iterator();
        while (iterator.hasNext()) {
            final StorageView<ItemVariant> next = iterator.next();
            final NbtCompound entry = new NbtCompound();
            entry.put("variant", next.getResource().toNbt());
            entry.putLong("amount", next.getAmount());
            nbtList.add(entry);
        }
        return compound;
    }

    @Override
    public void readFromNbt(final NbtCompound compound) {
        final Iterator<? extends StorageView<ItemVariant>> iterator = iterator();
        while (iterator.hasNext()) {
            final StorageView<ItemVariant> next = iterator.next();
            if (extractInner(next.getResource(), next.getAmount()) != next.getAmount()) {
                throw new RuntimeException();
            }
        }
        final NbtList nbtList = compound.getList("data", NbtElement.COMPOUND_TYPE);
        for (final NbtElement element : nbtList) {
            final NbtCompound entry = (NbtCompound) element;
            final ItemVariant variant = ItemVariant.fromNbt(entry.getCompound("variant"));
            final long amount = entry.getLong("amount");
            insertInner(variant, amount);
        }
    }

    @Override
    public boolean isEmpty() {
        return getUniqueCount() == 0;
    }

    @Override
    public boolean isSyncNeeded() {
        return syncNeeded;
    }

    @Override
    public void clearSyncFlag() {
        syncNeeded = false;
    }
}
