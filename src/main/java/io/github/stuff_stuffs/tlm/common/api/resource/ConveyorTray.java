package io.github.stuff_stuffs.tlm.common.api.resource;

import io.github.stuff_stuffs.tlm.common.TLM;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public final class ConveyorTray {
    public static final float TRAY_SIZE = 0.75F;
    public static final Vec3d HALF_EXTENTS = new Vec3d(TRAY_SIZE / 2.0F, TRAY_SIZE / 2.0F, TRAY_SIZE / 2.0F);
    public static final int STACK_COUNT = 2;
    private final ConveyorTrayDataStack firstStack;
    private final ConveyorTrayDataStack secondStack;
    private ConveyedResource<?, ?> resource;
    private ConveyedResourceType<?, ?> last = null;
    private Vec3d lastPos = Vec3d.ZERO;
    private Vec3d currentPos = Vec3d.ZERO;

    private ConveyorTray(final ConveyorTrayDataStack firstStack, final ConveyorTrayDataStack secondStack) {
        resource = null;
        this.firstStack = firstStack;
        this.secondStack = secondStack;
    }

    public ConveyorTray() {
        this(new ConveyorTrayDataStack(), new ConveyorTrayDataStack());
    }

    private ConveyorTray(final ConveyedResource<?, ?> resource, final ConveyorTrayDataStack firstStack, final ConveyorTrayDataStack secondStack) {
        this.firstStack = firstStack;
        this.secondStack = secondStack;
        this.resource = resource;
    }

    public ConveyorTray(final ConveyedResource<?, ?> resource) {
        this(resource, new ConveyorTrayDataStack(), new ConveyorTrayDataStack());
    }

    public void setPosition(final Vec3d pos, final boolean override) {
        if (override) {
            lastPos = pos;
        } else {
            lastPos = currentPos;
        }
        currentPos = pos;
    }

    public ConveyorTrayDataStack getStack(final int index) {
        return switch (index) {
            case 0 -> firstStack;
            case 1 -> secondStack;
            default -> throw new RuntimeException("Invalid stack index: " + index);
        };
    }

    public boolean isSyncNeeded() {
        final ConveyedResourceType<?, ?> current = isEmpty() ? null : resource.getType();
        if (current != last) {
            return true;
        }
        if (resource != null) {
            return resource.isSyncNeeded();
        }
        return false;
    }

    public void clearSyncFlag() {
        if (resource != null) {
            resource.clearSyncFlag();
        }
        last = isEmpty() ? null : resource.getType();
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

    public Box getBounds(final double tickDelta) {
        final Vec3d position = interpolatePosition(tickDelta);
        return new Box(position.subtract(HALF_EXTENTS), position.add(HALF_EXTENTS));
    }

    public NbtCompound writeToNbt(final boolean client) {
        final NbtCompound compound = new NbtCompound();
        if (!isEmpty()) {
            final Optional<NbtCompound> encoded = encode(resource, resource.getType());
            if (encoded.isEmpty()) {
                throw new RuntimeException("Error while encoding resource");
            }
            compound.putString("type", ConveyedResourceType.REGISTRY.getId(resource.getType()).toString());
            compound.put("resource", encoded.get());
        }
        compound.put("firstStack", client ? firstStack.writeToClientNbt() : firstStack.writeToNbt());
        compound.put("secondStack", client ? secondStack.writeToClientNbt() : secondStack.writeToNbt());
        return compound;
    }

    private Optional<NbtCompound> encode(final ConveyedResource<?, ?> resource, final ConveyedResourceType<?, ?> type) {
        return type.cast(resource).map(ConveyedResource::writeToNbt);
    }

    public static ConveyorTray readFromNbt(final NbtCompound compound, final boolean client) {
        final ConveyorTrayDataStack firstStack = new ConveyorTrayDataStack();
        if (client) {
            firstStack.readFromClientNbt(compound.getCompound("firstStack"));
        } else {
            firstStack.readFromNbt(compound.getCompound("firstStack"));
        }
        final ConveyorTrayDataStack secondStack = new ConveyorTrayDataStack();
        if (client) {
            secondStack.readFromClientNbt(compound.getCompound("secondStack"));
        } else {
            secondStack.readFromNbt(compound.getCompound("secondStack"));
        }
        if (compound.contains("resource", NbtElement.COMPOUND_TYPE) && compound.contains("type", NbtElement.STRING_TYPE)) {
            final NbtCompound encoded = compound.getCompound("resource");
            final Identifier typeId = new Identifier(compound.getString("type"));
            final ConveyedResourceType<?, ?> type = ConveyedResourceType.REGISTRY.get(typeId);
            if (type == null) {
                TLM.LOGGER.error("Error while decoding conveyed resource with type {}, replacing with empty", typeId);
                return new ConveyorTray();
            }
            return new ConveyorTray(ConveyedResource.readFromNbt(encoded), firstStack, secondStack);
        } else {
            return new ConveyorTray(firstStack, secondStack);
        }
    }
}
