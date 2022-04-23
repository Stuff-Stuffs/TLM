package io.github.stuff_stuffs.tlm.client.network;

import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.network.UpdatingBlockEntitySender;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public final class UpdatingBlockEntityReceiver {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(UpdatingBlockEntitySender.IDENTIFIER, UpdatingBlockEntityReceiver::receive);
    }

    private static void receive(final MinecraftClient client, final ClientPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final BlockEntityType<?> type = Registry.BLOCK_ENTITY_TYPE.get(buf.readVarInt());
        final BlockPos pos = buf.readBlockPos();
        final PacketByteBuf slice = PacketByteBufs.slice(buf);
        slice.retain();
        client.execute(() -> {
            final BlockEntity blockEntity = client.world.getBlockEntity(pos);
            if (blockEntity instanceof UpdatingBlockEntity updateHandler && blockEntity.getType() == type) {
                updateHandler.handleUpdate(slice);
            }
            slice.release();
        });
    }

    private UpdatingBlockEntityReceiver() {
    }
}
