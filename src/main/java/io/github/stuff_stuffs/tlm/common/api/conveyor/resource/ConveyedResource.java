package io.github.stuff_stuffs.tlm.common.api.conveyor.resource;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

public interface ConveyedResource<O, V extends TransferVariant<O>> {
    Storage<V> getBackingStorage();

    boolean isEmpty();

    ConveyedResourceType<O, V, ?> getType();
}
