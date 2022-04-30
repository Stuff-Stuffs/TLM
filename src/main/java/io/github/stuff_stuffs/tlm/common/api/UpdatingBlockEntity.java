package io.github.stuff_stuffs.tlm.common.api;

import net.minecraft.network.PacketByteBuf;

import java.util.function.Consumer;

public interface UpdatingBlockEntity {
    void handleUpdate(PacketByteBuf buf);

    void update(Consumer<PacketByteBuf> consumer);
}
