package io.github.stuff_stuffs.tlm.common.api.conveyor.resource;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

@SuppressWarnings("UnstableApiUsage")
public final class ConveyedItem extends AbstractConveyedResource<Item, ItemVariant> {
    public static final int MAX_SIZE = 64;

    public ConveyedItem(final NbtCompound compound) {
        super(compound);
    }

    public ConveyedItem() {
        super();
    }

    @Override
    protected void read(final NbtCompound compound) {
        count = compound.getLong("count");
        if (count == 0) {
            variant = ItemVariant.blank();
        } else {
            variant = ItemVariant.fromNbt(compound.getCompound("variant"));
        }
    }

    @Override
    public NbtCompound toNbt() {
        final NbtCompound compound = new NbtCompound();
        compound.putLong("count", count);
        compound.put("variant", variant.toNbt());
        return compound;
    }

    @Override
    protected long getCapacity(final ItemVariant variant) {
        return Math.min(MAX_SIZE, variant.getItem().getMaxCount());
    }

    @Override
    protected ItemVariant getBlankVariant() {
        return ItemVariant.blank();
    }

    @Override
    public ConveyedResourceType<Item, ItemVariant, ?> getType() {
        return ConveyedResourceType.CONVEYED_ITEM_TYPE;
    }
}
