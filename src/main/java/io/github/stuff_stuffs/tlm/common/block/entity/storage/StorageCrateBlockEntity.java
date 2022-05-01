package io.github.stuff_stuffs.tlm.common.block.entity.storage;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyedResourceType;
import io.github.stuff_stuffs.tlm.common.api.storage.SerializableStorage;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageCrate;
import io.github.stuff_stuffs.tlm.common.block.entity.TLMBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class StorageCrateBlockEntity extends BlockEntity implements StorageCrate {
    private final StorageDelegate delegate;

    public StorageCrateBlockEntity(final BlockPos pos, final BlockState state) {
        super(TLMBlockEntities.STORAGE_CRATE_BLOCK_ENTITY_TYPE, pos, state);
        delegate = new StorageDelegate();
    }

    @Override
    public boolean isEmpty() {
        return delegate.storage == null || delegate.storage.storage().isEmpty();
    }

    @Override
    public @Nullable ConveyedResourceType<?, ?> getOccupied() {
        return isEmpty() ? null : delegate.storage.type();
    }

    @Override
    public <O, V extends TransferVariant<O>> Optional<Storage<V>> getStorage(final ConveyedResourceType<O, V> type, final TransactionContext transaction) {
        if (isEmpty()) {
            save(transaction);
            delegate.storage = createStorage(type);
            return Optional.of((Storage<V>) delegate.storage.storage());
        }
        if (type == delegate.storage.type()) {
            return Optional.of((Storage<V>) delegate.storage.storage());
        }
        return Optional.empty();
    }

    private void save(final TransactionContext transaction) {
        delegate.updateSnapshots(transaction);
    }

    @Override
    protected void writeNbt(final NbtCompound nbt) {
        final TypedPair<?, ?> storage = delegate.storage;
        if (!isEmpty()) {
            final NbtCompound compound = new NbtCompound();
            compound.putString("type", ConveyedResourceType.REGISTRY.getId(storage.type()).toString());
            compound.put("data", storage.storage().writeToNbt());
            nbt.put("storage", compound);
        } else {
            nbt.put("storage", new NbtCompound());
        }
    }

    @Override
    public void readNbt(final NbtCompound nbt) {
        if (nbt.contains("storage")) {
            final NbtCompound compound = nbt.getCompound("storage");
            if(compound.contains("type") && compound.contains("data")) {
                final Identifier typeId = new Identifier(compound.getString("type"));
                final ConveyedResourceType<?, ?> type = ConveyedResourceType.REGISTRY.get(typeId);
                if (type == null) {
                    throw new RuntimeException("No conveyed resource type with id " + typeId);
                }
                delegate.storage = createStorage(type);
                delegate.storage.storage().readFromNbt(compound.getCompound("data"));
            }
        } else if(nbt.contains("BlockEntityTag")) {
            readNbt(nbt.getCompound("BlockEntityTag"));
        }
    }

    private static <O, T extends TransferVariant<O>> TypedPair<?, ?> createStorage(final ConveyedResourceType<O, T> type) {
        return new TypedPair<>(type, type.createCrateStorage());
    }

    private record TypedPair<O, T extends TransferVariant<O>>(ConveyedResourceType<O, T> type,
                                                              SerializableStorage<T> storage) {
    }

    private final class StorageDelegate extends SnapshotParticipant<TypedPair<?, ?>> {
        private static final TypedPair<?, ?> EMPTY = new TypedPair<>(null, null);
        private TypedPair<?, ?> storage;

        @Override
        protected void onFinalCommit() {
            markDirty();
        }

        @Override
        protected TypedPair<?, ?> createSnapshot() {
            return storage == null ? EMPTY : storage;
        }

        @Override
        protected void readSnapshot(final TypedPair<?, ?> snapshot) {
            if (snapshot == EMPTY) {
                storage = null;
            } else {
                storage = snapshot;
            }
        }
    }
}
