package io.github.stuff_stuffs.tlm.common.api.conveyor.resource;

import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tlm.common.TLM;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public final class ConveyedResourceType<O, V extends TransferVariant<O>, T extends ConveyedResource<O, V>> {
    public static final RegistryKey<Registry<ConveyedResourceType<?, ?, ?>>> REGISTRY_KEY = RegistryKey.ofRegistry(TLM.createId("conveyed_resource_types"));
    public static final Registry<ConveyedResourceType<?, ?, ?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable(), ConveyedResourceType::getReference)).buildAndRegister();
    public static final ConveyedResourceType<Item, ItemVariant, ConveyedItem> CONVEYED_ITEM_TYPE = new ConveyedResourceType<>(Item.class, ItemVariant.class, ConveyedItem.class, ConveyedItem::toNbt, ConveyedItem::new);
    private final Class<O> objectType;
    private final Class<V> transferVariantType;
    private final Class<T> conveyedResourceType;
    private final Function<T, NbtCompound> toNbt;
    private final Function<NbtCompound, T> fromNbt;
    private final RegistryEntry.Reference<ConveyedResourceType<?, ?, ?>> reference;

    public ConveyedResourceType(final Class<O> objectType, final Class<V> transferVariantType, final Class<T> conveyedResourceType, final Function<T, NbtCompound> toNbt, final Function<NbtCompound, T> fromNbt) {
        this.objectType = objectType;
        this.transferVariantType = transferVariantType;
        this.conveyedResourceType = conveyedResourceType;
        this.toNbt = toNbt;
        this.fromNbt = fromNbt;
        reference = REGISTRY.createEntry(this);
    }

    public Optional<T> cast(final ConveyedResource<?, ?> resource) {
        if (resource.getType() == this) {
            return Optional.of((T) resource);
        }
        return Optional.empty();
    }

    public NbtCompound toNbt(final T resource) {
        return toNbt.apply(resource);
    }

    public T fromNbt(final NbtCompound compound) {
        return fromNbt.apply(compound);
    }

    public RegistryEntry.Reference<ConveyedResourceType<?, ?, ?>> getReference() {
        return reference;
    }

    public static void init() {
        Registry.register(REGISTRY, TLM.createId("item"), CONVEYED_ITEM_TYPE);
    }
}
