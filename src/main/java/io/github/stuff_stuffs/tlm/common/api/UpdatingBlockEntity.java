package io.github.stuff_stuffs.tlm.common.api;

import net.minecraft.network.PacketByteBuf;

public interface UpdatingBlockEntity {
    void handleUpdate(PacketByteBuf buf);
}
