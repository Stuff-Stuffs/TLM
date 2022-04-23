package io.github.stuff_stuffs.tlm.common.api.resource;

import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.resource.item.ConveyorItemStorage;
import io.github.stuff_stuffs.tlm.common.api.resource.item.CrateItemLoader;
import io.github.stuff_stuffs.tlm.common.api.resource.item.CrateItemStorage;
import io.github.stuff_stuffs.tlm.common.api.storage.SerializableStorage;
import io.github.stuff_stuffs.tlm.common.api.storage.StorageCrate;
import io.github.stuff_stuffs.tlm.common.block.entity.storage.StorageCrateLoaderBlockEntity;
import io.github.stuff_stuffs.tlm.common.util.Factory;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public final class ConveyedResourceType<O, V extends TransferVariant<O>> {
    public static final RegistryKey<Registry<ConveyedResourceType<?, ?>>> REGISTRY_KEY = RegistryKey.ofRegistry(TLM.createId("conveyed_resource_types"));
    public static final Registry<ConveyedResourceType<?, ?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable(), ConveyedResourceType::getReference)).buildAndRegister();
    public static final ConveyedResourceType<Item, ItemVariant> CONVEYED_ITEM_TYPE = new ConveyedResourceType<>(ConveyorItemStorage::new, CrateItemStorage::new, CrateItemLoader::new);
    private final Factory<SerializableStorage<V>> conveyorStorageFactory;
    private final Factory<SerializableStorage<V>> crateStorageFactory;
    private final Factory<CrateLoader<O, V>> crateLoaderFactory;
    private final RegistryEntry.Reference<ConveyedResourceType<?, ?>> reference;

    public ConveyedResourceType(final Factory<SerializableStorage<V>> conveyorStorageFactory, final Factory<SerializableStorage<V>> crateStorageFactory, final Factory<CrateLoader<O, V>> crateLoaderFactory) {
        this.conveyorStorageFactory = conveyorStorageFactory;
        this.crateStorageFactory = crateStorageFactory;
        this.crateLoaderFactory = crateLoaderFactory;
        reference = REGISTRY.createEntry(this);
    }

    public Optional<ConveyedResource<O, V>> cast(final ConveyedResource<?, ?> resource) {
        if (resource.getType() == this) {
            return Optional.of((ConveyedResource<O, V>) resource);
        }
        return Optional.empty();
    }

    public SerializableStorage<V> createConveyorStorage() {
        return conveyorStorageFactory.create();
    }

    public SerializableStorage<V> createCrateStorage() {
        return crateStorageFactory.create();
    }

    public CrateLoader<O, V> createCrateLoader() {
        return crateLoaderFactory.create();
    }

    public RegistryEntry.Reference<ConveyedResourceType<?, ?>> getReference() {
        return reference;
    }

    public interface CrateLoader<O, V extends TransferVariant<O>> {
        boolean tryLoad(StorageCrateLoaderBlockEntity loader, StorageCrate crate);
    }

    public static void init() {
        Registry.register(REGISTRY, TLM.createId("item"), CONVEYED_ITEM_TYPE);
    }
}
