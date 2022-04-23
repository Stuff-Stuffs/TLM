package io.github.stuff_stuffs.tlm.common.api.storage;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public interface StorageCrate {
    boolean isEmpty();

    @Nullable ConveyedResourceType<?, ?> getOccupied();

    <O,V extends TransferVariant<O>> Optional<Storage<V>> getStorage(ConveyedResourceType<O, V> type, TransactionContext transaction);
}
