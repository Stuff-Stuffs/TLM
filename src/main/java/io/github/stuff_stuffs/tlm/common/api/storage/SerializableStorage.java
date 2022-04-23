package io.github.stuff_stuffs.tlm.common.api.storage;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;

@SuppressWarnings("UnstableApiUsage")
public interface SerializableStorage<V extends TransferVariant<?>> extends Storage<V> {
    NbtCompound writeToNbt();

    void readFromNbt(NbtCompound compound);

    boolean isEmpty();

    boolean isSyncNeeded();

    void clearSyncFlag();
}
