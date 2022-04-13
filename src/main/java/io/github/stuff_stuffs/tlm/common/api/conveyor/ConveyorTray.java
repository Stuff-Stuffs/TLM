package io.github.stuff_stuffs.tlm.common.api.conveyor;

import io.github.stuff_stuffs.tlm.common.TLM;
import io.github.stuff_stuffs.tlm.common.api.conveyor.resource.ConveyedResource;
import io.github.stuff_stuffs.tlm.common.api.conveyor.resource.ConveyedResourceType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public final class ConveyorTray {
    public static final float TRAY_SIZE = 0.5F;
    private ConveyedResource<?, ?> resource;
    private Vec3d lastPos = Vec3d.ZERO;
    private Vec3d currentPos = Vec3d.ZERO;

    public ConveyorTray() {
        resource = null;
    }

    public ConveyorTray(final ConveyedResource<?, ?> resource) {
        this.resource = resource;
    }

    public void setPosition(final Vec3d pos, final boolean override) {
        if (override) {
            lastPos = pos;
        } else {
            lastPos = currentPos;
        }
        currentPos = pos;
    }

    public Vec3d interpolatePosition(final double t) {
        final Vec3d delta = currentPos.subtract(lastPos);
        return lastPos.add(delta.multiply(t));
    }

    public Optional<ConveyedResource<?, ?>> getResource() {
        if (isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resource);
    }

    public boolean tryInsert(final ConveyedResource<?, ?> resource) {
        if (isEmpty()) {
            this.resource = resource;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return resource == null || resource.isEmpty();
    }

    public NbtCompound toNbt() {
        final NbtCompound compound = new NbtCompound();
        if (!isEmpty()) {
            final Optional<NbtCompound> encoded = encode(resource, resource.getType());
            if (encoded.isEmpty()) {
                throw new RuntimeException("Error while encoding item");
            }
            compound.putString("type", ConveyedResourceType.REGISTRY.getId(resource.getType()).toString());
            compound.put("resource", encoded.get());
        }
        return compound;
    }

    private <T extends ConveyedResource<?, ?>> Optional<NbtCompound> encode(final ConveyedResource<?, ?> resource, final ConveyedResourceType<?, ?, T> type) {
        return type.cast(resource).map(type::toNbt);
    }

    public static ConveyorTray fromNbt(final NbtCompound compound) {
        if (compound.contains("resource", NbtElement.COMPOUND_TYPE) && compound.contains("type", NbtElement.STRING_TYPE)) {
            final NbtCompound encoded = compound.getCompound("resource");
            final Identifier typeId = new Identifier(compound.getString("type"));
            final ConveyedResourceType<?, ?, ?> type = ConveyedResourceType.REGISTRY.get(typeId);
            if (type == null) {
                TLM.LOGGER.error("Error while decoding conveyed resource with type {}, replacing with empty", typeId);
                return new ConveyorTray();
            }
            return new ConveyorTray(type.fromNbt(encoded));
        } else {
            return new ConveyorTray();
        }
    }
}
