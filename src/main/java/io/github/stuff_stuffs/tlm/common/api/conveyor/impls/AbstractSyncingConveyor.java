package io.github.stuff_stuffs.tlm.common.api.conveyor.impls;

import io.github.stuff_stuffs.tlm.common.api.resource.ConveyorTray;
import net.minecraft.network.PacketByteBuf;

public abstract class AbstractSyncingConveyor extends AbstractConveyor {
    protected AbstractSyncingConveyor(final float speed) {
        super(speed);
    }

    @Override
    public void readSyncFromBuf(final PacketByteBuf buf) {
        entries.clear();
        final int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            final ConveyorTray tray = ConveyorTray.readFromNbt(buf.readNbt(), true);
            final float pos = buf.readFloat();
            final float tickRemaining = fromByte(buf.readByte());
            final Entry e = new Entry(tray, pos, tickRemaining);
            entries.add(e);
            updatePosition(e, true);
        }
    }

    @Override
    public void writeSyncToBuf(final PacketByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (final Entry entry : entries) {
            buf.writeNbt(entry.getTray().writeToNbt(true));
            buf.writeFloat(entry.getPos());
            buf.writeByte(toByte(entry.getTickRemaining()));
        }
    }

    private static float fromByte(final byte b) {
        return ((int) b - Byte.MIN_VALUE) / (float) (Byte.MAX_VALUE - Byte.MIN_VALUE);
    }

    private static byte toByte(float f) {
        if (f > 1.0F) {
            f = 1.0F;
        } else if (f < 0.0F) {
            f = 0.0F;
        }
        return (byte) Math.round(f * (Byte.MAX_VALUE - Byte.MIN_VALUE));
    }
}
