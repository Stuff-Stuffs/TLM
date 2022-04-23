package io.github.stuff_stuffs.tlm.common.api.resource;

import io.github.stuff_stuffs.tlm.common.api.storage.SerializableStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public final class ConveyedResource<O, V extends TransferVariant<O>> {
    private final SerializableStorage<V> storage;
    private final ConveyedResourceType<O, V> type;

    public ConveyedResource(final ConveyedResourceType<O, V> type) {
        this.type = type;
        storage = type.createConveyorStorage();
    }

    private ConveyedResource(final NbtCompound storageData, final ConveyedResourceType<O, V> type) {
        storage = type.createConveyorStorage();
        storage.readFromNbt(storageData);
        this.type = type;
    }

    public Storage<V> getBackingStorage() {
        return storage;
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public boolean isSyncNeeded() {
        return storage.isSyncNeeded();
    }

    public void clearSyncFlag() {
        storage.clearSyncFlag();
    }

    public ConveyedResourceType<O, V> getType() {
        return type;
    }

    public NbtCompound writeToNbt() {
        final NbtCompound compound = new NbtCompound();
        compound.putString("type", ConveyedResourceType.REGISTRY.getId(type).toString());
        compound.put("data", storage.writeToNbt());
        return compound;
    }

    public static ConveyedResource<?, ?> readFromNbt(final NbtCompound compound) {
        final Identifier typeId = new Identifier(compound.getString("type"));
        final ConveyedResourceType<?, ?> type = ConveyedResourceType.REGISTRY.get(typeId);
        if (type == null) {
            throw new RuntimeException("Could not find resource type " + typeId);
        }
        return new ConveyedResource<>(compound.getCompound("data"), type);
    }
}
