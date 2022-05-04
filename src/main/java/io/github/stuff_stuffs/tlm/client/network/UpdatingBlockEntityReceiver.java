package io.github.stuff_stuffs.tlm.client.network;

import io.github.stuff_stuffs.tlm.common.api.UpdatingBlockEntity;
import io.github.stuff_stuffs.tlm.common.network.UpdatingBlockEntitySender;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
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

import java.util.Map;

public final class UpdatingBlockEntityReceiver {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(UpdatingBlockEntitySender.IDENTIFIER, UpdatingBlockEntityReceiver::receive);
    }

    private static void receive(final MinecraftClient client, final ClientPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final Map<Key, PacketByteBuf> bufs = new Reference2ReferenceOpenHashMap<>();
        while (buf.readableBytes() > 0) {
            final BlockEntityType<?> type = Registry.BLOCK_ENTITY_TYPE.get(buf.readVarInt());
            final BlockPos pos = buf.readBlockPos();
            final int len = buf.readVarInt();
            bufs.put(new Key(type, pos), PacketByteBufs.readRetainedSlice(buf, len));
        }
        client.execute(() -> {
            for (final Map.Entry<Key, PacketByteBuf> entry : bufs.entrySet()) {
                final BlockEntity entity = client.world.getBlockEntity(entry.getKey().pos());
                if (entity instanceof UpdatingBlockEntity updating && entity.getType() == entry.getKey().type()) {
                    updating.handleUpdate(entry.getValue());
                }
                entry.getValue().release();
            }
        });
    }

    private record Key(BlockEntityType<?> type, BlockPos pos) {
    }

    private UpdatingBlockEntityReceiver() {
    }
}
